package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageFactory;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;
import cn.fantasticmao.dnshooks.proxy.disruptor.HookThreadFactory;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQueryDecoder;
import io.netty.handler.codec.dns.DatagramDnsResponseEncoder;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.util.concurrent.DefaultThreadFactory;
import jdk.nashorn.internal.ir.annotations.Immutable;

import javax.annotation.Nonnegative;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

/**
 * DnsProxyServer
 *
 * @author maomao
 * @since 2020-03-11
 */
@Immutable
public class DnsProxyServer implements AutoCloseable {
    private final Disruptor<DnsMessage<DnsQuery, DnsResponse>> disruptor;
    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;
    private final DnsProxyClient proxyClient;

    public DnsProxyServer(@Nonnegative int ringBufferSize) {
        // use blocking wait strategy，avoid to cost a lot of CPU resources
        final WaitStrategy waitStrategy = new BlockingWaitStrategy();
        this.disruptor = new Disruptor<>(DnsMessageFactory.INSTANCE, ringBufferSize,
            HookThreadFactory.INSTANCE, ProducerType.MULTI, waitStrategy);
        // register all DNS hooks as event handler to Disruptor
        this.disruptor.handleEventsWith(this.loadHooks().toArray(new DnsMessageHook[0]));
        // start Disruptor
        this.disruptor.start();

        final InetSocketAddress localAddress = new InetSocketAddress(53);
        // new DnsProxyClient instance
        this.proxyClient = new DnsProxyDatagramClient(localAddress);

        // config netty Bootstrap
        this.workerGroup = new NioEventLoopGroup(new DefaultThreadFactory("DnsProxyServer"));
        this.bootstrap = new Bootstrap()
            .group(workerGroup)
            .channel(NioDatagramChannel.class)
            .localAddress(localAddress)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new DatagramDnsQueryDecoder())
                        .addLast(new DnsProxyServerClientHandler(DnsProxyServer.this.proxyClient))
                        .addLast(new DatagramDnsResponseEncoder())
                        .addLast(new DnsProxyServerDisruptorHandler(DnsProxyServer.this.disruptor));
                }
            });
    }

    public void run() throws Exception {
        ChannelFuture future = this.bootstrap.bind().sync();
        System.out.println("start DNSHooks-Proxy success");
        future.channel().closeFuture().sync().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                }
            }
        });
    }

    @Override
    public void close() throws Exception {
        this.proxyClient.close();
        this.disruptor.shutdown(1000, TimeUnit.MILLISECONDS);
        this.workerGroup.shutdownGracefully();
    }

    /**
     * use ServiceLoader to find all DNS hooks
     *
     * @return {@link DnsMessageHook} list
     */
    private List<DnsMessageHook> loadHooks() {
        List<DnsMessageHook> handlerList = new LinkedList<>();
        ServiceLoader.load(DnsMessageHook.class).forEach(handlerList::add);
        return handlerList;
    }
}
