package cn.fantasticmao.dnshook.log2es.hook;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DnsMessageHookLogToElasticsearch
 *
 * @author maomao
 * @since 2020-03-12
 */
public class DnsMessageHookLogToElasticsearch implements DnsMessageHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(DnsMessageHookLogToElasticsearch.class);

    @Override

    public String name() {
        return "Log To Elasticsearch";
    }

    @Override
    public void onEvent(DnsMessage event, long sequence, boolean endOfBatch) throws Exception {
        // TODO implement log DNS message to Elasticsearch
        final DnsQuery query = event.getQueryBefore().content();
        final DnsResponse response = event.getResponseAfter().content();
        LOGGER.info("query {}", query);
        LOGGER.info("response {}", response);
    }
}
