package cn.fantasticmao.dnshooks.proxy.netty;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;

/**
 * InetAddressTest
 *
 * @author maomao
 * @since 2020-03-24
 */
public class InetAddressTest {

    @Test
    public void loopback() {
        InetAddress loopback = InetAddress.getLoopbackAddress();
        Assert.assertNotNull(loopback);
        System.out.println(loopback.getHostName());
        System.out.println(loopback.getHostAddress());
    }

    @Test
    public void localhost() throws Exception {
        InetAddress localHost = InetAddress.getLocalHost();
        Assert.assertNotNull(localHost);
        System.out.println(localHost.getHostName());
        System.out.println(localHost.getHostAddress());
    }
}
