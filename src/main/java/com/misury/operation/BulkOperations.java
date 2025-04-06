package com.misury.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.misury.exception.ElasticsearchOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Elasticsearch批量操作服务类
 * 提供批量索引、更新和删除文档的功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BulkOperations {
    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;

    /**
     * 批量索引文档
     *
     * @param index     索引名称
     * @param documents 待索引的文档列表
     * @param <T>       文档类型
     * @return BulkResponse 批量操作的响应结果
     * @throws ElasticsearchOperationException 当批量索引操作失败时抛出
     */
    public <T> BulkResponse bulkIndex(String index, List<T> documents) {
        try {
            BulkRequest request = new BulkRequest();
            for (T document : documents) {
                String source = objectMapper.writeValueAsString(document);
                request.add(new IndexRequest(index)
                    .source(source, XContentType.JSON));
            }
            return client.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchOperationException("批量索引文档失败", e);
        }
    }

    /**
     * 批量更新文档
     *
     * @param index     索引名称
     * @param documents 文档ID和文档内容的映射，key为文档ID，value为文档内容
     * @param <T>       文档类型
     * @return BulkResponse 批量操作的响应结果
     * @throws ElasticsearchOperationException 当批量更新操作失败时抛出
     */
    public <T> BulkResponse bulkUpdate(String index, Map<String, T> documents) {
        try {
            BulkRequest request = new BulkRequest();
            for (Map.Entry<String, T> entry : documents.entrySet()) {
                String source = objectMapper.writeValueAsString(entry.getValue());
                request.add(new UpdateRequest(index, entry.getKey())
                    .doc(source, XContentType.JSON)
                    .docAsUpsert(true));
            }
            return client.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchOperationException("批量更新文档失败", e);
        }
    }

    /**
     * 批量删除文档
     *
     * @param index 索引名称
     * @param ids   要删除的文档ID列表
     * @return BulkResponse 批量操作的响应结果
     * @throws ElasticsearchOperationException 当批量删除操作失败时抛出
     */
    public BulkResponse bulkDelete(String index, List<String> ids) {
        try {
            BulkRequest request = new BulkRequest();
            for (String id : ids) {
                request.add(new DeleteRequest(index, id));
            }
            return client.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchOperationException("批量删除文档失败", e);
        }
    }
}
