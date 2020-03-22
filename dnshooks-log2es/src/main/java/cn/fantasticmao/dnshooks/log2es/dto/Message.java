package cn.fantasticmao.dnshooks.log2es.dto;

import lombok.*;

import java.net.InetSocketAddress;

/**
 * Message
 *
 * @author maomao
 * @since 2020-03-21
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    public static final String FIELD_SEND = "send";
    public static final String FIELD_RECIPIENT = "recipient";
    public static final String FIELD_DOMAIN = "domain";

    private InetSocketAddress send;
    private InetSocketAddress recipient;
    private String domain;
}
