package cn.fantasticmao.dnshooks.proxy;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageFactory;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;
import cn.fantasticmao.dnshooks.proxy.disruptor.HookThreadFactory;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyClient;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyDatagramClient;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyServer;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

/**
 * Main
 *
 * @author maomao
 * @since 2020-03-11
 */
public class Main {
    private static final int RINGBUFFER_DEFAULT_SIZE = 256 * 1024;

    public static void main(String[] args) throws Exception {
        // use blocking wait strategy，avoid to cost a lot of CPU resources
        final WaitStrategy waitStrategy = new BlockingWaitStrategy();
        final Disruptor<DnsMessage> disruptor = new Disruptor<>(DnsMessageFactory.INSTANCE,
            RINGBUFFER_DEFAULT_SIZE, HookThreadFactory.INSTANCE, ProducerType.MULTI, waitStrategy);
        // register all DNS hooks as event handler to Disruptor
        DnsMessageHook[] handlers = loadHooks().toArray(new DnsMessageHook[0]);
        disruptor.handleEventsWith(handlers);
        // start Disruptor
        disruptor.start();

        final InetSocketAddress localAddress = new InetSocketAddress(53);
        try (DnsProxyClient client = new DnsProxyDatagramClient(localAddress);
             DnsProxyServer server = new DnsProxyServer(client, disruptor)) {
            server.run();
        } finally {
            disruptor.shutdown(1000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * use ServiceLoader to find all DNS hooks
     *
     * @return {@link DnsMessageHook} list
     */
    private static List<DnsMessageHook> loadHooks() {
        List<DnsMessageHook> handlerList = new LinkedList<>();
        ServiceLoader.load(DnsMessageHook.class).forEach(handlerList::add);
        return handlerList;
    }
}
