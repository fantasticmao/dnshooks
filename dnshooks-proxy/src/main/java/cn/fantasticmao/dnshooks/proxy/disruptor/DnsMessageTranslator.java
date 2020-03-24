package cn.fantasticmao.dnshooks.proxy.disruptor;

import com.lmax.disruptor.EventTranslatorVararg;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * DnsMessageTranslator
 *
 * @author maomao
 * @since 2020-03-12
 */
@Slf4j
public enum DnsMessageTranslator implements EventTranslatorVararg<DnsMessage> {
    INSTANCE;

    @Override
    public void translateTo(DnsMessage event, long sequence, Object... args) {
        final DnsQuery queryBefore = (DnsQuery) args[0];
        final DnsQuery queryAfter = (DnsQuery) args[1];
        final DnsResponse responseBefore = (DnsResponse) args[2];
        final DnsResponse responseAfter = (DnsResponse) args[3];

        event.setQueryBefore(queryBefore);
        event.setQueryAfter(queryAfter);
        event.setResponseBefore(responseBefore);
        event.setResponseAfter(responseAfter);
        log.trace("translate Disruptor/Ring Buffer event to: {}", event);
    }
}
