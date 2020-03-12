package cn.fantasticmao.dnshooks.proxy;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
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

import java.util.concurrent.TimeUnit;

/**
 * DnsProxyServer
 *
 * @author maomao
 * @since 2020-03-11
 */
class DnsProxyServer implements Runnable, AutoCloseable {
    private final int port;
    private final Disruptor<DnsRecordMessage> disruptor;

    DnsProxyServer(int ringBufferSize) {
        this.port = 53;
        // use blocking wait strategy，avoid to cost a lot of CPU resource
        final WaitStrategy waitStrategy = new BlockingWaitStrategy();
        this.disruptor = new Disruptor<>(DnsRecordMessageFactory.INSTANCE, ringBufferSize,
            HookThreadFactory.INSTANCE, ProducerType.MULTI, waitStrategy);

        final EventHandler<DnsRecordMessage>[] handlers = new DnsRecordMessageHandler[]{
            new DnsRecordMessageHandler()
        };
        disruptor.handleEventsWith(handlers);

        // start disruptor
        this.disruptor.start();
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
                        .addLast(new DnsProxyHandler(DnsProxyServer.this.disruptor));
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

    @Override
    public void close() throws Exception {
        this.disruptor.shutdown(1000, TimeUnit.MILLISECONDS);
    }
}
