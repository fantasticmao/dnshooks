package cn.fantasticmao.dnshooks.proxy.netty.handler;

import cn.fantasticmao.dnshooks.proxy.netty.AttributeKeyConstant;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyDatagramClient;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.dns.DatagramDnsResponse;
import io.netty.handler.codec.dns.DatagramDnsResponseDecoder;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * ProxyResponseDecoder
 *
 * @author maomao
 * @since 2020-03-24
 */
public interface ProxyResponseDecoder extends ChannelInboundHandler {

    @Slf4j
    @Immutable
    @ChannelHandler.Sharable
    class Udp extends DatagramDnsResponseDecoder implements ProxyResponseDecoder {
        private final DnsProxyDatagramClient client;

        public Udp(@Nonnull DnsProxyDatagramClient client) {
            this.client = client;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
            // obtain raw sender, and it is the raw recipient in DnsResponse
            final InetSocketAddress recipient = ctx.channel().attr(AttributeKeyConstant.RAW_SENDER).get();
            log.trace("obtain DnsQuery raw sender address: {}", recipient);

            DatagramDnsResponse responseBefore = (DatagramDnsResponse) super.decodeResponse(null, packet.copy());
            log.trace("save DnsResponse before DNSHooks-Proxy: {}", responseBefore);
            ctx.channel().attr(AttributeKeyConstant.RESPONSE_BEFORE).set(responseBefore);

            DatagramPacket responseProxy = new DatagramPacket(packet.content(), recipient, client.getLocalAddress());
            super.decode(ctx, responseProxy, out);
        }
    }
}
