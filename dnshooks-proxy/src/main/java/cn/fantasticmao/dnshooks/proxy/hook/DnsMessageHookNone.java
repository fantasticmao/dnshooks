package cn.fantasticmao.dnshooks.proxy.hook;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;

/**
 * DnsMessageHookNone
 *
 * @author maomao
 * @since 2020/3/15
 */
public class DnsMessageHookNone implements DnsMessageHook {

    @Override
    public String name() {
        return "None";
    }

    @Override
    public void onEvent(DnsMessage event, long sequence, boolean endOfBatch) throws Exception {
        // do nothing
        System.out.println("query: " + event.getQuery());
        System.out.println("response: " + event.getResponse());
    }
}
