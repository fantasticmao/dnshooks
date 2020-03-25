package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.netty.handler.ObtainMessageChannelHandler;
import cn.fantasticmao.dnshooks.proxy.netty.handler.ProxyQueryEncoder;
import cn.fantasticmao.dnshooks.proxy.netty.handler.ProxyResponseDecoder;
import cn.fantasticmao.dnshooks.proxy.netty.handler.codec.DnsMessageTriplet;
import cn.fantasticmao.dnshooks.proxy.util.Constant;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DatagramDnsResponse;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.net.InetSocketAddress;

/**
 * DnsProxyDatagramClient
 *
 * @author maomao
 * @since 2020-03-12
 */
@Slf4j
@Immutable
public class DnsProxyDatagramClient extends DnsProxyClient {
    private final InetSocketAddress localAddress;
    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;

    public DnsProxyDatagramClient(@Nonnull final InetSocketAddress localAddress) {
        this.localAddress = localAddress;
        this.workerGroup = new NioEventLoopGroup(new DefaultThreadFactory("DnsProxyDatagramClient"));
        this.bootstrap = new Bootstrap()
            .group(workerGroup)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Constant.LOOKUP_TIMEOUT)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                /*
                 * Notice: the DnsProxyDatagramClient instance object reference (this keyword) is escaped from the
                 * constructor, but DnsProxyDatagramClient is a thread safe class, so it's no problem.
                 */
                @Override
                protected void initChannel(NioDatagramChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new ProxyQueryEncoder.Udp(DnsProxyDatagramClient.this))
                        .addLast(new ProxyResponseDecoder.Udp(DnsProxyDatagramClient.this))
                        .addLast(new ObtainMessageChannelHandler<>(DatagramDnsResponse.class));
                }
            });
    }

    @Nonnull
    public InetSocketAddress getLocalAddress() {
        return this.localAddress;
    }

    @Nonnull
    @Override
    public DnsMessageTriplet lookup(@Nonnull final InetSocketAddress nameServer, @Nonnull final DnsQuery query)
        throws Exception {
        if (!(query instanceof DatagramDnsQuery)) {
            throw new IllegalArgumentException(query.getClass().getName() + "cannot case to "
                + DatagramDnsQuery.class.getName());
        }
        // TODO should need to cache netty channel?
        log.trace("connect to DNS Server: {}", nameServer);
        final Channel channel = this.bootstrap.connect(nameServer).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    log.error("connect to DNS Server: " + nameServer + " error", future.cause());
                }
            }
        }).sync().channel();
        try {
            log.trace("write DNS Query: {} to DNS Server: {}", query, nameServer);
            channel.writeAndFlush(query.retain()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        log.error("write DNS Query: " + query + " to  DNS Server " + nameServer + " error", future.cause());
                    }
                }
            });

            @SuppressWarnings("unchecked")
            ObtainMessageChannelHandler<DatagramDnsResponse> obtainMessageChannelHandler =
                channel.pipeline().get(ObtainMessageChannelHandler.class);
            final DatagramDnsResponse responseAfter = obtainMessageChannelHandler.getMessage();

            final DnsQuery queryAfter = channel.attr(AttributeKeyConstant.QUERY_AFTER).get();
            log.trace("obtain DnsQuery after DNSHooks-Proxy: {}", queryAfter);

            final DnsResponse responseBefore = channel.attr(AttributeKeyConstant.RESPONSE_BEFORE).get();
            log.trace("obtain DnsResponse before DNSHooks-Proxy: {}", responseBefore);

            return new DnsMessageTriplet(queryAfter, responseBefore, responseAfter);
        } finally {
            channel.close();
        }
    }

    @Override
    public void close() throws Exception {
        this.workerGroup.shutdownGracefully();
    }
}
