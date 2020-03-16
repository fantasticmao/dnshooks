package cn.fantasticmao.dnshooks.proxy.disruptor;

import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

/**
 * DnsMessage
 *
 * @author maomao
 * @since 2020-03-12
 */
public class DnsMessage {
    private DnsQuery query;
    private DnsResponse response;

    public DnsMessage() {
    }

    public DnsQuery getQuery() {
        return query;
    }

    public void setQuery(DnsQuery query) {
        this.query = query;
    }

    public DnsResponse getResponse() {
        return response;
    }

    public void setResponse(DnsResponse response) {
        this.response = response;
    }
}
