package cn.fantasticmao.dnshooks.proxy.netty;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * DnsServerAddressUtilTest
 *
 * @author maomao
 * @since 2020-03-16
 */
public class DnsServerAddressUtilTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DnsServerAddressUtilTest.class);

    @Test
    public void listRawDnsServerAddress() {
        List<InetSocketAddress> dnsServerAddressList = DnsServerAddressUtil.listRawDnsServerAddress();
        dnsServerAddressList.stream()
            .map(InetSocketAddress::toString)
            .forEach(LOGGER::info);
    }
}