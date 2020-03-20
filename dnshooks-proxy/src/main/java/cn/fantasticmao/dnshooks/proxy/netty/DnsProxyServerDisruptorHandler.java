package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageTranslator;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.channel.*;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.util.ReferenceCountUtil;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.net.InetSocketAddress;

/**
 * DnsProxyServerDisruptorHandler
 *
 * @author maomao
 * @since 2020-03-17
 */
@Immutable
@ChannelHandler.Sharable
class DnsProxyServerDisruptorHandler extends ChannelOutboundHandlerAdapter {
    private final Disruptor<DnsMessage> disruptor;

    DnsProxyServerDisruptorHandler(@Nonnull Disruptor<DnsMessage> disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object responseAfter, ChannelPromise promise) throws Exception {
        try {
            // obtain query before DNSHooks proxy
            final AddressedEnvelope<? extends DnsQuery, InetSocketAddress> queryBefore
                = ctx.channel().attr(AttributeKeyConstant.QUERY_BEFORE).get();

            // obtain query after DNSHooks proxy
            final AddressedEnvelope<? extends DnsQuery, InetSocketAddress> queryAfter
                = ctx.channel().attr(AttributeKeyConstant.QUERY_AFTER).get();

            // obtain response before DNSHooks proxy
            final AddressedEnvelope<? extends DnsResponse, InetSocketAddress> responseBefore
                = ctx.channel().attr(AttributeKeyConstant.RESPONSE_BEFORE).get();

            try {
                this.disruptor.getRingBuffer().tryPublishEvent(DnsMessageTranslator.INSTANCE,
                    queryBefore, queryAfter, responseBefore, responseAfter);
            } finally {
                ReferenceCountUtil.release(queryBefore);
                // queryAfter doesn't need to release
                //ReferenceCountUtil.release(queryAfter);
                ReferenceCountUtil.release(responseBefore);
            }
        } finally {
            ctx.writeAndFlush(responseAfter, promise);
        }
    }
}
