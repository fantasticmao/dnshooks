package cn.fantasticmao.dnshooks.proxy.hook;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;

/**
 * DnsMessageHookNoOp
 *
 * @author maomao
 * @since 2020-03-15
 */
public class DnsMessageHookNoOp implements DnsMessageHook {

    @Override
    public String name() {
        return "No Operation Hook";
    }

    @Override
    public void onEvent(DnsMessage event, long sequence, boolean endOfBatch) throws Exception {
        // do nothing
    }
}
