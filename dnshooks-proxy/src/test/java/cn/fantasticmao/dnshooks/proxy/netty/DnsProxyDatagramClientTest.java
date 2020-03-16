package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.handler.codec.dns.*;
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
public class DnsProxyDatagramClientTest {
    private Random idGenerator = new Random();

    @Test
    public void lookup() {
        List<InetSocketAddress> dnsServerAddressList = DnsServerAddressUtil.listRawDnsServerAddress();
        if (dnsServerAddressList.size() == 0) {
            return;
        }

        final InetSocketAddress dnsServerAddress = dnsServerAddressList.get(0);
        final int id = idGenerator.nextInt(Short.MAX_VALUE);
        System.out.println("Transaction ID: " + id);

        final DatagramDnsQuery dnsQuery = new DatagramDnsQuery(null, dnsServerAddress, id);
        dnsQuery.addRecord(DnsSection.QUESTION, new DefaultDnsQuestion("fantasticmao.cn", DnsRecordType.A));

        try (DnsProxyDatagramClient client = new DnsProxyDatagramClient()) {
            DnsResponse response = client.lookup(dnsServerAddress, dnsQuery);
            Assert.assertNotNull(response);
            System.out.println(response.toString());
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }
}