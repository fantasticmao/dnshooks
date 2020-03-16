package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DatagramDnsQueryEncoder;
import io.netty.handler.codec.dns.DatagramDnsResponseDecoder;
import io.netty.handler.codec.dns.DnsResponse;

import java.net.InetSocketAddress;

/**
 * DnsProxyClient
 *
 * @author maomao
 * @since 2020-03-12
 */
class DnsProxyClient implements AutoCloseable {
    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;

    public DnsProxyClient() {
        this.workerGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap()
            .group(workerGroup)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new DatagramDnsQueryEncoder())
                        .addLast(new DatagramDnsResponseDecoder())
                        .addLast(new DnsProxyClientHandler());
                }
            });
    }

    public DnsResponse lookup(final InetSocketAddress nameServer, final DatagramDnsQuery query) throws InterruptedException {
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
        this.workerGroup.shutdownGracefully();
    }
}
