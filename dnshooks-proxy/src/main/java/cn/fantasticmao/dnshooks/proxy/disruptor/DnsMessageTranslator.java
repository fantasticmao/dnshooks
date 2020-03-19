package cn.fantasticmao.dnshooks.proxy.disruptor;

import com.lmax.disruptor.EventTranslatorVararg;
import io.netty.channel.AddressedEnvelope;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

import java.net.InetSocketAddress;

/**
 * DnsMessageTranslator
 *
 * @author maomao
 * @since 2020-03-12
 */
public enum DnsMessageTranslator implements EventTranslatorVararg<DnsMessage<DnsQuery, DnsResponse>> {
    INSTANCE;

    @Override
    public void translateTo(DnsMessage<DnsQuery, DnsResponse> event, long sequence, Object... args) {
        @SuppressWarnings("unchecked") final AddressedEnvelope<DnsQuery, InetSocketAddress> queryBefore
            = (AddressedEnvelope<DnsQuery, InetSocketAddress>) args[0];
        @SuppressWarnings("unchecked") final AddressedEnvelope<DnsQuery, InetSocketAddress> queryAfter
            = (AddressedEnvelope<DnsQuery, InetSocketAddress>) args[1];
        @SuppressWarnings("unchecked") final AddressedEnvelope<DnsResponse, InetSocketAddress> responseBefore
            = (AddressedEnvelope<DnsResponse, InetSocketAddress>) args[2];
        @SuppressWarnings("unchecked") final AddressedEnvelope<DnsResponse, InetSocketAddress> responseAfter
            = (AddressedEnvelope<DnsResponse, InetSocketAddress>) args[3];

        event.setQueryBefore(queryBefore);
        event.setQueryAfter(queryAfter);
        event.setResponseBefore(responseBefore);
        event.setResponseAfter(responseAfter);
    }
}
