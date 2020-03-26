package cn.fantasticmao.dnshooks.proxy.netty.handler.codec;

import io.netty.handler.codec.dns.*;
import io.netty.util.ReferenceCounted;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;

/**
 * DnsMessageUtil
 *
 * @author maomao
 * @since 2020-03-25
 */
public class DnsMessageUtil {

    /**
     * new {@link DatagramDnsQuery}
     */
    @Nonnull
    public static DatagramDnsQuery newUdpQuery(@Nonnull InetSocketAddress sender,
                                               @Nonnull InetSocketAddress recipient,
                                               @Nonnull DnsQuery dnsQuery) {
        final DatagramDnsQuery newQuery = new DatagramDnsQuery(sender, recipient, dnsQuery.id(), dnsQuery.opCode())
            .setRecursionDesired(dnsQuery.isRecursionDesired())
            .setZ(dnsQuery.z());
        if (dnsQuery.count(DnsSection.QUESTION) > 0) {
            setRecord(DnsSection.QUESTION, dnsQuery, newQuery);
        }
        return newQuery;
    }

    /**
     * new {@link DatagramDnsResponse}
     */
    public static DatagramDnsResponse newUdpResponse(@Nonnull InetSocketAddress sender,
                                                     @Nonnull InetSocketAddress recipient,
                                                     @Nonnull DnsResponse dnsResponse) {
        final DatagramDnsResponse newResponse = new DatagramDnsResponse(sender, recipient, dnsResponse.id(),
            dnsResponse.opCode(), dnsResponse.code())
            .setAuthoritativeAnswer(dnsResponse.isAuthoritativeAnswer())
            .setTruncated(dnsResponse.isTruncated())
            .setRecursionAvailable(dnsResponse.isRecursionAvailable())
            .setRecursionDesired(dnsResponse.isRecursionDesired())
            .setZ(dnsResponse.z());
        if (dnsResponse.count(DnsSection.QUESTION) > 0) {
            setRecord(DnsSection.QUESTION, dnsResponse, newResponse);
        }
        if (dnsResponse.count(DnsSection.ANSWER) > 0) {
            setRecord(DnsSection.ANSWER, dnsResponse, newResponse);
        }
        if (dnsResponse.count(DnsSection.AUTHORITY) > 0) {
            setRecord(DnsSection.AUTHORITY, dnsResponse, newResponse);
        }
        if (dnsResponse.count(DnsSection.ADDITIONAL) > 0) {
            setRecord(DnsSection.ADDITIONAL, dnsResponse, newResponse);
        }
        return newResponse;
    }

    /**
     * new error {@link DatagramDnsResponse}
     */
    public static DatagramDnsResponse newErrorUdpResponse(@Nonnull DatagramDnsQuery datagramDnsQuery,
                                                          @Nonnull DnsResponseCode rCode) {
        return newErrorUdpResponse(datagramDnsQuery.recipient(), datagramDnsQuery.sender(),
            datagramDnsQuery, rCode);
    }

    /**
     * new error {@link DatagramDnsResponse}
     */
    public static DatagramDnsResponse newErrorUdpResponse(@Nonnull DatagramDnsResponse datagramDnsResponse,
                                                          @Nonnull DnsResponseCode rCode) {
        return newErrorUdpResponse(datagramDnsResponse.recipient(), datagramDnsResponse.sender(),
            datagramDnsResponse, rCode);
    }

    /**
     * new error {@link DatagramDnsResponse}
     */
    public static DatagramDnsResponse newErrorUdpResponse(@Nonnull InetSocketAddress sender,
                                                          @Nonnull InetSocketAddress recipient,
                                                          @Nonnull DnsMessage dnsMessage,
                                                          @Nonnull DnsResponseCode rCode) {
        DatagramDnsResponse response = new DatagramDnsResponse(sender, recipient, dnsMessage.id(),
            dnsMessage.opCode(), rCode);
        if (dnsMessage.count(DnsSection.QUESTION) > 0) {
            setRecord(DnsSection.QUESTION, dnsMessage, response);
        }
        return response;
    }

    private static void setRecord(DnsSection dnsSection, DnsMessage oldDnsMessage, DnsMessage newDnsMessage) {
        for (int i = 0; i < oldDnsMessage.count(dnsSection); i++) {
            DnsRecord dnsRecord = oldDnsMessage.recordAt(dnsSection, i);
            if (dnsRecord instanceof ReferenceCounted) {
                ((ReferenceCounted) dnsRecord).retain();
            }
            newDnsMessage.addRecord(dnsSection, dnsRecord);
        }
    }
}
