package com.misury.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.misury.exception.ElasticsearchOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档操作实现类
 * 提供单个文档的索引、更新、删除、查询等基本操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentOperations {
    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;

    /**
     * 索引文档（自动生成文档ID）
     *
     * @param index    索引名称
     * @param document 要索引的文档对象
     * @param <T>      文档类型
     * @return 生成的文档ID
     * @throws ElasticsearchOperationException 当索引操作失败时抛出
     */
    public <T> String index(String index, T document) {
        try {
            String source = objectMapper.writeValueAsString(document);
            IndexRequest request = new IndexRequest(index)
                .source(source, XContentType.JSON);
            return client.index(request, RequestOptions.DEFAULT).getId();
        } catch (Exception e) {
            throw new ElasticsearchOperationException("索引文档失败", e);
        }
    }

    /**
     * 索引文档（指定文档ID）
     *
     * @param index    索引名称
     * @param id       文档ID
     * @param document 要索引的文档对象
     * @param <T>      文档类型
     * @throws ElasticsearchOperationException 当索引操作失败时抛出
     */
    public <T> void index(String index, String id, T document) {
        try {
            String source = objectMapper.writeValueAsString(document);
            IndexRequest request = new IndexRequest(index)
                .id(id)
                .source(source, XContentType.JSON);
            client.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchOperationException("索引文档失败", e);
        }
    }

    /**
     * 更新文档
     *
     * @param index    索引名称
     * @param id       文档ID
     * @param document 新的文档内容
     * @param <T>      文档类型
     * @throws ElasticsearchOperationException 当更新操作失败时抛出
     */
    public <T> void update(String index, String id, T document) {
        try {
            String source = objectMapper.writeValueAsString(document);
            UpdateRequest request = new UpdateRequest(index, id)
                .doc(source, XContentType.JSON)
                .docAsUpsert(true);
            client.update(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchOperationException("更新文档失败", e);
        }
    }

    /**
     * 删除文档
     *
     * @param index 索引名称
     * @param id    要删除的文档ID
     * @throws ElasticsearchOperationException 当删除操作失败时抛出
     */
    public void delete(String index, String id) {
        try {
            DeleteRequest request = new DeleteRequest(index, id);
            client.delete(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchOperationException("删除文档失败", e);
        }
    }

    /**
     * 获取单个文档
     *
     * @param index 索引名称
     * @param id    文档ID
     * @return GetResponse 包含文档内容的响应对象
     * @throws ElasticsearchOperationException 当获取操作失败时抛出
     */
    public GetResponse get(String index, String id) {
        try {
            GetRequest request = new GetRequest(index, id);
            return client.get(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchOperationException("获取文档失败", e);
        }
    }

    /**
     * 批量获取文档
     *
     * @param index 索引名称
     * @param ids   文档ID列表
     * @return MultiGetResponse 包含多个文档内容的响应对象
     * @throws ElasticsearchOperationException 当批量获取操作失败时抛出
     */
    public MultiGetResponse multiGet(String index, List<String> ids) {
        try {
            MultiGetRequest request = new MultiGetRequest();
            for (String id : ids) {
                request.add(new MultiGetRequest.Item(index, id));
            }
            return client.mget(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchOperationException("批量获取文档失败", e);
        }
    }

    /**
     * 执行搜索查询
     *
     * @param index        索引名称
     * @param queryBuilder 查询构建器
     * @return SearchResponse 搜索结果响应对象
     * @throws ElasticsearchOperationException 当搜索操作失败时抛出
     */
    public SearchResponse search(String index, QueryBuilder queryBuilder) {
        return search(index, new SearchSourceBuilder().query(queryBuilder));
    }

    /**
     * 执行高级搜索查询
     *
     * @param index          索引名称
     * @param sourceBuilder  搜索源构建器，可以设置查询、排序、分页等
     * @return SearchResponse 搜索结果响应对象
     * @throws ElasticsearchOperationException 当搜索操作失败时抛出
     */
    public SearchResponse search(String index, SearchSourceBuilder sourceBuilder) {
        try {
            SearchRequest request = new SearchRequest(index);
            request.source(sourceBuilder);
            return client.search(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchOperationException("搜索文档失败", e);
        }
    }
}
