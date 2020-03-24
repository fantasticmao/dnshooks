package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.handler.codec.dns.DatagramDnsResponse;

/**
 * ErrorResponseConstant
 *
 * @author maomao
 * @since 2020-03-21
 */
public interface ErrorResponseConstant {
    interface UDP {
        DatagramDnsResponse DEFAULT = null;
        DatagramDnsResponse TIMEOUT = null;
    }
}
