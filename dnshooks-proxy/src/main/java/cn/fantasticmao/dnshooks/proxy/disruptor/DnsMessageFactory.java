package cn.fantasticmao.dnshooks.proxy.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * DnsMessageFactory
 *
 * @author maomao
 * @since 2020-03-12
 */
public enum DnsMessageFactory implements EventFactory<DnsMessage> {
    INSTANCE;

    @Override
    public DnsMessage newInstance() {
        return new DnsMessage();
    }
}