package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.util.Constant;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * ObtainMessageChannelHandler
 *
 * @author maomao
 * @since 2020-03-15
 */
@Slf4j
class ObtainMessageChannelHandler<T> extends SimpleChannelInboundHandler<T> {
    private final BlockingQueue<T> answer;

    ObtainMessageChannelHandler(@Nonnull Class<? extends T> inboundMessageType) {
        super(inboundMessageType, false);
        // Notice: LinkedBlockingQueue may cause OutOfMemoryError
        this.answer = new LinkedBlockingQueue<>();
    }

    public T getMessage() throws InterruptedException {
        return this.answer.poll(Constant.LOOKUP_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        this.answer.offer(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("caught an exception", cause);
        ctx.close();
    }
}
