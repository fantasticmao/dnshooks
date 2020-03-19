package cn.fantasticmao.dnshooks.proxy.hook;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

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
    public void onEvent(DnsMessage<DnsQuery, DnsResponse> event, long sequence, boolean endOfBatch) throws Exception {
        // do nothing
        System.out.printf("queryBefore from: %s to: %s%n", event.getQueryBefore().sender(), event.getQueryBefore().recipient());
        System.out.printf("responseAfter from: %s to: %s%n", event.getResponseAfter().sender(), event.getResponseAfter().recipient());
    }
}
