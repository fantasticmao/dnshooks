package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.channel.AddressedEnvelope;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

import java.net.InetSocketAddress;

/**
 * DnsProxyClient
 *
 * @author maomao
 * @since 2020-03-12
 */
abstract class DnsProxyClient implements AutoCloseable {

    protected abstract DnsResponse lookup(final InetSocketAddress nameServer, final DnsQuery query)
        throws Exception;

    protected static final class AddressedEnvelopeAdapter
        implements AddressedEnvelope<DnsQuery, InetSocketAddress> {
        private final InetSocketAddress sender;
        private final InetSocketAddress recipient;
        private final AddressedEnvelope<DnsQuery, InetSocketAddress> in;

        AddressedEnvelopeAdapter(InetSocketAddress sender, InetSocketAddress recipient,
                                 AddressedEnvelope<DnsQuery, InetSocketAddress> in) {
            this.sender = sender;
            this.recipient = recipient;
            this.in = in;
        }

        @Override
        public DnsQuery content() {
            return in.content();
        }

        @Override
        public InetSocketAddress sender() {
            return this.sender;
        }

        @Override
        public InetSocketAddress recipient() {
            return this.recipient;
        }

        @Override
        public AddressedEnvelope<DnsQuery, InetSocketAddress> retain() {
            return in.retain();
        }

        @Override
        public AddressedEnvelope<DnsQuery, InetSocketAddress> retain(int increment) {
            return in.retain(increment);
        }

        @Override
        public AddressedEnvelope<DnsQuery, InetSocketAddress> touch() {
            return in.touch();
        }

        @Override
        public AddressedEnvelope<DnsQuery, InetSocketAddress> touch(Object hint) {
            return in.touch(hint);
        }

        @Override
        public int refCnt() {
            return in.refCnt();
        }

        @Override
        public boolean release() {
            return in.release();
        }

        @Override
        public boolean release(int decrement) {
            return in.release(decrement);
        }
    }
}