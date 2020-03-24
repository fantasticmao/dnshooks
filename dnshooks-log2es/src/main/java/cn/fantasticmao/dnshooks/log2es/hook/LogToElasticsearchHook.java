package cn.fantasticmao.dnshooks.log2es.hook;

import cn.fantasticmao.dnshooks.log2es.dto.Message;
import cn.fantasticmao.dnshooks.log2es.service.MessageService;
import cn.fantasticmao.dnshooks.log2es.service.MessageServiceImpl;
import cn.fantasticmao.dnshooks.log2es.util.Constant;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;
import io.netty.handler.codec.dns.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * LogToElasticsearchHook
 *
 * @author maomao
 * @since 2020-03-12
 */
@Slf4j
public class LogToElasticsearchHook implements DnsMessageHook {
    private MessageService service;

    public LogToElasticsearchHook() {
        this.service = new MessageServiceImpl();
        try {
            boolean result = this.service.createIfNotExists();
            if (!result) {
                log.warn("create if not exists {} fail", Constant.INDEX_NAME);
            }
        } catch (IOException e) {
            log.error("create if not exists " + Constant.INDEX_NAME + " error", e);
        }
    }

    @Override
    public String name() {
        return "Log To Elasticsearch";
    }

    @Override
    public void onEvent(DnsMessage event, long sequence, boolean endOfBatch) throws Exception {
        final DnsQuery queryBefore = event.getQueryBefore();
        final DnsResponse responseAfter = event.getResponseAfter();
        if (queryBefore.count(DnsSection.QUESTION) > 0
            && queryBefore instanceof DatagramDnsQuery
            && responseAfter instanceof DatagramDnsResponse) {
            final DatagramDnsQuery dnsQuery = (DatagramDnsQuery) queryBefore;
            final DatagramDnsResponse dnsResponse = (DatagramDnsResponse) responseAfter;
            final DnsRecord dnsQuestion = dnsQuery.recordAt(DnsSection.QUESTION);
            final Message message = new Message(LocalDateTime.now(), dnsQuery.sender(), dnsResponse.sender(),
                dnsQuestion.name());
            try {
                boolean result = service.save(message);
                if (!result) {
                    log.warn("save message {} to {} fail", message, Constant.INDEX_NAME);
                }
            } catch (IOException e) {
                log.error("save message " + message + " to " + Constant.INDEX_NAME + " error", e);
            }
        } else {
            log.warn("dns query queries count less than 1: {}", queryBefore);
        }
    }

    @Override
    public void close() throws Exception {
        service.close();
    }
}
