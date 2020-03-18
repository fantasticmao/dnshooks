package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * DnsProxyDatagramClient
 *
 * @author maomao
 * @since 2020-03-12
 */
class DnsProxyDatagramClient extends DnsProxyClient {
    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;
    private static final String SENDER_ATTRIBUTE_KEY = "rawSender";

    DnsProxyDatagramClient() {
        this.workerGroup = new NioEventLoopGroup(new DefaultThreadFactory("DnsProxyDatagramClient"));
        this.bootstrap = new Bootstrap()
            .group(workerGroup)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new ProxyQueryEncoder())
                        .addLast(new ProxyResponseDecoder())
                        .addLast(new ObtainMessageChannelHandler<>(DatagramDnsResponse.class));
                }
            });
    }

    @Override
    protected DnsResponse lookup(final InetSocketAddress nameServer, final DnsQuery query)
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

    @ChannelHandler.Sharable
    private static class ProxyQueryEncoder extends DatagramDnsQueryEncoder {

        @Override
        protected void encode(ChannelHandlerContext ctx,
                              AddressedEnvelope<DnsQuery, InetSocketAddress> in, List<Object> out) throws Exception {
            // save raw sender
            final AttributeKey<InetSocketAddress> key = AttributeKey.valueOf(DnsProxyDatagramClient.SENDER_ATTRIBUTE_KEY);
            ctx.channel().attr(key).set(in.sender());

            // get DNS server address from channel
            final InetSocketAddress recipient = (InetSocketAddress) ctx.channel().remoteAddress();

            AddressedEnvelopeAdapter queryProxy = new AddressedEnvelopeAdapter(in.sender(), recipient, in);
            super.encode(ctx, queryProxy, out);
        }
    }

    @ChannelHandler.Sharable
    private static class ProxyResponseDecoder extends DatagramDnsResponseDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
            // obtain raw sender, and it is the raw recipient in DnsResponse
            final AttributeKey<InetSocketAddress> key = AttributeKey.valueOf(DnsProxyDatagramClient.SENDER_ATTRIBUTE_KEY);
            final InetSocketAddress rawRecipient = ctx.channel().attr(key).get();

            // get DNS server address from channel
            final InetSocketAddress recipient = (InetSocketAddress) ctx.channel().remoteAddress();

            DatagramPacket responseProxy = new DatagramPacket(packet.content(), rawRecipient, recipient);
            super.decode(ctx, responseProxy, out);
        }
    }
}
