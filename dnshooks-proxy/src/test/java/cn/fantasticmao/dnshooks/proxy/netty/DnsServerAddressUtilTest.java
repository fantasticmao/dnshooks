package cn.fantasticmao.dnshooks.proxy.netty;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * DnsServerAddressUtilTest
 *
 * @author maomao
 * @since 2020/3/16
 */
public class DnsServerAddressUtilTest {

    @Test
    public void listRawDnsServerAddress() {
        List<InetSocketAddress> dnsServerAddressList = DnsServerAddressUtil.listRawDnsServerAddress();
        dnsServerAddressList.forEach(System.out::println);
    }
}