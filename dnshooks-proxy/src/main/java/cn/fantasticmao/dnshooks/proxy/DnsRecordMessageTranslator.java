package cn.fantasticmao.dnshooks.proxy;

import com.lmax.disruptor.EventTranslatorVararg;

/**
 * DnsRecordMessageTranslator
 *
 * @author maomao
 * @since 2020-03-12
 */
enum DnsRecordMessageTranslator implements EventTranslatorVararg<DnsRecordMessage> {
    INSTANCE;

    @Override
    public void translateTo(DnsRecordMessage event, long sequence, Object... args) {
        String name = (String) args[0];
        event.setName(name);
    }
}
