package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageTranslator;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * DnsProxyServerDisruptorHandler
 *
 * @author maomao
 * @since 2020-03-17
 */
@Immutable
@ChannelHandler.Sharable
class DnsProxyServerDisruptorHandler extends ChannelOutboundHandlerAdapter {
    private final Disruptor<DnsMessage<DnsQuery, DnsResponse>> disruptor;

    public DnsProxyServerDisruptorHandler(@Nonnull Disruptor<DnsMessage<DnsQuery, DnsResponse>> disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object responseAfter, ChannelPromise promise) throws Exception {
        try {
            // obtain query before DNSHooks proxy
            final DnsQuery queryBefore = ctx.channel().attr(AttributeKeyConstant.QUERY_BEFORE).get();

            this.disruptor.getRingBuffer().tryPublishEvent(DnsMessageTranslator.INSTANCE,
                queryBefore, null, null, responseAfter);
        } finally {
            ctx.writeAndFlush(responseAfter, promise);
        }
    }
}
