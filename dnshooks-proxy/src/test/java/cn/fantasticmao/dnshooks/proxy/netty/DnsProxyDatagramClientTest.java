package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.netty.handler.codec.DnsMessageTriplet;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DefaultDnsQuestion;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.handler.codec.dns.DnsSection;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;

/**
 * DnsProxyDatagramClientTest
 *
 * @author maomao
 * @since 2020-03-14
 */
@Slf4j
public class DnsProxyDatagramClientTest {
    private Random idGenerator = new Random();

    @Test
    public void lookup() throws Exception {
        List<InetSocketAddress> dnsServerAddressList = DnsServerAddressUtil.listRawDnsServerAddress();
        if (dnsServerAddressList.size() == 0) {
            return;
        }

        final InetSocketAddress localAddress = new InetSocketAddress(53);
        final InetSocketAddress dnsServerAddress = dnsServerAddressList.get(0);
        final int id = idGenerator.nextInt(Short.MAX_VALUE);
        log.info("Transaction ID: " + id);

        final DatagramDnsQuery dnsQuery = new DatagramDnsQuery(null, dnsServerAddress, id);
        dnsQuery.addRecord(DnsSection.QUESTION, new DefaultDnsQuestion("fantasticmao.cn", DnsRecordType.A));

        try (DnsProxyDatagramClient client = new DnsProxyDatagramClient(localAddress)) {
            final DnsMessageTriplet triplet = client.lookup(dnsServerAddress, dnsQuery);
            Assert.assertNotNull(triplet);
            log.info("DnsQuery After :" + triplet.getQueryAfter());
            log.info("DnsResponse Before: " + triplet.getResponseBefore());
            log.info("DnsResponse After: " + triplet.getResponseAfter());
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }
}