package cn.fantasticmao.dnshooks.proxy.hook;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

/**
 * DnsMessageHookShell
 *
 * @author maomao
 * @since 2020-03-15
 */
public class DnsMessageHookShell implements DnsMessageHook {

    @Override
    public String name() {
        return "Shell Hook";
    }

    @Override
    public void onEvent(DnsMessage<DnsQuery, DnsResponse> event, long sequence, boolean endOfBatch) throws Exception {
        // TODO implement default Shell hook
        throw new UnsupportedOperationException("TODO implement default Shell hook");
    }
}
