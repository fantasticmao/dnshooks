package cn.fantasticmao.dnshooks.proxy.netty.handler;

import cn.fantasticmao.dnshooks.proxy.netty.AttributeKeyConstant;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyDatagramClient;
import cn.fantasticmao.dnshooks.proxy.netty.handler.codec.DnsMessageUtil;
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
 * Used for change the DNS response's sender and recipient, and will NOT change the DNS record.
 *
 * <p>
 * For example: change <code>DnsResponse(from: /192.168.1.1:53, to: /192.168.1.66:5678)</code>
 * to <code>DnsResponse(from: /192.168.1.66:53, to: /192.168.1.66:1234)</code>
 * </p>
 *
 * @author maomao
 * @see ProxyQueryEncoder
 * @since 2020-03-24
 */
public interface ProxyResponseDecoder extends ChannelInboundHandler {

    /**
     * Decodes a {@link DatagramPacket} into a {@link DatagramDnsResponse}.
     */
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
            final DatagramDnsResponse responseBefore = (DatagramDnsResponse) super.decodeResponse(null, packet);
            log.trace("save DnsResponse before DNSHooks-Proxy: {}", responseBefore);
            ctx.channel().attr(AttributeKeyConstant.RESPONSE_BEFORE).set(responseBefore);

            final DatagramDnsResponse responseAfter = DnsMessageUtil.newUdpResponse(client.getLocalAddress(),
                recipient, responseBefore);
            out.add(responseAfter);
        }
    }
}
