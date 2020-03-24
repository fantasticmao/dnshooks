package cn.fantasticmao.dnshooks.proxy.disruptor;

import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * DnsMessageExceptionHandler
 *
 * @author maomao
 * @since 2020-03-23
 */
@Slf4j
public class DnsMessageExceptionHandler implements ExceptionHandler<DnsMessage> {

    @Override
    public void handleEventException(Throwable ex, long sequence, DnsMessage event) {
        log.error("handleEventException, DNS Message: " + event, ex);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        log.error("handleOnStartException", ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        log.error("handleOnShutdownException", ex);
    }
}
