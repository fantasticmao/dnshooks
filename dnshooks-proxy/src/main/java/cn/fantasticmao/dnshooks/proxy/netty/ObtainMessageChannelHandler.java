package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import javax.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ObtainMessageChannelHandler
 *
 * @author maomao
 * @since 2020-03-15
 */
class ObtainMessageChannelHandler<T> extends SimpleChannelInboundHandler<T> {
    private final BlockingQueue<T> answer;

    ObtainMessageChannelHandler(@Nonnull Class<? extends T> inboundMessageType) {
        super(inboundMessageType, false);
        // Notice: LinkedBlockingQueue may cause OutOfMemoryError
        this.answer = new LinkedBlockingQueue<>();
    }

    public T getMessage() {
        boolean interrupted = false;
        try {
            for (; ; ) {
                try {
                    return this.answer.take();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        this.answer.offer(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
