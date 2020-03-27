package cn.fantasticmao.dnshooks.proxy.netty.handler;

import cn.fantasticmao.dnshooks.proxy.netty.AttributeKeyConstant;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProtocolTest;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyDatagramClient;
import cn.fantasticmao.dnshooks.proxy.netty.handler.codec.DnsMessageTriplet;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DatagramDnsResponse;
import io.netty.handler.codec.dns.DnsQuery;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;

/**
 * DnsProxyServerClientHandlerTest
 *
 * @author maomao
 * @since 2020-03-26
 */
public class DnsProxyServerClientHandlerTest extends DnsProtocolTest {

    @Test
    public void proxySuccess() throws Exception {
        final DnsQuery dnsQuery = newUdpQuery(super.dnsClientAddress, super.proxyServerAddress);
        try (DnsProxyDatagramClient client = new DnsProxyDatagramClient(super.proxyServerAddress, super.dnsServerAddress)) {
            final EmbeddedChannel embeddedChannel = new EmbeddedChannel(new DnsProxyServerClientHandler(client));
            embeddedChannel.writeInbound(dnsQuery);
            Assert.assertFalse(embeddedChannel.finish());

            final DatagramDnsQuery queryBefore = (DatagramDnsQuery) embeddedChannel
                .attr(AttributeKeyConstant.QUERY_BEFORE).get();
            Assert.assertEquals(super.dnsClientAddress, queryBefore.sender());
            Assert.assertEquals(super.proxyServerAddress, queryBefore.recipient());
            final DatagramDnsQuery queryAfter = (DatagramDnsQuery) embeddedChannel
                .attr(AttributeKeyConstant.QUERY_AFTER).get();
            //Assert.assertEquals(super.proxyClientAddress, queryAfter.sender());
            Assert.assertEquals(super.dnsServerAddress, queryAfter.recipient());
            final DatagramDnsResponse responseBefore = (DatagramDnsResponse) embeddedChannel
                .attr(AttributeKeyConstant.RESPONSE_BEFORE).get();
            Assert.assertEquals(super.dnsServerAddress, responseBefore.sender());
            Assert.assertNotNull(responseBefore.recipient());
        }
    }

    @Test
    public void proxyFail() throws Exception {
        final DnsQuery dnsQuery = newUdpQuery(super.dnsClientAddress, super.proxyServerAddress);
        try (DnsProxyDatagramClient client = new DnsProxyDatagramClient(super.proxyServerAddress, super.dnsServerAddress) {
            @Nonnull
            @Override
            public DnsMessageTriplet lookup(@Nonnull DnsQuery query) throws Exception {
                throw new UnitTestException();
            }
        }) {
            final EmbeddedChannel embeddedChannel = new EmbeddedChannel(new DnsProxyServerClientHandler(client));
            embeddedChannel.writeInbound(dnsQuery);
            Assert.assertFalse(embeddedChannel.finish());
            Assert.assertNull(embeddedChannel.attr(AttributeKeyConstant.QUERY_AFTER).get());
            Assert.assertNull(embeddedChannel.attr(AttributeKeyConstant.RESPONSE_BEFORE).get());
        }
    }
}