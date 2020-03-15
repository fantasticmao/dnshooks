package cn.fantasticmao.dnshooks.proxy.disruptor;

import io.netty.handler.codec.dns.DnsResponse;

import java.net.SocketAddress;

/**
 * DnsMessage
 *
 * @author maomao
 * @since 2020-03-12
 */
public class DnsMessage {
    private DnsResponse content;
    private SocketAddress sender;
    private SocketAddress recipient;

    public DnsMessage() {
    }

    public DnsResponse getContent() {
        return content;
    }

    public void setContent(DnsResponse content) {
        this.content = content;
    }

    public SocketAddress getSender() {
        return sender;
    }

    public void setSender(SocketAddress sender) {
        this.sender = sender;
    }

    public SocketAddress getRecipient() {
        return recipient;
    }

    public void setRecipient(SocketAddress recipient) {
        this.recipient = recipient;
    }
}
