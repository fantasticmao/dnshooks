package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.netty.handler.codec.DnsMessageTriplet;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;

/**
 * DnsProxyDatagramClientTest
 *
 * @author maomao
 * @since 2020-03-14
 */
@Slf4j
public class DnsProxyDatagramClientTest extends DnsProtocolTest {

    @Test
    public void lookup() throws Exception {
        final DatagramDnsQuery dnsQuery = super.newUdpQuery(new InetSocketAddress(0), super.dnsServerAddress);
        try (DnsProxyDatagramClient client = new DnsProxyDatagramClient(super.proxyServerAddress)) {
            final DnsMessageTriplet triplet = client.lookup(super.dnsServerAddress, dnsQuery);
            Assert.assertNotNull(triplet);
            log.info("DnsQuery After :" + triplet.getQueryAfter());
            log.info("DnsResponse Before: " + triplet.getResponseBefore());
            log.info("DnsResponse After: " + triplet.getResponseAfter());
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }
}