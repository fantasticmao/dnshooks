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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.dns.DatagramDnsResponse;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.DnsResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DnsProxyServerDisruptorHandlerTest
 *
 * @author maomao
 * @since 2020-03-26
 */
@Slf4j
public class DnsProxyServerDisruptorHandlerTest extends DnsProtocolTest {

    @Test
    public void testSuccess() throws Exception {
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

    @Test
    public void testError() {
        final EmbeddedChannel embeddedChannel = new EmbeddedChannel(
            new ChannelOutboundHandlerAdapter() {
                private AtomicInteger triggerCount = new AtomicInteger(0);

                @Override
                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                    // trigger an exception
                    if (triggerCount.getAndIncrement() < 1) {
                        throw new UnitTestException();
                    }
                    super.write(ctx, msg, promise);
                }
            },
            new DnsProxyServerDisruptorHandler(null));

        final DatagramDnsResponse responseAfter = newUdpResponse(super.proxyServerAddress, super.dnsClientAddress);
        try {
            embeddedChannel.writeOutbound(responseAfter);
            Assert.fail();
        } catch (UnitTestException ignore) {
        }
        Assert.assertTrue(embeddedChannel.finish());

        final DatagramDnsResponse dnsResponse = embeddedChannel.readOutbound();
        Assert.assertEquals(responseAfter.sender(), dnsResponse.sender());
        Assert.assertEquals(responseAfter.recipient(), dnsResponse.recipient());
        Assert.assertEquals(DnsResponseCode.SERVFAIL, dnsResponse.code());
    }

    /**
     * Used to synchronized get message from Disruptor {@link com.lmax.disruptor.EventHandler}
     */
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