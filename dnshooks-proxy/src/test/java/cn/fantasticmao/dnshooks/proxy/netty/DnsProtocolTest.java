package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.dns.*;
import io.netty.util.internal.SocketUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;

import java.net.InetSocketAddress;
import java.util.Random;

/**
 * DnsProtocolTest
 *
 * @author maomao
 * @since 2020-03-25
 */
@Slf4j
public abstract class DnsProtocolTest {
    protected final Random idGenerator;
    protected final InetSocketAddress dnsClientAddress;
    protected final InetSocketAddress dnsServerAddress;
    protected final InetSocketAddress proxyClientAddress;
    protected final InetSocketAddress proxyServerAddress;

    protected DnsProtocolTest() {
        this.idGenerator = new Random();
        this.dnsClientAddress = SocketUtils.socketAddress("192.168.1.66", 1234);
        this.dnsServerAddress = DnsServerAddressUtil.listRawDnsServerAddress().get(0);
        this.proxyClientAddress = SocketUtils.socketAddress("192.168.1.66", 5678);
        this.proxyServerAddress = SocketUtils.socketAddress("192.168.1.66", 53);
    }

    protected DatagramDnsQuery newUdpQuery(InetSocketAddress sender, InetSocketAddress recipient) {
        final int id = this.idGenerator.nextInt(Short.MAX_VALUE);
        log.trace("Transaction ID: {}", id);

        final DatagramDnsQuery dnsQuery = new DatagramDnsQuery(sender, recipient, id);
        final DnsRecord question = new DefaultDnsQuestion("fantasticmao.cn", DnsRecordType.A);
        dnsQuery.addRecord(DnsSection.QUESTION, question);
        log.trace("DatagramDnsQuery: {}", dnsQuery);
        return dnsQuery;
    }

    protected DatagramPacket newUdpPacket(InetSocketAddress sender, InetSocketAddress recipient) {
        final int id = this.idGenerator.nextInt(Short.MAX_VALUE);
        log.trace("Transaction ID: {}", id);

        final DatagramDnsResponse dnsResponse = new DatagramDnsResponse(sender, recipient, id,
            DnsOpCode.QUERY, DnsResponseCode.NOERROR);
        final DnsRecord question = new DefaultDnsQuestion("fantasticmao.cn", DnsRecordType.A);
        dnsResponse.addRecord(DnsSection.QUESTION, question);
        final ByteBuf ip = Unpooled.buffer(4).writeByte(47).writeByte(98).writeByte(180).writeByte(53);
        final DnsRecord answer = new DefaultDnsRawRecord("fantasticmao.cn", DnsRecordType.A, 575, ip);
        dnsResponse.addRecord(DnsSection.ANSWER, answer);
        log.trace("DatagramDnsResponse: {}", dnsResponse);

        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new DatagramDnsResponseEncoder());
        embeddedChannel.writeOutbound(dnsResponse);
        Assert.assertTrue(embeddedChannel.finish());
        return embeddedChannel.readOutbound();
    }
}
