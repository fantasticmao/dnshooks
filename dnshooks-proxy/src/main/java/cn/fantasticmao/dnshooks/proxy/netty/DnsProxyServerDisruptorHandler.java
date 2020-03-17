package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageTranslator;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.util.AttributeKey;

/**
 * DnsProxyServerDisruptorHandler
 *
 * @author maomao
 * @since 2020-03-17
 */
@ChannelHandler.Sharable
public class DnsProxyServerDisruptorHandler extends ChannelOutboundHandlerAdapter {
    private final Disruptor<DnsMessage> disruptor;

    public DnsProxyServerDisruptorHandler(Disruptor<DnsMessage> disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object response, ChannelPromise promise) throws Exception {
        try {
            final AttributeKey<DnsQuery> key = AttributeKey.valueOf(DnsProxyServerClientHandler.QUERY_ATTRIBUTE_KEY);
            final DnsQuery query = ctx.channel().attr(key).get();
            this.disruptor.getRingBuffer().tryPublishEvent(DnsMessageTranslator.INSTANCE, query, response);
        } finally {
            ctx.writeAndFlush(response, promise);
        }
    }
}
