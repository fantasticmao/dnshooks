package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.netty.handler.codec.DnsMessageTriplet;
import io.netty.handler.codec.dns.DnsQuery;

import javax.annotation.Nonnull;

/**
 * DnsProxyClient
 *
 * @author maomao
 * @since 2020-03-12
 */
public abstract class DnsProxyClient implements AutoCloseable {

    @Nonnull
    public abstract DnsMessageTriplet lookup(@Nonnull final DnsQuery query) throws Exception;

}
