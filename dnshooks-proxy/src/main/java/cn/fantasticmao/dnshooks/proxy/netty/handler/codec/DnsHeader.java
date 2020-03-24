package cn.fantasticmao.dnshooks.proxy.netty.handler.codec;

import io.netty.handler.codec.dns.DnsOpCode;
import io.netty.handler.codec.dns.DnsResponseCode;

/**
 * DnsHeader
 *
 * @author maomao
 * @see <a href="https://tools.ietf.org/html/rfc1035#section-4.1.1">RFC1035#section-4.1.1</a>
 * @since 2020-03-24
 */
public class DnsHeader {
    /**
     * A 16 bit identifier assigned by the program that generates any kind of query. This identifier is copied
     * the corresponding reply and can be used by the requester to match up replies to outstanding queries.
     */
    private short id;

    /**
     * A one bit field that specifies whether this message is a query (0), or a response (1).
     */
    private boolean qr;

    /**
     * A four bit field that specifies kind of query in this message. This value is set by the originator of a query
     * and copied into the response.
     */
    private DnsOpCode opCode;

    /**
     * Authoritative Answer - this bit is valid in responses, and specifies that the responding name server is an
     * authority for the domain name in question section.
     */
    private boolean aa;

    /**
     * TrunCation - specifies that this message was truncated due to length greater than that permitted on the
     * transmission channel.
     */
    private boolean tc;

    /**
     * Recursion Desired - this bit may be set in a query and is copied into the response.  If RD is set, it directs
     * the name server to pursue the query recursively. Recursive query support is optional.
     */
    private boolean rd;

    /**
     * Recursion Available - this be is set or cleared in a response, and denotes whether recursive query support is
     * available in the name server.
     */
    private boolean ra;

    /**
     * Reserved for future use.  Must be zero in all queries and responses.
     */
    private byte z;

    /**
     * Response code - this 4 bit field is set as part of responses.
     */
    private DnsResponseCode rCode;

    /**
     * an unsigned 16 bit integer specifying the number of entries in the question section.
     */
    private short qdCount;

    /**
     * an unsigned 16 bit integer specifying the number of resource records in the answer section.
     */
    private short anCount;

    /**
     * an unsigned 16 bit integer specifying the number of name server resource records in the authority records section.
     */
    private short nsCount;

    /**
     * an unsigned 16 bit integer specifying the number of resource records in the additional records section.
     */
    private short arCount;
}
