package cn.fantasticmao.dnshooks.proxy.netty.handler;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageFactory;
import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessageHook;
import cn.fantasticmao.dnshooks.proxy.disruptor.HookThreadFactory;
import cn.fantasticmao.dnshooks.proxy.netty.AttributeKeyConstant;
import cn.fantasticmao.dnshooks.proxy.netty.DnsProtocolTest;
import cn.fantasticmao.dnshooks.proxy.util.Constant;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * DnsProxyServerDisruptorHandlerTest
 *
 * @author maomao
 * @since 2020-03-26
 */
@Slf4j
public class DnsProxyServerDisruptorHandlerTest extends DnsProtocolTest {

    @Test
    public void unitTest() throws Exception {
        final WaitStrategy waitStrategy = new BlockingWaitStrategy();
        final Disruptor<DnsMessage> disruptor = new Disruptor<>(DnsMessageFactory.INSTANCE,
            Constant.RINGBUFFER_SIZE, HookThreadFactory.INSTANCE, ProducerType.MULTI, waitStrategy);
        final SyncEventHandler syncEventHandler = new SyncEventHandler();
        disruptor.handleEventsWith(syncEventHandler);
        disruptor.start();
        log.trace("start Disruptor/Ring Buffer success");

        final EmbeddedChannel embeddedChannel = new EmbeddedChannel(new DnsProxyServerDisruptorHandler(disruptor));

        final DnsQuery queryBefore = newUdpQuery(super.dnsClientAddress, super.proxyServerAddress);
        embeddedChannel.attr(AttributeKeyConstant.QUERY_BEFORE).set(queryBefore);

        final DnsQuery queryAfter = newUdpQuery(super.proxyClientAddress, super.dnsServerAddress);
        embeddedChannel.attr(AttributeKeyConstant.QUERY_AFTER).set(queryAfter);

        final DnsResponse responseBefore = newUdpResponse(super.dnsServerAddress, super.proxyClientAddress);
        embeddedChannel.attr(AttributeKeyConstant.RESPONSE_BEFORE).set(responseBefore);

        final DnsResponse responseAfter = newUdpResponse(super.proxyServerAddress, super.dnsClientAddress);
        embeddedChannel.writeOutbound(responseAfter);
        Assert.assertTrue(embeddedChannel.finish());
        Assert.assertEquals(responseAfter, embeddedChannel.readOutbound());

        final DnsMessage dnsMessage = syncEventHandler.getMessage();
        Assert.assertEquals(queryBefore, dnsMessage.getQueryBefore());
        Assert.assertEquals(queryAfter, dnsMessage.getQueryAfter());
        Assert.assertEquals(responseBefore, dnsMessage.getResponseBefore());
    }

    private static class SyncEventHandler implements DnsMessageHook {
        private BlockingQueue<DnsMessage> msg = new LinkedBlockingQueue<>();

        @Override
        public String name() {
            return "Sync Event Handler";
        }

        @Override
        public void onEvent(DnsMessage event, long sequence, boolean endOfBatch) throws Exception {
            this.msg.offer(event);
        }

        public DnsMessage getMessage() throws InterruptedException {
            return this.msg.take();
        }
    }

}