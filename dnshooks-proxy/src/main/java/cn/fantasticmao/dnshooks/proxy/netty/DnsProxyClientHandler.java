package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.DnsResponse;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * DnsProxyClientHandler
 *
 * @author maomao
 * @since 2020/3/15
 */
public class DnsProxyClientHandler extends SimpleChannelInboundHandler<DnsResponse> {
    private BlockingQueue<DnsResponse> answer;

    public DnsProxyClientHandler() {
        this.answer = new LinkedTransferQueue<>();
    }

    public DnsResponse getResponse() {
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
    public void channelRead0(ChannelHandlerContext ctx, DnsResponse msg) throws Exception {
        this.answer.offer(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
