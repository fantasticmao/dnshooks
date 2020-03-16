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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQueryDecoder;
import io.netty.handler.codec.dns.DatagramDnsResponseEncoder;

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
public class DnsProxyServer implements AutoCloseable {
    private final Disruptor<DnsMessage> disruptor;
    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;

    public DnsProxyServer(int ringBufferSize) {
        // use blocking wait strategy，avoid to cost a lot of CPU resources
        final WaitStrategy waitStrategy = new BlockingWaitStrategy();
        this.disruptor = new Disruptor<>(DnsMessageFactory.INSTANCE, ringBufferSize,
            HookThreadFactory.INSTANCE, ProducerType.MULTI, waitStrategy);
        // use ServiceLoader to find all DNS hooks
        List<DnsMessageHook> handlerList = new LinkedList<>();
        ServiceLoader.load(DnsMessageHook.class).forEach(handlerList::add);
        // register all DNS hooks as event handler to disruptor
        this.disruptor.handleEventsWith(handlerList.toArray(new DnsMessageHook[0]));
        // start disruptor
        this.disruptor.start();

        // config netty Bootstrap
        this.workerGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap()
            .group(workerGroup)
            .channel(NioDatagramChannel.class)
            .localAddress(53)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new DatagramDnsQueryDecoder())
                        .addLast(new DatagramDnsResponseEncoder())
                        .addLast(new DnsProxyServerDatagramHandler(DnsProxyServer.this.disruptor));
                }
            });
    }

    public void run() throws Exception {
        ChannelFuture future = this.bootstrap.bind().sync();
        System.out.println("start DNSHooks-Proxy success");
        future.channel().closeFuture().sync();
    }

    @Override
    public void close() throws Exception {
        this.disruptor.shutdown(1000, TimeUnit.MILLISECONDS);
        this.workerGroup.shutdownGracefully();
    }
}
