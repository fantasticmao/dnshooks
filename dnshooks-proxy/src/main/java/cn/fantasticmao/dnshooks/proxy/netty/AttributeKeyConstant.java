package cn.fantasticmao.dnshooks.proxy.netty;

import io.netty.channel.AddressedEnvelope;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

/**
 * AttributeKeyConstant
 *
 * @author maomao
 * @since 2020-03-19
 */
interface AttributeKeyConstant {
    AttributeKey<InetSocketAddress> RAW_SENDER = AttributeKey.valueOf("raw_sender");

    AttributeKey<AddressedEnvelope<? extends DnsQuery, InetSocketAddress>> QUERY_BEFORE
        = AttributeKey.valueOf("query_before");

    AttributeKey<AddressedEnvelope<? extends DnsQuery, InetSocketAddress>> QUERY_AFTER
        = AttributeKey.valueOf("query_after");

    AttributeKey<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> RESPONSE_BEFORE
        = AttributeKey.valueOf("response_before");
}
