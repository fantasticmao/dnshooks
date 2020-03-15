package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQueryEncoder;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

/**
 * DnsProxyClient
 *
 * @author maomao
 * @since 2020-03-12
 */
class DnsProxyClient implements AutoCloseable {
    private final int port;
    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;

    public DnsProxyClient() {
        this.port = 53;
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
                        .addLast(new DnsProxyClientHandler());
                }
            });
    }

    public DnsResponse lookup(final DnsQuery dnsQuery) throws InterruptedException {
        ChannelFuture channelFuture = this.bootstrap.connect("192.168.1.1", this.port).sync();
        channelFuture.channel().writeAndFlush(dnsQuery).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                }
            }
        });
        channelFuture.channel().closeFuture().sync();
        return null;
    }

    @Override
    public void close() {
        this.workerGroup.shutdownGracefully();
    }
}
