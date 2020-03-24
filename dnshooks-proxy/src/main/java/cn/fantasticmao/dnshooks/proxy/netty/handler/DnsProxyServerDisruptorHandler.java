package cn.fantasticmao.dnshooks.proxy.netty.handler;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageTranslator;
import cn.fantasticmao.dnshooks.proxy.netty.AttributeKeyConstant;
import cn.fantasticmao.dnshooks.proxy.netty.ErrorResponseConstant;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.channel.*;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * DnsProxyServerDisruptorHandler
 *
 * @author maomao
 * @since 2020-03-17
 */
@Slf4j
@Immutable
@ChannelHandler.Sharable
public class DnsProxyServerDisruptorHandler extends ChannelOutboundHandlerAdapter {
    private final Disruptor<DnsMessage> disruptor;

    public DnsProxyServerDisruptorHandler(@Nonnull Disruptor<DnsMessage> disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object responseAfter, ChannelPromise promise) throws Exception {
        try {
            final DnsQuery queryBefore = ctx.channel().attr(AttributeKeyConstant.QUERY_BEFORE).get();
            log.trace("obtain DnsQuery before DNSHooks-Proxy: {}", queryBefore);

            final DnsQuery queryAfter = ctx.channel().attr(AttributeKeyConstant.QUERY_AFTER).get();
            log.trace("obtain DnsQuery after DNSHooks-Proxy: {}", queryAfter);

            // obtain response before DNSHooks proxy
            final DnsResponse responseBefore = ctx.channel().attr(AttributeKeyConstant.RESPONSE_BEFORE).get();
            log.trace("obtain DnsResponse before DNSHooks-Proxy: {}", responseBefore);

            try {
                log.trace("publish event to Disruptor/Ring Buffer");
                boolean result = this.disruptor.getRingBuffer().tryPublishEvent(DnsMessageTranslator.INSTANCE,
                    queryBefore, queryAfter, responseBefore, responseAfter);
                if (result) {
                    log.trace("publish event to Disruptor/Ring Buffer success");
                } else {
                    log.warn("publish event to Disruptor Disruptor/Ring Buffer error");
                }
            } finally {
                ReferenceCountUtil.release(queryBefore);
                // queryAfter doesn't need to release
                //ReferenceCountUtil.release(queryAfter);
                ReferenceCountUtil.release(responseBefore);
            }
        } finally {
            log.trace("write and flush DNS Response: {}", responseAfter);
            ctx.writeAndFlush(responseAfter, promise).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        log.error("write and flush DnsResponse after DNSHooks-Proxy error", future.cause());
                        future.channel().writeAndFlush(ErrorResponseConstant.UDP.DEFAULT);
                    }
                }
            });
        }
    }
}
