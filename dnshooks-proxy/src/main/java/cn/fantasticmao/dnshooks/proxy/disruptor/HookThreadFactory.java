package cn.fantasticmao.dnshooks.proxy.disruptor;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HookThreadFactory
 *
 * @author maomao
 * @since 2020-03-12
 */
public enum HookThreadFactory implements ThreadFactory {
    INSTANCE;

    private static AtomicInteger count = new AtomicInteger(0);

    @Override
    public Thread newThread(@Nonnull Runnable runnable) {
        String threadName = "DNSHooks-Processor-" + count.incrementAndGet();
        Thread thread = new Thread(runnable, threadName);
        thread.setDaemon(true);
        return thread;
    }
}
