package com.misury.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch配置类
 * 提供Elasticsearch客户端的Spring配置
 *
 * @author misury
 */
@Configuration
public class ElasticsearchConfig {

    /**
     * 创建并配置Elasticsearch高级别REST客户端
     * 默认连接到本地Elasticsearch实例
     *
     * @return 配置好的RestHighLevelClient实例
     */
    @Bean
    public RestHighLevelClient restClient() {
        return new RestHighLevelClient(
            RestClient.builder(
                new HttpHost("localhost", 9200, "http")
            )
        );
    }
}
