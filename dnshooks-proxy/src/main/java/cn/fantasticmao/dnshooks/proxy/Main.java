package cn.fantasticmao.dnshooks.proxy;

import cn.fantasticmao.dnshooks.proxy.disruptor.*;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyClient;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyDatagramClient;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyServer;
import cn.fantasticmao.dnshooks.proxy.util.Constant;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {
        printBanner();

        // use blocking wait strategy，avoid to cost a lot of CPU resources
        final WaitStrategy waitStrategy = new BlockingWaitStrategy();
        final Disruptor<DnsMessage> disruptor = new Disruptor<>(DnsMessageFactory.INSTANCE,
            Constant.RINGBUFFER_SIZE, HookThreadFactory.INSTANCE, ProducerType.MULTI, waitStrategy);
        // register all DNS hooks as event handler to Disruptor
        DnsMessageHook[] handlers = loadHooks().toArray(new DnsMessageHook[0]);
        disruptor.handleEventsWith(handlers);
        disruptor.setDefaultExceptionHandler(new DnsMessageExceptionHandler());
        // start Disruptor
        disruptor.start();

        final InetSocketAddress localAddress = new InetSocketAddress(53);
        final DnsProxyClient client = new DnsProxyDatagramClient(localAddress);
        final DnsProxyServer server = new DnsProxyServer(client, disruptor);
        server.run();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.close();
                    server.close();
                    disruptor.shutdown(1000, TimeUnit.MILLISECONDS);
                    for (DnsMessageHook hook : handlers) {
                        hook.close();
                    }
                } catch (Exception e) {
                    log.error("catch an exception in JVM shutdown hook", e);
                }
            }
        }));
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

    private static void printBanner() throws URISyntaxException, IOException {
        Path path = Paths.get(Main.class.getResource("/banner.txt").toURI());
        byte[] bytes = Files.readAllBytes(path);
        System.out.write(bytes);
    }
}
