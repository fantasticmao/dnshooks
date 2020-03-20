package cn.fantasticmao.dnshooks.log2es.hook;

import cn.fantasticmao.dnshooks.log2es.dto.Message;
import cn.fantasticmao.dnshooks.log2es.service.MessageService;
import cn.fantasticmao.dnshooks.log2es.service.MessageServiceImpl;
import cn.fantasticmao.dnshooks.log2es.util.Constant;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;
import io.netty.channel.AddressedEnvelope;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.DnsSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * LogToElasticsearchHook
 *
 * @author maomao
 * @since 2020-03-12
 */
public class LogToElasticsearchHook implements DnsMessageHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogToElasticsearchHook.class);
    private MessageService service;

    public LogToElasticsearchHook() {
        this.service = new MessageServiceImpl();
        try {
            boolean result = this.service.createIfNotExists();
            if (!result) {
                LOGGER.warn("create if not exists {} fail", Constant.INDEX_NAME);
            }
        } catch (IOException e) {
            LOGGER.error("create if not exists " + Constant.INDEX_NAME + " error", e);
        }
    }

    @Override
    public String name() {
        return "Log To Elasticsearch";
    }

    @Override
    public void onEvent(DnsMessage event, long sequence, boolean endOfBatch) throws Exception {
        final AddressedEnvelope<DnsQuery, InetSocketAddress> queryBefore = event.getQueryBefore();
        final AddressedEnvelope<DnsResponse, InetSocketAddress> responseAfter = event.getResponseAfter();
        if (queryBefore.content().count(DnsSection.QUESTION) > 0) {
            DnsRecord dnsRecord = queryBefore.content().recordAt(DnsSection.QUESTION);
            final Message message = new Message(queryBefore.sender(), responseAfter.sender(),
                dnsRecord.name());
            try {
                boolean result = service.save(message);
                if (!result) {
                    LOGGER.warn("save message {} to {} fail", message, Constant.INDEX_NAME);
                }
            } catch (IOException e) {
                LOGGER.error("save message " + message + " to " + Constant.INDEX_NAME + " error", e);
            }
        } else {
            LOGGER.warn("dns query queries count less than 1: {}", queryBefore);
        }
    }

    @Override
    public void close() {
        try {
            service.close();
        } catch (Exception e) {
            LOGGER.error("close DnsMessageHook " + name() + " error", e);
        }
    }
}
