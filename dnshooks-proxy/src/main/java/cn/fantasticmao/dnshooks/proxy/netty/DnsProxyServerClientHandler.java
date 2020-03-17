package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * DnsProxyServerClientHandler
 *
 * @author maomao
 * @since 2020-03-17
 */
@ChannelHandler.Sharable
public class DnsProxyServerClientHandler extends SimpleChannelInboundHandler<DnsQuery> {
    private final DnsProxyClient client;
    static final String QUERY_ATTRIBUTE_KEY = "query";

    public DnsProxyServerClientHandler(DnsProxyClient client) {
        super(false);
        this.client = client;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DnsQuery query) throws Exception {
        final AttributeKey<DnsQuery> key = AttributeKey.valueOf(QUERY_ATTRIBUTE_KEY);
        ctx.channel().attr(key).set(query);

        DnsResponse response = this.proxy(query);
        ctx.pipeline().write(response).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    private DnsResponse proxy(DnsQuery query) throws Exception {
        // TODO filter 127.0.0.1:53 && chose DNS server address
        List<InetSocketAddress> dnsServerAddressList = DnsServerAddressUtil.listRawDnsServerAddress();
        return client.lookup(dnsServerAddressList.get(0), query);
    }
}
