package cn.fantasticmao.dnshooks.proxy.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.NetworkInterface;
import java.net.SocketException;

/**
 * NetworkInterfaceUtilTest
 *
 * @author maomao
 * @since 2020/3/14
 */
public class NetworkInterfaceUtilTest {
    private NetworkInterface networkInterface;

    public NetworkInterfaceUtilTest() throws SocketException {
        this.networkInterface = NetworkInterface.getByName("en0");
    }

    @Test
    public void getMacAddress() throws SocketException {
        String macAddress = NetworkInterfaceUtil.getMacAddress(networkInterface);
        Assert.assertNotNull(macAddress);
        System.out.println("getMacAddress: " + macAddress);
    }

    @Test
    public void getAvailableInet4Address() {
        Inet4Address ip4 = NetworkInterfaceUtil.getAvailableInet4Address(networkInterface);
        Assert.assertNotNull(ip4);
        System.out.println("getAvailableInet4Address: " + ip4);
    }

    @Test
    public void getAvailableInet6Address() {
        Inet6Address ip6 = NetworkInterfaceUtil.getAvailableInet6Address(networkInterface);
        Assert.assertNotNull(ip6);
        System.out.println("getAvailableInet6Address: " + ip6);
    }
}