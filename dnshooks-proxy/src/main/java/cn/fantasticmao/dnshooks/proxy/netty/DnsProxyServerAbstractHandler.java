package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageTranslator;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

/**
 * DnsProxyServerAbstractHandler
 *
 * @author maomao
 * @since 2020/3/16
 */
public abstract class DnsProxyServerAbstractHandler<T extends DnsQuery> extends SimpleChannelInboundHandler<T> {
    private final Disruptor<DnsMessage> disruptor;

    public DnsProxyServerAbstractHandler(Disruptor<DnsMessage> disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T query) throws Exception {
        final DnsResponse response = proxy(query);
        try {
            this.disruptor.getRingBuffer().tryPublishEvent(DnsMessageTranslator.INSTANCE, query, response);
        } finally {
            // write dns response
            ctx.writeAndFlush(response);
        }
    }

    protected abstract DnsResponse proxy(T query);
}
