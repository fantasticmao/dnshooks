package cn.fantasticmao.dnshooks.proxy;

import com.lmax.disruptor.EventHandler;

/**
 * DnsRecordMessageHandler
 *
 * @author maomao
 * @since 2020-03-12
 */
class DnsRecordMessageHandler implements EventHandler<DnsRecordMessage> {

    @Override
    public void onEvent(DnsRecordMessage event, long sequence, boolean endOfBatch) throws Exception {
        // TODO handle dns hook
        System.out.println(event.getName());
    }
}
