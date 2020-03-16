package cn.fantasticmao.dnshooks.proxy.disruptor;

import com.lmax.disruptor.EventTranslatorVararg;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

/**
 * DnsMessageTranslator
 *
 * @author maomao
 * @since 2020-03-12
 */
public enum DnsMessageTranslator implements EventTranslatorVararg<DnsMessage> {
    INSTANCE;

    @Override
    public void translateTo(DnsMessage event, long sequence, Object... args) {
        final DnsQuery query = (DnsQuery) args[0];
        final DnsResponse response = (DnsResponse) args[1];

        event.setQuery(query);
        event.setResponse(response);
    }
}
