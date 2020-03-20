package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DefaultDnsQuestion;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.handler.codec.dns.DnsSection;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;

/**
 * DnsProxyDatagramClientTest
 *
 * @author maomao
 * @since 2020-03-14
 */
public class DnsProxyDatagramClientTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DnsProxyDatagramClientTest.class);
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
        LOGGER.info("Transaction ID: " + id);

        final DatagramDnsQuery dnsQuery = new DatagramDnsQuery(null, dnsServerAddress, id);
        dnsQuery.addRecord(DnsSection.QUESTION, new DefaultDnsQuestion("fantasticmao.cn", DnsRecordType.A));

        try (DnsProxyDatagramClient client = new DnsProxyDatagramClient(localAddress)) {
            final DnsProxyClient.Triplet triplet = client.lookup(dnsServerAddress, dnsQuery);
            Assert.assertNotNull(triplet);
            LOGGER.info("DnsQuery After :" + triplet.queryAfter);
            LOGGER.info("DnsResponse Before: " + triplet.responseBefore);
            LOGGER.info("DnsResponse After: " + triplet.responseAfter);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }
}