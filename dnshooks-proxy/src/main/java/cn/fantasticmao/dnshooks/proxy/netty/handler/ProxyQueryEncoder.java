package cn.fantasticmao.dnshooks.proxy.netty.handler;

import cn.fantasticmao.dnshooks.proxy.netty.AttributeKeyConstant;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyDatagramClient;
import cn.fantasticmao.dnshooks.proxy.netty.handler.codec.DnsMessageUtil;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DatagramDnsQueryEncoder;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsRecordEncoder;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Used for change the DNS query's sender and recipient, and will NOT change the DNS record.
 *
 * <p>
 * For example: change <code>DnsQuery(from: /192.168.1.66:1234, to: /192.168.1.66:53)</code>
 * to <code>DnsQuery(from: /192.168.1.66:5678, to: /192.168.1.1:53)</code>
 * </p>
 *
 * @author maomao
 * @see ProxyResponseDecoder
 * @since 2020-03-24
 */
public interface ProxyQueryEncoder extends ChannelOutboundHandler {

    /**
     * Encodes a {@link DatagramDnsQuery} into a {@link DatagramPacket}.
     */
    @Slf4j
    @Immutable
    @ChannelHandler.Sharable
    class Udp extends DatagramDnsQueryEncoder implements ProxyQueryEncoder {
        private final DnsProxyDatagramClient client;

        public Udp(@Nonnull DnsProxyDatagramClient client) {
            super(DnsRecordEncoder.DEFAULT);
            this.client = client;
        }

        @Override
        protected void encode(ChannelHandlerContext ctx,
                              AddressedEnvelope<DnsQuery, InetSocketAddress> in, List<Object> out) throws Exception {
            // get DNS server address from channel
            final InetSocketAddress recipient = (InetSocketAddress) ctx.channel().remoteAddress();
            final InetSocketAddress sender = in.sender();
            log.trace("save DnsQuery raw sender address: {}", sender);
            ctx.channel().attr(AttributeKeyConstant.RAW_SENDER).set(sender);

            final DatagramDnsQuery queryAfter = DnsMessageUtil.newUdpQuery(sender, recipient, in.content());
            log.trace("save DnsQuery after DNSHooks-Proxy: {}", queryAfter);
            ctx.channel().attr(AttributeKeyConstant.QUERY_AFTER).set(queryAfter);

            final DefaultAddressedEnvelope<DnsQuery, InetSocketAddress> addressedEnvelope
                = new DefaultAddressedEnvelope<>(queryAfter.retain(), recipient, null);
            super.encode(ctx, addressedEnvelope, out);
        }
    }
}
