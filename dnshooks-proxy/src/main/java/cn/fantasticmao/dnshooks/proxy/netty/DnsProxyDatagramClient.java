package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.util.Constant;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * DnsProxyDatagramClient
 *
 * @author maomao
 * @since 2020-03-12
 */
@Slf4j
@Immutable
public class DnsProxyDatagramClient extends DnsProxyClient {
    private final InetSocketAddress localAddress;
    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;

    public DnsProxyDatagramClient(@Nonnull final InetSocketAddress localAddress) {
        this.localAddress = localAddress;
        this.workerGroup = new NioEventLoopGroup(new DefaultThreadFactory("DnsProxyDatagramClient"));
        this.bootstrap = new Bootstrap()
            .group(workerGroup)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Constant.LOOKUP_TIMEOUT)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                /*
                 * Notice: the DnsProxyDatagramClient instance object reference (this keyword) is escaped from the
                 * constructor, but DnsProxyDatagramClient is a thread safe class, so it's no problem.
                 */
                @Override
                protected void initChannel(NioDatagramChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new DatagramProxyQueryEncoder(DnsProxyDatagramClient.this))
                        .addLast(new DatagramProxyResponseDecoder(DnsProxyDatagramClient.this))
                        .addLast(new ObtainMessageChannelHandler<>(DatagramDnsResponse.class));
                }
            });
    }

    @Nonnull
    @Override
    protected InetSocketAddress getLocalAddress() {
        return this.localAddress;
    }

    @Nonnull
    @Override
    protected Triplet lookup(@Nonnull final InetSocketAddress nameServer, @Nonnull final DnsQuery query)
        throws Exception {
        if (!(query instanceof DatagramDnsQuery)) {
            throw new IllegalArgumentException(query.getClass().getName() + "cannot case to "
                + DatagramDnsQuery.class.getName());
        }
        // TODO should need to cache netty channel?
        log.trace("connect to DNS server: {}", nameServer);
        final Channel channel = this.bootstrap.connect(nameServer).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    log.error("connect to " + nameServer + " error", future.cause());
                }
            }
        }).sync().channel();
        try {
            log.trace("write queryBefore: {} to DNS server: {}", query, nameServer);
            channel.writeAndFlush(query.retain()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        log.error("write query " + query + " to  DNS server " + nameServer + " error", future.cause());
                    }
                }
            });

            @SuppressWarnings("unchecked")
            ObtainMessageChannelHandler<DatagramDnsResponse> obtainMessageChannelHandler =
                channel.pipeline().get(ObtainMessageChannelHandler.class);
            final DatagramDnsResponse responseAfter = obtainMessageChannelHandler.getMessage();

            final AddressedEnvelope<? extends DnsQuery, InetSocketAddress> queryAfter
                = channel.attr(AttributeKeyConstant.QUERY_AFTER).get();
            log.trace("obtain queryAfter: {}", queryAfter);

            final AddressedEnvelope<? extends DnsResponse, InetSocketAddress> responseBefore
                = channel.attr(AttributeKeyConstant.RESPONSE_BEFORE).get();
            log.trace("obtain responseBefore: {}", responseBefore);

            return new Triplet(queryAfter, responseBefore, responseAfter);
        } finally {
            channel.close();
        }
    }

    @Override
    public void close() throws Exception {
        this.workerGroup.shutdownGracefully();
    }

    @Slf4j
    @Immutable
    @ChannelHandler.Sharable
    private static class DatagramProxyQueryEncoder extends DatagramDnsQueryEncoder implements ProxyQueryEncoder {
        private final DnsProxyDatagramClient client;

        public DatagramProxyQueryEncoder(@Nonnull DnsProxyDatagramClient client) {
            super();
            this.client = client;
        }

        @Override
        protected void encode(ChannelHandlerContext ctx,
                              AddressedEnvelope<DnsQuery, InetSocketAddress> in, List<Object> out) throws Exception {
            log.trace("save raw sender: {}", in.sender());
            ctx.channel().attr(AttributeKeyConstant.RAW_SENDER).set(in.sender());

            // get DNS server address from channel
            final InetSocketAddress recipient = (InetSocketAddress) ctx.channel().remoteAddress();

            AddressedEnvelopeAdapter queryProxy = new AddressedEnvelopeAdapter(null, recipient, in);

            log.trace("save queryAfter: {}", queryProxy);
            ctx.channel().attr(AttributeKeyConstant.QUERY_AFTER).set(queryProxy);

            super.encode(ctx, queryProxy, out);
        }
    }

    @Slf4j
    @Immutable
    @ChannelHandler.Sharable
    private static class DatagramProxyResponseDecoder extends DatagramDnsResponseDecoder implements ProxyResponseDecoder {
        private final DnsProxyDatagramClient client;

        public DatagramProxyResponseDecoder(@Nonnull DnsProxyDatagramClient client) {
            this.client = client;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
            DatagramDnsResponse responseBefore = (DatagramDnsResponse) super.decodeResponse(null, packet.copy());
            log.trace("save responseBefore: {}", responseBefore);
            ctx.channel().attr(AttributeKeyConstant.RESPONSE_BEFORE).set(responseBefore);

            // obtain raw sender, and it is the raw recipient in DnsResponse
            final InetSocketAddress recipient = ctx.channel().attr(AttributeKeyConstant.RAW_SENDER).get();
            log.trace("obtain raw sender: {}", recipient);

            DatagramPacket responseProxy = new DatagramPacket(packet.content(), recipient, client.localAddress);
            super.decode(ctx, responseProxy, out);
        }
    }
}
