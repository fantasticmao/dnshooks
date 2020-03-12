package cn.fantasticmao.dnshooks.proxy;

/**
 * DnsRecordMessage
 *
 * @author maomao
 * @since 2020-03-12
 */
class DnsRecordMessage {
    private String name;

    DnsRecordMessage() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
