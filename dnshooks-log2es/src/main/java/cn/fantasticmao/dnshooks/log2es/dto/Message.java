package cn.fantasticmao.dnshooks.log2es.dto;

import java.net.InetSocketAddress;

/**
 * Message
 *
 * @author maomao
 * @since 2020-03-21
 */
public class Message {
    private InetSocketAddress send;
    private InetSocketAddress recipient;
    private String queryDomain;

    public Message() {
    }

    public Message(InetSocketAddress send, InetSocketAddress recipient, String queryDomain) {
        this.send = send;
        this.recipient = recipient;
        this.queryDomain = queryDomain;
    }

    public InetSocketAddress getSend() {
        return send;
    }

    public void setSend(InetSocketAddress send) {
        this.send = send;
    }

    public InetSocketAddress getRecipient() {
        return recipient;
    }

    public void setRecipient(InetSocketAddress recipient) {
        this.recipient = recipient;
    }

    public String getQueryDomain() {
        return queryDomain;
    }

    public void setQueryDomain(String queryDomain) {
        this.queryDomain = queryDomain;
    }

    @Override
    public String toString() {
        return "Message{" +
            "send=" + send +
            ", recipient=" + recipient +
            ", queryDomain='" + queryDomain + '\'' +
            '}';
    }
}
