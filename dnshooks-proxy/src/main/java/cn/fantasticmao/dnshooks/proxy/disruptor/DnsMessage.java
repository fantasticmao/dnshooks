package cn.fantasticmao.dnshooks.proxy.disruptor;

import io.netty.channel.AddressedEnvelope;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

import javax.annotation.concurrent.NotThreadSafe;
import java.net.InetSocketAddress;

/**
 * represent the messages in DNSHooks Proxy's workflow, will contains the raw {@link DnsQuery} and
 * {@link DnsResponse} DNS messages.
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
@NotThreadSafe
public class DnsMessage {
    /**
     * {@link DnsQuery} before DNSHooks Proxy
     */
    private AddressedEnvelope<DnsQuery, InetSocketAddress> queryBefore;

    /**
     * {@link DnsQuery} after DNSHooks Proxy
     */
    private AddressedEnvelope<DnsQuery, InetSocketAddress> queryAfter;

    /**
     * {@link DnsResponse} before DNSHooks Proxy
     */
    private AddressedEnvelope<DnsResponse, InetSocketAddress> responseBefore;

    /**
     * {@link DnsResponse} after DNSHooks Proxy
     */
    private AddressedEnvelope<DnsResponse, InetSocketAddress> responseAfter;

    public DnsMessage() {
    }

    public AddressedEnvelope<DnsQuery, InetSocketAddress> getQueryBefore() {
        return queryBefore;
    }

    public void setQueryBefore(AddressedEnvelope<DnsQuery, InetSocketAddress> queryBefore) {
        this.queryBefore = queryBefore;
    }

    public AddressedEnvelope<DnsQuery, InetSocketAddress> getQueryAfter() {
        return queryAfter;
    }

    public void setQueryAfter(AddressedEnvelope<DnsQuery, InetSocketAddress> queryAfter) {
        this.queryAfter = queryAfter;
    }

    public AddressedEnvelope<DnsResponse, InetSocketAddress> getResponseBefore() {
        return responseBefore;
    }

    public void setResponseBefore(AddressedEnvelope<DnsResponse, InetSocketAddress> responseBefore) {
        this.responseBefore = responseBefore;
    }

    public AddressedEnvelope<DnsResponse, InetSocketAddress> getResponseAfter() {
        return responseAfter;
    }

    public void setResponseAfter(AddressedEnvelope<DnsResponse, InetSocketAddress> responseAfter) {
        this.responseAfter = responseAfter;
    }
}
