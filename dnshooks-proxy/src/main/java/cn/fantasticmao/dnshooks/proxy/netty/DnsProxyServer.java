package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQueryDecoder;
import io.netty.handler.codec.dns.DatagramDnsResponseEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import jdk.nashorn.internal.ir.annotations.Immutable;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * DnsProxyServer
 *
 * @author maomao
 * @since 2020-03-11
 */
@Immutable
public class DnsProxyServer implements AutoCloseable {
    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;

    public DnsProxyServer(@Nonnull DnsProxyClient proxyClient,
                          @Nonnegative Disruptor<DnsMessage> disruptor) {
        // config netty Bootstrap
        this.workerGroup = new NioEventLoopGroup(new DefaultThreadFactory("DnsProxyServer"));
        this.bootstrap = new Bootstrap()
            .group(workerGroup)
            .channel(NioDatagramChannel.class)
            .localAddress(proxyClient.getLocalAddress())
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new DatagramDnsQueryDecoder())
                        .addLast(new DnsProxyServerClientHandler(proxyClient))
                        .addLast(new DatagramDnsResponseEncoder())
                        .addLast(new DnsProxyServerDisruptorHandler(disruptor));
                }
            });
    }

    public void run() throws Exception {
        ChannelFuture future = this.bootstrap.bind().sync();
        System.out.println("start DNSHooks-Proxy success");
        future.channel().closeFuture().sync().addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    @Override
    public void close() throws Exception {
        this.workerGroup.shutdownGracefully();
    }

}
