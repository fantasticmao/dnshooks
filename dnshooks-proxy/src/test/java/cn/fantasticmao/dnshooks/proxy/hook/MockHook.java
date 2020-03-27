package cn.fantasticmao.dnshooks.proxy.hook;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;

/**
 * MockHook
 *
 * @author maomao
 * @since 2020-03-27
 */
public class MockHook implements DnsMessageHook {

    @Override
    public String name() {
        return "Mock Hook";
    }

    @Override
    public void onEvent(DnsMessage event, long sequence, boolean endOfBatch) throws Exception {

    }
}
