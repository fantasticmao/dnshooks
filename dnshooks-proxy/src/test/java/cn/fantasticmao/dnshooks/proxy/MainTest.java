package cn.fantasticmao.dnshooks.proxy;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * MainTest
 *
 * @author maomao
 * @since 2020-03-27
 */
public class MainTest {

    @Test
    public void choseDnsServerAddress() {
        InetSocketAddress dnsServer = Main.choseDnsServerAddress();
        Assert.assertNotNull(dnsServer);
        Assert.assertEquals(53, dnsServer.getPort());
    }

    @Test
    public void loadHooks() {
        List<DnsMessageHook> hooks = Main.loadHooks();
        Assert.assertEquals(2, hooks.size());
        Assert.assertEquals("No Operation Hook", hooks.get(0).name());
        Assert.assertEquals("Mock Hook", hooks.get(1).name());
    }

    @Test
    public void readBanner() throws Exception {
        byte[] bytes = Main.readBanner("/banner.txt");
        Assert.assertNotNull(bytes);
    }

}