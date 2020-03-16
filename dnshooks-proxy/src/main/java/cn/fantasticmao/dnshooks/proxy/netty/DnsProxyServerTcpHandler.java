package cn.fantasticmao.dnshooks.proxy.netty;

import cn.fantasticmao.dnshooks.proxy.disruptor.DnsMessage;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsResponse;

/**
 * DnsProxyServerTcpHandler
 *
 * @author maomao
 * @since 2020/3/16
 */
public class DnsProxyServerTcpHandler extends DnsProxyServerAbstractHandler<DnsQuery> {

    public DnsProxyServerTcpHandler(Disruptor<DnsMessage> disruptor) {
        super(disruptor);
    }

    @Override
    protected DnsResponse proxy(DnsQuery query) {
        // TODO implement DNS-over-TCP handler
        throw new UnsupportedOperationException("TODO implement DNS-over-TCP handler");
    }
}
