package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.dns.*;

/**
 * DnsProxyServerDatagramHandler
 *
 * @author maomao
 * @since 2020/3/16
 */
public class DnsProxyServerDatagramHandler extends DnsProxyServerAbstractHandler<DatagramDnsQuery> {

    public DnsProxyServerDatagramHandler(Disruptor<DnsMessage> disruptor) {
        super(disruptor);
    }

    @Override
    protected DnsResponse proxy(DatagramDnsQuery query) {
        DatagramDnsResponse response = new DatagramDnsResponse(query.recipient(), query.sender(), query.id());

        DnsQuestion question = query.recordAt(DnsSection.QUESTION);
        response.addRecord(DnsSection.QUESTION, question);

        byte[] ip = new byte[]{(byte) 192, (byte) 168, 1, 1};
        ByteBuf ipByteBuf = Unpooled.wrappedBuffer(ip);
        DnsRawRecord dnsRecord = new DefaultDnsRawRecord(question.name(), DnsRecordType.A, 120, ipByteBuf);
        response.addRecord(DnsSection.ANSWER, dnsRecord);

        return response;
    }
}
