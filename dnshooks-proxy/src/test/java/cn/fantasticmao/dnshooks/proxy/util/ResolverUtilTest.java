package cn.fantasticmao.dnshooks.proxy.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * ResolverUtilTest
 *
 * @author maomao
 * @since 2020/3/14
 */
public class ResolverUtilTest {

    @Test
    public void nameServer() {
        List<String> nameServerIpAddress = ResolverUtil.nameServer();
        Assert.assertNotNull(nameServerIpAddress);
        System.out.println(nameServerIpAddress);
    }
}