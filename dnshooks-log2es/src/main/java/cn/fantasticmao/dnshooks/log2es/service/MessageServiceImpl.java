package cn.fantasticmao.dnshooks.log2es.service;

import cn.fantasticmao.dnshooks.log2es.dto.Message;
import cn.fantasticmao.dnshooks.log2es.util.Constant;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * MessageServiceImpl
 *
 * @author maomao
 * @since 2020-03-21
 */
public class MessageServiceImpl implements MessageService {
    private RestHighLevelClient client;
    private final ZoneId zoneId;
    private final DateTimeFormatter dateTimeFormatter;
    private final String dateTimePattern = "yyyy-MM-dd HH:mm:ss";

    public MessageServiceImpl() {
        final HttpHost[] httpHosts = Stream.of(Constant.ELASTICSEARCH_HOSTS.split(","))
            .map(HttpHost::create)
            .toArray(HttpHost[]::new);
        final RestClientBuilder builder = RestClient.builder(httpHosts);
        this.client = new RestHighLevelClient(builder);
        this.zoneId = ZoneId.systemDefault();
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
    }

    @Override
    public void close() throws Exception {
        this.client.close();
    }

    @Override
    public boolean createIfNotExists() throws IOException {
        final GetIndexRequest getIndexRequest = new GetIndexRequest(Constant.INDEX_NAME);
        final boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if (exists) {
            return true;
        }

        final CreateIndexRequest createIndexRequest = new CreateIndexRequest(Constant.INDEX_NAME);
        createIndexRequest.mapping(indexMapping());
        final CreateIndexResponse createIndexResponse
            = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        return createIndexResponse.isAcknowledged();
    }

    @Override
    public boolean save(Message message) throws IOException {
        final IndexRequest indexRequest = new IndexRequest(Constant.INDEX_NAME);
        indexRequest.source(
            Message.FIELD_DATETIME, message.getDatetime().atZone(this.zoneId).format(this.dateTimeFormatter),
            Message.FIELD_SEND, message.getSend().getAddress().getHostAddress(),
            Message.FIELD_RECIPIENT, message.getRecipient().getAddress().getHostAddress(),
            Message.FIELD_DOMAIN, message.getDomain());
        final IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        return indexResponse.getResult() == DocWriteResponse.Result.CREATED;
    }

    private Map<String, ?> indexMapping() throws IOException {
        Map<String, Object> datetime = new HashMap<>();
        datetime.put("type", "date");
        datetime.put("format", this.dateTimePattern);
        Map<String, Object> send = new HashMap<>();
        send.put("type", "ip");
        Map<String, Object> recipient = new HashMap<>();
        recipient.put("type", "ip");
        Map<String, Object> domain = new HashMap<>();
        domain.put("type", "text");
        Map<String, Object> properties = new HashMap<>();
        properties.put(Message.FIELD_DATETIME, datetime);
        properties.put(Message.FIELD_SEND, send);
        properties.put(Message.FIELD_RECIPIENT, recipient);
        properties.put(Message.FIELD_DOMAIN, domain);
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        return mapping;
    }
}
