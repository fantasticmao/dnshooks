package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DnsQuery;

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
            ctx.channel().attr(AttributeKeyConstant.QUERY_BEFORE).set((DatagramDnsQuery) query);
        } else {
            // TODO adapter tcp DnsQuery
        }

        final DnsProxyClient.Triplet triplet = this.proxy(query);

        // save query after DNSHooks proxy
        ctx.channel().attr(AttributeKeyConstant.QUERY_AFTER).set(triplet.queryAfter);

        // save response before DNSHooks proxy
        ctx.channel().attr(AttributeKeyConstant.RESPONSE_BEFORE).set(triplet.responseBefore);

        // send response after DNSHooks proxy to next channel handler
        ctx.pipeline().write(triplet.responseAfter).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    private DnsProxyClient.Triplet proxy(@Nonnull DnsQuery query) throws Exception {
        // TODO filter 127.0.0.1:53 && chose DNS server address
        List<InetSocketAddress> dnsServerAddressList = DnsServerAddressUtil.listRawDnsServerAddress();
        return client.lookup(dnsServerAddressList.get(0), query);
    }
}
