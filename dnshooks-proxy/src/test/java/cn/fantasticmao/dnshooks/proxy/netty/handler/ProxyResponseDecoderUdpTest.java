package cn.fantasticmao.dnshooks.proxy.netty.handler;

import cn.fantasticmao.dnshooks.proxy.netty.AttributeKeyConstant;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProtocolTest;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.dns.DatagramDnsResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * ProxyResponseDecoderUdpTest
 *
 * @author maomao
 * @since 2020-03-26
 */
public class ProxyResponseDecoderUdpTest extends DnsProtocolTest {

    @Test
    public void unitTest() throws Exception {
        final DatagramPacket dnsResponsePacket = super.newUdpPacket(super.dnsServerAddress, super.proxyClientAddress);
        final ProxyResponseDecoder.Udp responseDecoder = new ProxyResponseDecoder.Udp(super.proxyServerAddress);
        final EmbeddedChannel embeddedChannel = new EmbeddedChannel(responseDecoder);

        embeddedChannel.attr(AttributeKeyConstant.RAW_SENDER).set(super.dnsClientAddress);
        embeddedChannel.writeInbound(dnsResponsePacket);
        Assert.assertTrue(embeddedChannel.finish());

        final DatagramDnsResponse responseBefore = (DatagramDnsResponse) embeddedChannel
            .attr(AttributeKeyConstant.RESPONSE_BEFORE).get();
        //Assert.assertEquals(super.dnsServerAddress, responseBefore.sender());
        Assert.assertEquals(super.proxyClientAddress, responseBefore.recipient());

        final DatagramDnsResponse responseAfter = embeddedChannel.readInbound();
        Assert.assertEquals(super.proxyServerAddress, responseAfter.sender());
        Assert.assertEquals(super.dnsClientAddress, responseAfter.recipient());
    }

}