package cn.fantasticmao.dnshooks.proxy;

/**
 * Main
 *
 * @author maomao
 * @since 2020-03-11
 */
public class Main {
    private static final int RINGBUFFER_DEFAULT_SIZE = 256 * 1024;

    public static void main(String[] args) {
        new DnsProxyServer(RINGBUFFER_DEFAULT_SIZE)
            .run();
    }
}
