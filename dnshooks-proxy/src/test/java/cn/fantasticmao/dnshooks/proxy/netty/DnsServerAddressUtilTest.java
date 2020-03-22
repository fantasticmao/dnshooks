package cn.fantasticmao.dnshooks.proxy.netty;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * DnsServerAddressUtilTest
 *
 * @author maomao
 * @since 2020-03-16
 */
@Slf4j
public class DnsServerAddressUtilTest {

    @Test
    public void listRawDnsServerAddress() {
        List<InetSocketAddress> dnsServerAddressList = DnsServerAddressUtil.listRawDnsServerAddress();
        dnsServerAddressList.stream()
            .map(InetSocketAddress::toString)
            .forEach(log::info);
    }
}