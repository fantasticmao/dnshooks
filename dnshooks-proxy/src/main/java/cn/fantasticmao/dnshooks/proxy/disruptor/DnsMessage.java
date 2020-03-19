package cn.fantasticmao.dnshooks.proxy.disruptor;

import io.netty.channel.AddressedEnvelope;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

import javax.annotation.concurrent.NotThreadSafe;
import java.net.InetSocketAddress;

/**
 * DnsMessage
 *
 * @author maomao
 * @since 2020-03-12
 */
@NotThreadSafe
public class DnsMessage<Query extends DnsQuery, Response extends DnsResponse> {
    /**
     * {@link DnsQuery} before DNSHooks Proxy
     */
    private AddressedEnvelope<Query, InetSocketAddress> queryBefore;

    /**
     * {@link DnsQuery} after DNSHooks Proxy
     */
    private AddressedEnvelope<Query, InetSocketAddress> queryAfter;

    /**
     * {@link DnsResponse} before DNSHooks Proxy
     */
    private AddressedEnvelope<Response, InetSocketAddress> responseBefore;

    /**
     * {@link DnsResponse} after DNSHooks Proxy
     */
    private AddressedEnvelope<Response, InetSocketAddress> responseAfter;

    public DnsMessage() {
    }

    public AddressedEnvelope<Query, InetSocketAddress> getQueryBefore() {
        return queryBefore;
    }

    public void setQueryBefore(AddressedEnvelope<Query, InetSocketAddress> queryBefore) {
        this.queryBefore = queryBefore;
    }

    public AddressedEnvelope<Query, InetSocketAddress> getQueryAfter() {
        return queryAfter;
    }

    public void setQueryAfter(AddressedEnvelope<Query, InetSocketAddress> queryAfter) {
        this.queryAfter = queryAfter;
    }

    public AddressedEnvelope<Response, InetSocketAddress> getResponseBefore() {
        return responseBefore;
    }

    public void setResponseBefore(AddressedEnvelope<Response, InetSocketAddress> responseBefore) {
        this.responseBefore = responseBefore;
    }

    public AddressedEnvelope<Response, InetSocketAddress> getResponseAfter() {
        return responseAfter;
    }

    public void setResponseAfter(AddressedEnvelope<Response, InetSocketAddress> responseAfter) {
        this.responseAfter = responseAfter;
    }
}
