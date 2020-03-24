package cn.fantasticmao.dnshooks.proxy.netty;

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
class DnsProxyServerClientHandler extends SimpleChannelInboundHandler<DnsQuery> {
    private final DnsProxyClient client;

    DnsProxyServerClientHandler(@Nonnull DnsProxyClient client) {
        super(false);
        this.client = client;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DnsQuery query) throws Exception {
        if (query instanceof DatagramDnsQuery) {
            DatagramDnsQuery dnsQuery = (DatagramDnsQuery) query;
            log.trace("save queryBefore: {}", dnsQuery);
            ctx.channel().attr(AttributeKeyConstant.QUERY_BEFORE).set(dnsQuery);
        } else {
            // TODO adapter tcp DnsQuery
        }

        final DnsProxyClient.Triplet triplet = this.proxy(query);

        log.trace("save queryAfter: {}", triplet.queryAfter);
        ctx.channel().attr(AttributeKeyConstant.QUERY_AFTER).set(triplet.queryAfter);

        log.trace("save responseBefore: {}", triplet.responseBefore);
        ctx.channel().attr(AttributeKeyConstant.RESPONSE_BEFORE).set(triplet.responseBefore);

        log.trace("write responseAfter to DnsProxyServerDisruptorHandler: {}", triplet.responseAfter);
        ctx.channel().write(triplet.responseAfter);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("read DNS query error", cause);
        ctx.channel().writeAndFlush(ErrorResponseConstant.UDP.DEFAULT);
    }

    private DnsProxyClient.Triplet proxy(@Nonnull DnsQuery query) {
        // TODO filter 127.0.0.1:53 && chose DNS server address
        try {
            List<InetSocketAddress> dnsServerAddressList = DnsServerAddressUtil.listRawDnsServerAddress();
            final InetSocketAddress dnsServerAddress = dnsServerAddressList.get(0);
            log.trace("chose DNS server address: {}", dnsServerAddress);
            return client.lookup(dnsServerAddress, query);
        } catch (Exception e) {
            log.error("proxy request error", e);
            return new DnsProxyClient.Triplet(null, null, ErrorResponseConstant.UDP.DEFAULT);
        }
    }
}
