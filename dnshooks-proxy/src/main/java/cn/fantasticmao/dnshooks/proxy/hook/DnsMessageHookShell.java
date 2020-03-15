package cn.fantasticmao.dnshooks.proxy.hook;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;

/**
 * DnsMessageHookShell
 *
 * @author maomao
 * @since 2020/3/15
 */
public class DnsMessageHookShell implements DnsMessageHook {

    @Override
    public String name() {
        return "Shell Hook";
    }

    @Override
    public void onEvent(DnsMessage event, long sequence, boolean endOfBatch) throws Exception {
        // TODO implement default Shell hook
        throw new UnsupportedOperationException("TODO implement default Shell hook");
    }
}
