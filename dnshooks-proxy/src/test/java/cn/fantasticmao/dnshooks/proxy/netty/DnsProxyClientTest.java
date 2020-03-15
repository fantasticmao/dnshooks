package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.handler.codec.dns.*;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Random;

/**
 * DnsProxyClientTest
 *
 * @author maomao
 * @since 2020/3/14
 */
public class DnsProxyClientTest {
    private Random idGenerator = new Random();

    @Test
    public void lookup() {
        final int id = idGenerator.nextInt(Short.MAX_VALUE);
        System.out.println(id);
        InetSocketAddress recipient = new InetSocketAddress("192.168.1.1", 53);
        final DnsQuery dnsQuery = new DatagramDnsQuery(null, recipient, id);
        dnsQuery.addRecord(DnsSection.QUESTION, new DefaultDnsQuestion("fantasticmao.cn", DnsRecordType.A));

        try (DnsProxyClient client = new DnsProxyClient()) {
            DnsResponse response = client.lookup(dnsQuery);
            Assert.assertNotNull(response);
            System.out.println(response.toString());
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }
}