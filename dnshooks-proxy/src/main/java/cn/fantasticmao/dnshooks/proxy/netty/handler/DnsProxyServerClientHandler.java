package cn.fantasticmao.dnshooks.proxy.netty.handler;

import cn.fantasticmao.dnshooks.proxy.netty.AttributeKeyConstant;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyClient;
import cn.fantasticmao.dnshooks.proxy.netty.handler.codec.DnsMessageTriplet;
import cn.fantasticmao.dnshooks.proxy.netty.handler.codec.DnsMessageUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.DnsResponseCode;
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

        final DnsMessageTriplet dnsMessageTriplet = this.proxy(query);

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
        log.error("An exception '" + cause.getMessage() + "' was thrown by " + this.getClass(), cause);
        ctx.fireExceptionCaught(cause);
    }

    private DnsMessageTriplet proxy(@Nonnull DnsQuery query) throws IllegalArgumentException {
        try {
            return this.client.lookup(query);
        } catch (Exception e) {
            log.error(client.getClass() + " lookup DNS query: " + query +
                " error, DNSHooks-Proxy will return an error response", e);
            if (query instanceof DatagramDnsQuery) {
                DatagramDnsQuery dnsQuery = (DatagramDnsQuery) query;
                DnsResponse response = DnsMessageUtil.newErrorUdpResponse(dnsQuery, DnsResponseCode.SERVFAIL);
                return new DnsMessageTriplet(null, null, response);
            } else {
                throw new IllegalArgumentException("DNSHooks-Proxy does only support UDP DNS until now");
            }
        }
    }
}
