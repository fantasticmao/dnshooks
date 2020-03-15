package cn.fantasticmao.dnshook.log2es.hook;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;

/**
 * DnsMessageHookHttp
 *
 * @author maomao
 * @since 2020-03-12
 */
public class DnsMessageHookHttp implements DnsMessageHook {

    @Override
    public String name() {
        return "HTTP Hook";
    }

    @Override
    public void onEvent(DnsMessage event, long sequence, boolean endOfBatch) throws Exception {
        System.out.println(event.getContent());
        System.out.println(event.getSender());
        System.out.println(event.getRecipient());
    }
}
