package cn.fantasticmao.dnshooks.proxy.util;

import java.net.*;
import java.util.Enumeration;

/**
 * NetworkInterfaceUtil
 *
 * @author maomao
 * @since 2020/3/14
 */
public interface NetworkInterfaceUtil {

    static String getMacAddress(NetworkInterface networkInterface) throws SocketException {
        byte[] macBytes = networkInterface.getHardwareAddress();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < macBytes.length; i++) {
            sb.append(String.format("%02X%s", macBytes[i], (i < macBytes.length - 1) ? ":" : ""));
        }
        return sb.toString();
    }

    static Inet4Address getAvailableInet4Address(NetworkInterface networkInterface) {
        Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses();
        while (inetAddressEnumeration.hasMoreElements()) {
            InetAddress inetAddress = inetAddressEnumeration.nextElement();
            if (inetAddress instanceof Inet4Address) {
                return (Inet4Address) inetAddress;
            }
        }
        throw new IllegalArgumentException("no IP address is available");
    }

    static Inet6Address getAvailableInet6Address(NetworkInterface networkInterface) {
        Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses();
        while (inetAddressEnumeration.hasMoreElements()) {
            InetAddress inetAddress = inetAddressEnumeration.nextElement();
            if (inetAddress instanceof Inet6Address) {
                return (Inet6Address) inetAddress;
            }
        }
        throw new IllegalArgumentException("no IP address is available");
    }
}
