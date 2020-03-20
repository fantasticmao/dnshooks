package cn.fantasticmao.dnshooks.proxy.util;

/**
 * Constant
 *
 * @author maomao
 * @since 2020-03-21
 */
public interface Constant {
    int RINGBUFFER_SIZE = Integer.parseInt(System.getProperty("dnshooks.proxy.ringbuffer.size",
        Integer.toString(256 * 1024)));
}
