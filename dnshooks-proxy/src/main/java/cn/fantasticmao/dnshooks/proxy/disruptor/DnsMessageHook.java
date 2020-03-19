package cn.fantasticmao.dnshooks.proxy.disruptor;

import com.lmax.disruptor.EventHandler;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

/**
 * DnsMessageHook
 *
 * @author maomao
 * @since 2020-03-15
 */
public interface DnsMessageHook extends EventHandler<DnsMessage<DnsQuery, DnsResponse>> {

    /**
     * define the hook name
     *
     * @return The DNS hook name
     */
    String name();

    /**
     * {@inheritDoc}
     */
    void onEvent(DnsMessage<DnsQuery, DnsResponse> event, long sequence, boolean endOfBatch) throws Exception;
}
