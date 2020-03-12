package cn.fantasticmao.dnshooks.proxy;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HookThreadFactory
 *
 * @author maomao
 * @since 2020-03-12
 */
enum HookThreadFactory implements ThreadFactory {
    INSTANCE;

    private static AtomicInteger count = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable runnable) {
        String threadName = "dnshook-handler-" + count.incrementAndGet();
        Thread thread = new Thread(runnable, threadName);
        thread.setDaemon(true);
        return thread;
    }
}
