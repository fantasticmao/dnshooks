package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageTranslator;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.dns.DatagramDnsResponseEncoder;
import io.netty.handler.codec.dns.DnsResponse;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

/**
 * DatagramDnsResponseHookEncoder
 *
 * @author maomao
 * @since 2020-03-11
 */
class DatagramDnsResponseHookEncoder extends DatagramDnsResponseEncoder {
    private final DnsProxyClient client;
    private final Disruptor<DnsMessage> disruptor;

    DatagramDnsResponseHookEncoder(Disruptor<DnsMessage> disruptor) {
        this.client = new DnsProxyClient();
        this.disruptor = disruptor;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, AddressedEnvelope<DnsResponse, InetSocketAddress> in, List<Object> out) throws Exception {
        try {
            final DnsResponse response = in.content();
            final SocketAddress sender = in.sender();
            final SocketAddress recipient = in.recipient();
            this.disruptor.getRingBuffer().tryPublishEvent(DnsMessageTranslator.INSTANCE, response, sender, recipient);
        } finally {
            super.encode(ctx, in, out);
        }
    }

}
