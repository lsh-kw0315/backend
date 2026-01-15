package team.klover.server.global.elasticsearch.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//Repository가 아니라 REST API로 호출을 시도해야할 상황이라면
@Configuration
public class ElasticSearchClientConfig {

    @Value("${es.host}")
    private String host;

    @Bean
    public ElasticsearchClient esClient() {

        // JacksonJsonpMapper 생성 (ObjectMapper 포함)
        ObjectMapper objectMapper = new ObjectMapper();

        // Java 8 날짜/시간 처리 모듈 등록
        objectMapper.registerModule(new JavaTimeModule());

        // JacksonJsonpMapper 생성
        JsonpMapper jsonpMapper = new JacksonJsonpMapper(objectMapper);


        // 1. RestClient 생성 (Elasticsearch 8.x에서 HTTP 요청을 보내는 클라이언트)
        RestClient restClient = RestClient.builder(new HttpHost(host, 9200, "http"))
                .setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.setMaxConnTotal(100).setMaxConnPerRoute(100)).build();

        // 2. ElasticsearchTransport 생성 (Jackson JSON Mapper 사용)
        ElasticsearchTransport transport = new RestClientTransport(restClient, jsonpMapper);

        // 3. ElasticsearchClient 생성 (ElasticsearchTransport 필요)
        return new ElasticsearchClient(transport);
    }
}
