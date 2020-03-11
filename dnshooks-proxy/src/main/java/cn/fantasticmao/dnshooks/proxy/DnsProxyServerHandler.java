package cn.fantasticmao.dnshooks.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;

import java.util.HashMap;
import java.util.Map;

/**
 * DnsProxyServerHandler
 *
 * @author maomao
 * @since 2020-03-11
 */
public class DnsProxyServerHandler extends SimpleChannelInboundHandler<DatagramDnsQuery> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsQuery msg) throws Exception {
        // TODO handle dns hook
        System.out.println("handle dns hook");

        Map<String, byte[]> map = new HashMap<>();
        map.put("fantasticmao.cn.", new byte[]{(byte) 47, (byte) 98, (byte) 180, (byte) 53});
        DatagramDnsResponse response = new DatagramDnsResponse(msg.recipient(), msg.sender(), msg.id());
        try {
            DefaultDnsQuestion query = msg.recordAt(DnsSection.QUESTION);
            response.addRecord(DnsSection.QUESTION, query);

            String queryName = query.name();
            byte[] ip = map.getOrDefault(queryName, new byte[]{(byte) 192, (byte) 168, (byte) 0, (byte) 1});
            ByteBuf ipBuf = Unpooled.wrappedBuffer(ip);
            DefaultDnsRawRecord record = new DefaultDnsRawRecord("127.0.0.1", DnsRecordType.A, 120, ipBuf);
            response.addRecord(DnsSection.ANSWER, record);
        } finally {
            ctx.writeAndFlush(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
