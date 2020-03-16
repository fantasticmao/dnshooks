package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;
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
    private static final ThreadLocal<InetSocketAddress> RAW_DNS_SENDER_THREAD_LOCAL = new ThreadLocal<>();

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
                        .addLast(new QueryEncoder())
                        .addLast(new ResponseDecoder())
                        .addLast(new DnsProxyClientHandler());
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
            channel.writeAndFlush(query).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        future.cause().printStackTrace();
                    }
                }
            });
            return channel.pipeline().get(DnsProxyClientHandler.class).getResponse();
        } finally {
            channel.close();
        }
    }

    @Override
    public void close() {
        DnsProxyDatagramClient.RAW_DNS_SENDER_THREAD_LOCAL.remove();
        this.workerGroup.shutdownGracefully();
    }

    private static class QueryEncoder extends DatagramDnsQueryEncoder {

        @Override
        protected void encode(ChannelHandlerContext ctx, AddressedEnvelope<DnsQuery, InetSocketAddress> in, List<Object> out) throws Exception {
            DnsProxyDatagramClient.RAW_DNS_SENDER_THREAD_LOCAL.set(in.sender());
            // TODO chose DNS server address
            InetSocketAddress proxyRecipient = DnsServerAddressUtil.listRawDnsServerAddress().get(0);
            AddressedEnvelopeAdapter queryProxy = new AddressedEnvelopeAdapter(in.sender(), proxyRecipient, in);
            super.encode(ctx, queryProxy, out);
        }
    }

    private static class ResponseDecoder extends DatagramDnsResponseDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
            InetSocketAddress rawRecipient = DnsProxyDatagramClient.RAW_DNS_SENDER_THREAD_LOCAL.get();
            DatagramPacket responseProxy = new DatagramPacket(packet.content(), rawRecipient, packet.sender());
            super.decode(ctx, responseProxy, out);
        }
    }
}
