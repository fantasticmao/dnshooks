package cn.fantasticmao.dnshooks.proxy;

import com.lmax.disruptor.dsl.Disruptor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;

import java.util.HashMap;
import java.util.Map;

/**
 * DnsProxyHandler
 *
 * @author maomao
 * @since 2020-03-11
 */
class DnsProxyHandler extends SimpleChannelInboundHandler<DatagramDnsQuery> {
    private final Disruptor<DnsRecordMessage> disruptor;

    DnsProxyHandler(Disruptor<DnsRecordMessage> disruptor) {
        this.disruptor = disruptor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsQuery msg) throws Exception {
        // TODO dns proxy
        Map<String, byte[]> map = new HashMap<>();
        map.put("fantasticmao.cn.", new byte[]{(byte) 47, (byte) 98, (byte) 180, (byte) 53});

        DatagramDnsResponse response = new DatagramDnsResponse(msg.recipient(), msg.sender(), msg.id());
        DefaultDnsQuestion query = msg.recordAt(DnsSection.QUESTION);
        response.addRecord(DnsSection.QUESTION, query);

        byte[] ip = map.getOrDefault(query.name(), new byte[]{(byte) 192, (byte) 168, (byte) 0, (byte) 1});
        ByteBuf ipBuf = Unpooled.wrappedBuffer(ip);
        DefaultDnsRawRecord record = new DefaultDnsRawRecord("127.0.0.1", DnsRecordType.A, 120, ipBuf);
        response.addRecord(DnsSection.ANSWER, record);

        try {
            disruptor.getRingBuffer().tryPublishEvent(DnsRecordMessageTranslator.INSTANCE, query.name());
        } finally {
            ctx.writeAndFlush(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
