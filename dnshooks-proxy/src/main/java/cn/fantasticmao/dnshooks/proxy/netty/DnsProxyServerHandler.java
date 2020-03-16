package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageTranslator;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * DnsProxyServerHandler
 *
 * @author maomao
 * @since 2020-03-16
 */
class DnsProxyServerHandler extends SimpleChannelInboundHandler<DnsQuery> {
    private final DnsProxyClient client;
    private final Disruptor<DnsMessage> disruptor;

    protected DnsProxyServerHandler(DnsProxyClient client, Disruptor<DnsMessage> disruptor) {
        this.client = client;
        this.disruptor = disruptor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DnsQuery query) throws Exception {
        // DnsQuery will be written and released once by DnsProxyClient instance,
        // so wo should call ReferenceCounted#retain().
        query.retain();
        final DnsResponse response = proxy(query);
        try {
            // write dns response
            ctx.writeAndFlush(response);
        } finally {
            this.disruptor.getRingBuffer().tryPublishEvent(DnsMessageTranslator.INSTANCE, query, response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private DnsResponse proxy(DnsQuery query) throws Exception {
        // TODO filter 127.0.0.1:53
        List<InetSocketAddress> dnsServerAddressList = DnsServerAddressUtil.listRawDnsServerAddress();
        return client.lookup(dnsServerAddressList.get(0), query);
    }
}
