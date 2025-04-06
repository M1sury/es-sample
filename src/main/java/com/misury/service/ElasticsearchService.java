package com.misury.service;

import com.misury.exception.ElasticsearchOperationException;
import com.misury.operation.BulkOperations;
import com.misury.operation.DocumentOperations;
import com.misury.operation.IndexOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Elasticsearch服务类
 * 提供文档操作、索引管理和批量处理的统一接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchService {
    private final DocumentOperations documentOps;
    private final IndexOperations indexOps;
    private final BulkOperations bulkOps;

    // =============== 文档操作 ===============

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
        return documentOps.index(index, document);
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
        documentOps.index(index, id, document);
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
        documentOps.update(index, id, document);
    }

    /**
     * 删除文档
     *
     * @param index 索引名称
     * @param id    要删除的文档ID
     * @throws ElasticsearchOperationException 当删除操作失败时抛出
     */
    public void delete(String index, String id) {
        documentOps.delete(index, id);
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
        return documentOps.get(index, id);
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
        return documentOps.multiGet(index, ids);
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
        return documentOps.search(index, queryBuilder);
    }

    /**
     * 执行高级搜索查询
     *
     * @param index         索引名称
     * @param sourceBuilder 搜索源构建器，可以设置查询、排序、分页等
     * @return SearchResponse 搜索结果响应对象
     * @throws ElasticsearchOperationException 当搜索操作失败时抛出
     */
    public SearchResponse search(String index, SearchSourceBuilder sourceBuilder) {
        return documentOps.search(index, sourceBuilder);
    }

    // =============== 索引管理 ===============

    /**
     * 创建索引
     *
     * @param index    索引名称
     * @param mapping  索引映射配置，定义字段类型和属性
     * @param settings 索引设置，如分片数、副本数等
     * @return 是否创建成功
     * @throws ElasticsearchOperationException 当创建索引操作失败时抛出
     */
    public boolean createIndex(String index, Map<String, Object> mapping, Map<String, Object> settings) {
        return indexOps.createIndex(index, mapping, settings);
    }

    /**
     * 删除索引
     *
     * @param index 索引名称
     * @return 是否删除成功
     * @throws ElasticsearchOperationException 当删除索引操作失败时抛出
     */
    public boolean deleteIndex(String index) {
        return indexOps.deleteIndex(index);
    }

    /**
     * 检查索引是否存在
     *
     * @param index 索引名称
     * @return 索引是否存在
     * @throws ElasticsearchOperationException 当检查索引操作失败时抛出
     */
    public boolean indexExists(String index) {
        return indexOps.indexExists(index);
    }

    /**
     * 获取索引信息
     *
     * @param index 索引名称
     * @return GetIndexResponse 包含索引映射、设置等信息的响应对象
     * @throws ElasticsearchOperationException 当获取索引信息操作失败时抛出
     */
    public GetIndexResponse getIndex(String index) {
        return indexOps.getIndex(index);
    }

    /**
     * 更新索引设置
     *
     * @param index    索引名称
     * @param settings 新的索引设置
     * @return 是否更新成功
     * @throws ElasticsearchOperationException 当更新索引设置操作失败时抛出
     */
    public boolean updateIndexSettings(String index, Settings settings) {
        return indexOps.updateIndexSettings(index, settings);
    }

    /**
     * 重建索引
     * 创建新索引并将数据从旧索引复制过去
     *
     * @param sourceIndex 源索引名称
     * @param targetIndex 目标索引名称
     * @param mapping     新的映射配置（可选）
     * @param settings    新的索引设置（可选）
     * @throws ElasticsearchOperationException 当重建索引操作失败时抛出
     */
    public void reindex(String sourceIndex, String targetIndex, Map<String, Object> mapping, Map<String, Object> settings) {
        indexOps.reindex(sourceIndex, targetIndex, mapping, settings);
    }

    /**
     * 优化索引
     * 强制合并索引分片，减少段数量
     *
     * @param index          索引名称
     * @param maxNumSegments 最大段数
     * @throws ElasticsearchOperationException 当优化索引操作失败时抛出
     */
    public void optimizeIndex(String index, int maxNumSegments) {
        indexOps.optimizeIndex(index, maxNumSegments);
    }

    /**
     * 刷新索引
     * 使最近的更改对搜索可见
     *
     * @param index 索引名称
     * @throws ElasticsearchOperationException 当刷新索引操作失败时抛出
     */
    public void refreshIndex(String index) {
        indexOps.refreshIndex(index);
    }

    // =============== 批量操作 ===============

    /**
     * 批量索引文档
     *
     * @param index     索引名称
     * @param documents 要索引的文档列表
     * @param <T>       文档类型
     * @return BulkResponse 批量操作的响应结果
     * @throws ElasticsearchOperationException 当批量索引操作失败时抛出
     */
    public <T> BulkResponse bulkIndex(String index, List<T> documents) {
        return bulkOps.bulkIndex(index, documents);
    }

    /**
     * 批量更新文档
     *
     * @param index     索引名称
     * @param documents Map<文档ID, 文档内容>
     * @param <T>       文档类型
     * @return BulkResponse 批量操作的响应结果
     * @throws ElasticsearchOperationException 当批量更新操作失败时抛出
     */
    public <T> BulkResponse bulkUpdate(String index, Map<String, T> documents) {
        return bulkOps.bulkUpdate(index, documents);
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
        return bulkOps.bulkDelete(index, ids);
    }
}
