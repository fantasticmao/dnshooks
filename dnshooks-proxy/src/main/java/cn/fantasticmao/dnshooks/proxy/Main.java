package cn.fantasticmao.dnshooks.proxy;

import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyServer;

/**
 * Main
 *
 * @author maomao
 * @since 2020-03-11
 */
public class Main {
    private static final int RINGBUFFER_DEFAULT_SIZE = 256 * 1024;

    public static void main(String[] args) throws Exception {
        try (DnsProxyServer server = new DnsProxyServer(RINGBUFFER_DEFAULT_SIZE)) {
            server.run();
        }
    }
}
