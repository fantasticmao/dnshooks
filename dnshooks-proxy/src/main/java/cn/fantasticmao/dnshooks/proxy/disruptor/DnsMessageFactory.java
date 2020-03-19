package cn.fantasticmao.dnshooks.proxy.disruptor;

import com.lmax.disruptor.EventFactory;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

/**
 * DnsMessageFactory
 *
 * @author maomao
 * @since 2020-03-12
 */
public enum DnsMessageFactory implements EventFactory<DnsMessage<DnsQuery, DnsResponse>> {
    INSTANCE;

    @Override
    public DnsMessage<DnsQuery, DnsResponse> newInstance() {
        return new DnsMessage<>();
    }
}