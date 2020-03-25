package cn.fantasticmao.dnshooks.proxy.netty.handler;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;

/**
 * ObtainMessageChannelHandlerTest
 *
 * @author maomao
 * @since 2020-03-25
 */
public class ObtainMessageChannelHandlerTest {

    @Test
    public void getMessage() throws InterruptedException {
        final ObtainMessageChannelHandler<String> obtainMessageChannelHandler
            = new ObtainMessageChannelHandler<>(String.class);
        final EmbeddedChannel embeddedChannel = new EmbeddedChannel(obtainMessageChannelHandler);
        final String text = "Hello World";
        embeddedChannel.writeInbound(text);
        // use ObtainMessageChannelHandler#getMessage() to obtain message
        Assert.assertFalse(embeddedChannel.finish());
        String message = obtainMessageChannelHandler.getMessage();
        Assert.assertEquals(text, message);
    }
}