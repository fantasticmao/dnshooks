package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.netty.handler.codec.DnsMessageTriplet;
import io.netty.handler.codec.dns.DnsQuery;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;

/**
 * DnsProxyClient
 *
 * @author maomao
 * @since 2020-03-12
 */
public abstract class DnsProxyClient implements AutoCloseable {

    @Nonnull
    public abstract DnsMessageTriplet lookup(@Nonnull final InetSocketAddress nameServer,
                                             @Nonnull final DnsQuery query) throws Exception;

}
