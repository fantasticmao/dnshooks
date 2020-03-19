package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;
import io.netty.util.concurrent.DefaultThreadFactory;

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
@Immutable
class DnsProxyDatagramClient extends DnsProxyClient {
    private final InetSocketAddress localAddress;
    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;

    DnsProxyDatagramClient(@Nonnull final InetSocketAddress localAddress) {
        this.localAddress = localAddress;
        this.workerGroup = new NioEventLoopGroup(new DefaultThreadFactory("DnsProxyDatagramClient"));
        this.bootstrap = new Bootstrap()
            .group(workerGroup)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
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

    @Override
    protected DnsResponse lookup(@Nonnull final InetSocketAddress nameServer, @Nonnull final DnsQuery query)
        throws InterruptedException {
        if (!(query instanceof DatagramDnsQuery)) {
            throw new IllegalArgumentException(query.getClass().getName() + "cannot case to "
                + DatagramDnsQuery.class.getName());
        }
        Channel channel = this.bootstrap.connect(nameServer).sync().channel();
        try {
            channel.writeAndFlush(query.retain()).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            @SuppressWarnings("unchecked")
            ObtainMessageChannelHandler<DatagramDnsResponse> obtainMessageChannelHandler =
                (ObtainMessageChannelHandler<DatagramDnsResponse>) channel.pipeline().get(ObtainMessageChannelHandler.class);
            return obtainMessageChannelHandler.getMessage();
        } finally {
            channel.close();
        }
    }

    @Override
    public void close() {
        this.workerGroup.shutdownGracefully();
    }

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
            // save raw sender
            ctx.channel().attr(AttributeKeyConstant.RAW_SENDER).set(in.sender());

            // get DNS server address from channel
            final InetSocketAddress recipient = (InetSocketAddress) ctx.channel().remoteAddress();

            AddressedEnvelopeAdapter queryProxy = new AddressedEnvelopeAdapter(null, recipient, in);
            System.out.printf("queryAfter from: %s to: %s%n", queryProxy.sender(), queryProxy.recipient());
            super.encode(ctx, queryProxy, out);
        }
    }

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
            System.out.printf("responseBefore from: %s to: %s%n", responseBefore.sender(), responseBefore.recipient());

            // obtain raw sender, and it is the raw recipient in DnsResponse
            final InetSocketAddress recipient = ctx.channel().attr(AttributeKeyConstant.RAW_SENDER).get();

            DatagramPacket responseProxy = new DatagramPacket(packet.content(), recipient, client.localAddress);
            super.decode(ctx, responseProxy, out);
        }
    }
}
