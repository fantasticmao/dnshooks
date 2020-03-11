package cn.fantasticmao.dnshooks.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQueryDecoder;
import io.netty.handler.codec.dns.DatagramDnsResponseEncoder;

/**
 * DnsProxyServer
 *
 * @author maomao
 * @since 2020-03-11
 */
public class DnsProxyServer implements Runnable {
    private final int port;

    public DnsProxyServer() {
        this.port = 53;
    }

    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
            .group(group)
            .channel(NioDatagramChannel.class)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel ch) throws Exception {
                    ch.pipeline().addLast(new DatagramDnsQueryDecoder())
                        .addLast(new DatagramDnsResponseEncoder())
                        .addLast(new DnsProxyServerHandler());
                }
            }).option(ChannelOption.SO_BROADCAST, true);

        try {
            ChannelFuture future = bootstrap.bind(this.port).sync();
            System.out.println("start DNSHooks server success!");

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
