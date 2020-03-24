package cn.fantasticmao.dnshooks.proxy.netty;

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
public interface AttributeKeyConstant {
    AttributeKey<InetSocketAddress> RAW_SENDER = AttributeKey.valueOf("raw_sender");

    AttributeKey<DnsQuery> QUERY_BEFORE = AttributeKey.valueOf("query_before");

    AttributeKey<DnsQuery> QUERY_AFTER = AttributeKey.valueOf("query_after");

    AttributeKey<DnsResponse> RESPONSE_BEFORE = AttributeKey.valueOf("response_before");
}
