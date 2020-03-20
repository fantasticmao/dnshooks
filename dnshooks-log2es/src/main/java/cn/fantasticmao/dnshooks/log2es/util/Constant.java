package cn.fantasticmao.dnshooks.log2es.util;

/**
 * Constant
 *
 * @author maomao
 * @since 2020-03-21
 */
public interface Constant {
    String INDEX_NAME = System.getProperty("dnshooks.log2es.index.name", "dns_message");
    String ELASTICSEARCH_HOSTS = System.getProperty("dnshooks.log2es.es.hosts", "http://localhost:9200");
}
