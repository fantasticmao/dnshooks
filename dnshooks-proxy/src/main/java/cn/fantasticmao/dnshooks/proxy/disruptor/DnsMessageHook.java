package cn.fantasticmao.dnshooks.proxy.disruptor;

import com.lmax.disruptor.EventHandler;

/**
 * DnsMessageHook
 *
 * @author maomao
 * @since 2020-03-15
 */
public interface DnsMessageHook extends EventHandler<DnsMessage>, AutoCloseable {

    /**
     * define the hook name
     *
     * @return The DNS hook name
     */
    String name();

    /**
     * {@inheritDoc}
     */
    void onEvent(DnsMessage event, long sequence, boolean endOfBatch) throws Exception;

    @Override
    default void close() {
        // adapter for AutoCloseable
    }
}
