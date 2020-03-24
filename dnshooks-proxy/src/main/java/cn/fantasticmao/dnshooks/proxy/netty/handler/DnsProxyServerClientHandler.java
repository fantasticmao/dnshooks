package cn.fantasticmao.dnshooks.proxy.netty.handler;

import cn.fantasticmao.dnshooks.proxy.netty.AttributeKeyConstant;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyClient;
import cn.fantasticmao.dnshooks.proxy.netty.DnsServerAddressUtil;
import cn.fantasticmao.dnshooks.proxy.netty.ErrorResponseConstant;
import cn.fantasticmao.dnshooks.proxy.netty.handler.codec.DnsMessageTriplet;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DnsQuery;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.net.InetSocketAddress;
import java.util.List;

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
        if (query instanceof DatagramDnsQuery) {
            DatagramDnsQuery dnsQuery = (DatagramDnsQuery) query;
            log.trace("save DnsQuery before DNSHooks-Proxy: {}", dnsQuery);
            ctx.channel().attr(AttributeKeyConstant.QUERY_BEFORE).set(dnsQuery);
        } else {
            // TODO adapter tcp DnsQuery
        }

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
        log.error("read DNS Query error", cause);
        ctx.channel().writeAndFlush(ErrorResponseConstant.UDP.DEFAULT);
    }

    private DnsMessageTriplet proxy(@Nonnull DnsQuery query) {
        // TODO filter 127.0.0.1:53 && chose DNS server address
        try {
            List<InetSocketAddress> dnsServerAddressList = DnsServerAddressUtil.listRawDnsServerAddress();
            final InetSocketAddress dnsServerAddress = dnsServerAddressList.get(0);
            log.trace("chose DNS Server Address: {}", dnsServerAddress);
            return client.lookup(dnsServerAddress, query);
        } catch (Exception e) {
            log.error("proxy DNS Query error", e);
            return new DnsMessageTriplet(null, null, ErrorResponseConstant.UDP.DEFAULT);
        }
    }
}
