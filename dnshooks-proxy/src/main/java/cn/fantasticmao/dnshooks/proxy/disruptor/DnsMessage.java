package cn.fantasticmao.dnshooks.proxy.disruptor;

import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Messages in DNSHooks Proxy's workflow, will contains the raw {@link DnsQuery} and {@link DnsResponse} DNS messages.
 * <p>
 * DNSHooks Proxy's workflow is as flow:
 * <pre>
 * +------------+                       +----------------+                        +------------+
 * |            | -->  queryBefore  --> |                | -->   queryAfter   --> |            |
 * | DNS Client |                       | DNSHooks Proxy |                        | DNS Server |
 * |            | <-- responseAfter <-- |                | <-- responseBefore <-- |            |
 * +------------+                       +----------------+                        +------------+
 * </pre>
 * </p>
 *
 * @author maomao
 * @since 2020-03-12
 */
@Setter
@ToString
@NotThreadSafe
public class DnsMessage {
    /**
     * {@link DnsQuery} before DNSHooks Proxy
     */
    private DnsQuery queryBefore;

    /**
     * {@link DnsQuery} after DNSHooks Proxy
     */
    private DnsQuery queryAfter;

    /**
     * {@link DnsResponse} before DNSHooks Proxy
     */
    private DnsResponse responseBefore;

    /**
     * {@link DnsResponse} after DNSHooks Proxy
     */
    private DnsResponse responseAfter;

    public DnsMessage() {
    }

    @Nonnull
    public DnsQuery getQueryBefore() {
        return queryBefore;
    }

    @Nullable
    public DnsQuery getQueryAfter() {
        return queryAfter;
    }

    @Nullable
    public DnsResponse getResponseBefore() {
        return responseBefore;
    }

    @Nonnull
    public DnsResponse getResponseAfter() {
        return responseAfter;
    }

}
