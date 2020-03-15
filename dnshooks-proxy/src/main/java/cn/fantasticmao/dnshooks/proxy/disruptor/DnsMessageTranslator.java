package cn.fantasticmao.dnshooks.proxy.disruptor;

import com.lmax.disruptor.EventTranslatorVararg;
import io.netty.handler.codec.dns.DnsResponse;

import java.net.SocketAddress;

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
        final DnsResponse content = (DnsResponse) args[0];
        final SocketAddress sender = (SocketAddress) args[1];
        final SocketAddress recipient = (SocketAddress) args[2];

        event.setContent(content);
        event.setSender(sender);
        event.setRecipient(recipient);
    }
}
