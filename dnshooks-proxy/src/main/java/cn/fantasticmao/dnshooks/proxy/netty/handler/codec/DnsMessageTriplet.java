package cn.fantasticmao.dnshooks.proxy.netty.handler.codec;

import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * DnsMessageTriplet
 *
 * @author maomao
 * @since 2020-03-25
 */
@Getter
@Immutable
public final class DnsMessageTriplet {
    private final DnsQuery queryAfter;
    private final DnsResponse responseBefore;
    private final DnsResponse responseAfter;

    public DnsMessageTriplet(@Nullable DnsQuery queryAfter, @Nullable DnsResponse responseBefore,
                             @Nonnull DnsResponse responseAfter) {
        this.queryAfter = queryAfter;
        this.responseBefore = responseBefore;
        this.responseAfter = responseAfter;
    }
}
