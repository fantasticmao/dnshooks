package cn.fantasticmao.dnshooks.proxy.disruptor;

import com.lmax.disruptor.EventTranslatorVararg;
import io.netty.channel.AddressedEnvelope;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * DnsMessageTranslator
 *
 * @author maomao
 * @since 2020-03-12
 */
@Slf4j
public enum DnsMessageTranslator implements EventTranslatorVararg<DnsMessage> {
    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public void translateTo(DnsMessage event, long sequence, Object... args) {
        final AddressedEnvelope<DnsQuery, InetSocketAddress> queryBefore
            = (AddressedEnvelope<DnsQuery, InetSocketAddress>) args[0];
        final AddressedEnvelope<DnsQuery, InetSocketAddress> queryAfter
            = (AddressedEnvelope<DnsQuery, InetSocketAddress>) args[1];
        final AddressedEnvelope<DnsResponse, InetSocketAddress> responseBefore
            = (AddressedEnvelope<DnsResponse, InetSocketAddress>) args[2];
        final AddressedEnvelope<DnsResponse, InetSocketAddress> responseAfter
            = (AddressedEnvelope<DnsResponse, InetSocketAddress>) args[3];

        event.setQueryBefore(queryBefore);
        event.setQueryAfter(queryAfter);
        event.setResponseBefore(responseBefore);
        event.setResponseAfter(responseAfter);
        log.trace("translate Disruptor event to: {}", event);
    }
}
