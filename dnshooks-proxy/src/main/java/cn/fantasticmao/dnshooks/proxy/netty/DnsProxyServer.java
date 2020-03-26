package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.netty.handler.DnsProxyServerClientHandler;
import cn.fantasticmao.dnshooks.proxy.netty.handler.DnsProxyServerDisruptorHandler;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQueryDecoder;
import io.netty.handler.codec.dns.DatagramDnsResponseEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;

/**
 * DnsProxyServer
 *
 * @author maomao
 * @since 2020-03-11
 */
@Slf4j
@Immutable
public class DnsProxyServer implements AutoCloseable {
    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;
    private Channel channel;

    public DnsProxyServer(@Nonnull InetSocketAddress proxyServerAddress,
                          @Nonnull DnsProxyClient proxyClient,
                          @Nonnull Disruptor<DnsMessage> disruptor) {
        // config netty Bootstrap
        this.workerGroup = new NioEventLoopGroup(new DefaultThreadFactory("DnsProxyServer"));
        this.bootstrap = new Bootstrap()
            .group(workerGroup)
            .channel(NioDatagramChannel.class)
            .localAddress(proxyServerAddress)
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

    public void start() throws Exception {
        this.channel = this.bootstrap.bind().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("start DNSHooks-Proxy success");
                } else {
                    log.error("start DNSHooks-Proxy error", future.cause());
                }
            }
        }).sync().channel();

        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("stop DNSHooks-Proxy success");
                } else {
                    log.error("stop DNSHooks-Proxy error", future.cause());
                }
            }
        });
    }

    @Override
    public void close() throws Exception {
        if (channel != null) {
            channel.close();
        }
        this.workerGroup.shutdownGracefully();
    }

}
