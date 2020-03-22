package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.channel.*;
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
class DnsProxyServerClientHandler extends SimpleChannelInboundHandler<DnsQuery> {
    private final DnsProxyClient client;

    DnsProxyServerClientHandler(@Nonnull DnsProxyClient client) {
        super(false);
        this.client = client;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DnsQuery query) throws Exception {
        // save query before DNSHooks proxy
        if (query instanceof DatagramDnsQuery) {
            DatagramDnsQuery dnsQuery = (DatagramDnsQuery) query;
            ctx.channel().attr(AttributeKeyConstant.QUERY_BEFORE).set(dnsQuery);
        } else {
            // TODO adapter tcp DnsQuery
        }

        final DnsProxyClient.Triplet triplet = this.proxy(query);

        // save query after DNSHooks proxy
        ctx.channel().attr(AttributeKeyConstant.QUERY_AFTER).set(triplet.queryAfter);

        // save response before DNSHooks proxy
        ctx.channel().attr(AttributeKeyConstant.RESPONSE_BEFORE).set(triplet.responseBefore);

        // send response after DNSHooks proxy to next channel handler
        ctx.channel().write(triplet.responseAfter);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("read query error", cause);
        ctx.channel().writeAndFlush(ErrorResponseConstant.UDP.DEFAULT);
    }

    private DnsProxyClient.Triplet proxy(@Nonnull DnsQuery query) {
        // TODO filter 127.0.0.1:53 && chose DNS server address
        try {
            List<InetSocketAddress> dnsServerAddressList = DnsServerAddressUtil.listRawDnsServerAddress();
            return client.lookup(dnsServerAddressList.get(0), query);
        } catch (Exception e) {
            log.error("proxy request error", e);
            return new DnsProxyClient.Triplet(null, null, ErrorResponseConstant.UDP.DEFAULT);
        }
    }
}
