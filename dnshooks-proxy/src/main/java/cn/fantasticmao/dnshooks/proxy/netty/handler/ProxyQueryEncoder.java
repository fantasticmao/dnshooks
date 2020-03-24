package cn.fantasticmao.dnshooks.proxy.netty.handler;

import cn.fantasticmao.dnshooks.proxy.netty.AttributeKeyConstant;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProxyDatagramClient;
import cn.fantasticmao.dnshooks.proxy.netty.handler.codec.AddressedEnvelopeAdapter;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.handler.codec.dns.DatagramDnsQueryEncoder;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsRecordEncoder;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * ProxyQueryEncoder
 *
 * @author maomao
 * @since 2020-03-24
 */
public interface ProxyQueryEncoder extends ChannelOutboundHandler {

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
            log.trace("save DnsQuery raw sender address: {}", in.sender());
            ctx.channel().attr(AttributeKeyConstant.RAW_SENDER).set(in.sender());

            // get DNS server address from channel
            final InetSocketAddress recipient = (InetSocketAddress) ctx.channel().remoteAddress();
            AddressedEnvelopeAdapter queryProxy = new AddressedEnvelopeAdapter(null, recipient, in);

            log.trace("save DnsQuery after DNSHooks-Proxy: {}", in.content());
            ctx.channel().attr(AttributeKeyConstant.QUERY_AFTER).set(in.content());
            super.encode(ctx, queryProxy, out);
        }
    }
}
