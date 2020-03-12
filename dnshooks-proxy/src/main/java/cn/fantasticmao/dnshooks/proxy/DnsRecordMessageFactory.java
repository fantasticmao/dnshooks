package cn.fantasticmao.dnshooks.proxy;

import com.lmax.disruptor.EventFactory;

/**
 * DnsRecordMessageFactory
 *
 * @author maomao
 * @since 2020-03-12
 */
enum DnsRecordMessageFactory implements EventFactory<DnsRecordMessage> {
    INSTANCE;

    @Override
    public DnsRecordMessage newInstance() {
        return new DnsRecordMessage();
    }
}