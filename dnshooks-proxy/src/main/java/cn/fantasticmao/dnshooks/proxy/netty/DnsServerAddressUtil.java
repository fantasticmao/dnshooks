package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.resolver.dns.DnsServerAddressStream;
import io.netty.resolver.dns.DnsServerAddressStreamProvider;
import io.netty.resolver.dns.DnsServerAddressStreamProviders;
import io.netty.util.internal.SocketUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

/**
 * DnsServerAddressUtil
 *
 * @author maomao
 * @since 2020/3/16
 */
class DnsServerAddressUtil {
    private static DnsServerAddressStreamProvider provider = DnsServerAddressStreamProviders.platformDefault();
    private static InetAddress loopbackAddress = SocketUtils.loopbackAddress();

    static List<InetSocketAddress> listRawDnsServerAddress() {
        List<InetSocketAddress> list = new LinkedList<>();
        DnsServerAddressStream serverAddressStream = provider.nameServerAddressStream("");
        for (int i = 0; i < serverAddressStream.size(); i++) {
            InetSocketAddress inetSocketAddress = serverAddressStream.next();
            if (!loopbackAddress.equals(inetSocketAddress.getAddress())) {
                list.add(inetSocketAddress);
            }
        }
        return list;
    }
}
