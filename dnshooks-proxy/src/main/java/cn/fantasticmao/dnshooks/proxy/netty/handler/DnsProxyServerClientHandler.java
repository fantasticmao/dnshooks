package cn.fantasticmao.dnshooks.proxy.netty.handler;

import cn.fantasticmao.dnshooks.proxy.netty.AttributeKeyConstant;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyClient;
import cn.fantasticmao.dnshooks.proxy.netty.ErrorResponseConstant;
import cn.fantasticmao.dnshooks.proxy.netty.handler.codec.DnsMessageTriplet;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.DnsQuery;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * DnsProxyServerClientHandler
 *
 * @author maomao
 * @since 2020-03-17
 */
@Slf4j
@Immutable
@ChannelHandler.Sharable
public class DnsProxyServerClientHandler extends SimpleChannelInboundHandler<DnsQuery> {
    private final DnsProxyClient client;

    public DnsProxyServerClientHandler(@Nonnull DnsProxyClient client) {
        super(false);
        this.client = client;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DnsQuery query) throws Exception {
        log.trace("save DnsQuery before DNSHooks-Proxy: {}", query);
        ctx.channel().attr(AttributeKeyConstant.QUERY_BEFORE).set(query);

        final DnsMessageTriplet dnsMessageTriplet = this.client.lookup(query);

        log.trace("save DnsQuery after DNSHooks-Proxy: {}", dnsMessageTriplet.getQueryAfter());
        ctx.channel().attr(AttributeKeyConstant.QUERY_AFTER).set(dnsMessageTriplet.getQueryAfter());

        log.trace("save DnsResponse before DNSHooks-Proxy: {}", dnsMessageTriplet.getResponseBefore());
        ctx.channel().attr(AttributeKeyConstant.RESPONSE_BEFORE).set(dnsMessageTriplet.getResponseBefore());

        log.trace("write DNS Response: {} to DnsProxyServerDisruptorHandler",
            dnsMessageTriplet.getResponseAfter());
        ctx.channel().write(dnsMessageTriplet.getResponseAfter());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("read DNS Query error", cause);
        ctx.channel().writeAndFlush(ErrorResponseConstant.UDP.ERROR);
    }
}
