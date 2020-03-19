package cn.fantasticmao.dnshooks.proxy.hook;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;

/**
 * DnsMessageHookNone
 *
 * @author maomao
 * @since 2020-03-15
 */
public class DnsMessageHookNone implements DnsMessageHook {

    @Override
    public String name() {
        return "None";
    }

    @Override
    public void onEvent(DnsMessage event, long sequence, boolean endOfBatch) throws Exception {
        // do nothing
        //System.out.printf("queryBefore from: %s to: %s%n", event.getQueryBefore().sender(), event.getQueryBefore().recipient());
        //System.out.printf("queryAfter from: %s to: %s%n", event.getQueryAfter().sender(), event.getQueryAfter().recipient());
        //System.out.printf("responseBefore from: %s to: %s%n", event.getResponseBefore().sender(), event.getResponseBefore().recipient());
        //System.out.printf("responseAfter from: %s to: %s%n", event.getResponseAfter().sender(), event.getResponseAfter().recipient());
    }
}
