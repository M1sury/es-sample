package com.misury.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.misury.exception.ElasticsearchOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 索引操作实现类
 * 提供索引级别的管理操作，包括创建、删除、更新设置、重建索引等功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IndexOperations {
    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;

    /**
     * 创建索引
     *
     * @param index    索引名称
     * @param mapping  索引映射配置
     * @param settings 索引设置
     * @return 是否创建成功
     * @throws ElasticsearchOperationException 当创建索引操作失败时抛出
     */
    public boolean createIndex(String index, Map<String, Object> mapping, Map<String, Object> settings) {
        try {
            CreateIndexRequest request = new CreateIndexRequest(index);

            if (mapping != null && !mapping.isEmpty()) {
                request.mapping(objectMapper.writeValueAsString(mapping), XContentType.JSON);
            }

            if (settings != null && !settings.isEmpty()) {
                request.settings(settings);
            }

            CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (Exception e) {
            throw new ElasticsearchOperationException("创建索引失败: " + index, e);
        }
    }

    /**
     * 检查索引是否存在
     *
     * @param index 索引名称
     * @return 索引是否存在
     * @throws ElasticsearchOperationException 当检查索引操作失败时抛出
     */
    public boolean indexExists(String index) {
        try {
            GetIndexRequest request = new GetIndexRequest(index);
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchOperationException("检查索引存在失败: " + index, e);
        }
    }

    /**
     * 删除索引
     *
     * @param index 索引名称
     * @return 是否删除成功
     * @throws ElasticsearchOperationException 当删除索引操作失败时抛出
     */
    public boolean deleteIndex(String index) {
        try {
            DeleteIndexRequest request = new DeleteIndexRequest(index);
            return client.indices().delete(request, RequestOptions.DEFAULT).isAcknowledged();
        } catch (Exception e) {
            throw new ElasticsearchOperationException("删除索引失败: " + index, e);
        }
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
        try {
            UpdateSettingsRequest request = new UpdateSettingsRequest(index);
            request.settings(settings);
            return client.indices().putSettings(request, RequestOptions.DEFAULT).isAcknowledged();
        } catch (Exception e) {
            throw new ElasticsearchOperationException("更新索引设置失败: " + index, e);
        }
    }

    /**
     * 获取索引信息
     *
     * @param index 索引名称
     * @return GetIndexResponse 包含索引信息的响应对象
     * @throws ElasticsearchOperationException 当获取索引信息操作失败时抛出
     */
    public GetIndexResponse getIndex(String index) {
        try {
            GetIndexRequest request = new GetIndexRequest(index);
            return client.indices().get(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchOperationException("获取索引信息失败: " + index, e);
        }
    }

    /**
     * 重建索引
     * 创建新索引并将数据从旧索引复制过去
     *
     * @param sourceIndex 源索引名称
     * @param targetIndex 目标索引名称
     * @param mapping    新的映射配置（可选）
     * @param settings   新的索引设置（可选）
     * @throws ElasticsearchOperationException 当重建索引操作失败时抛出
     */
    public void reindex(String sourceIndex, String targetIndex,
                       Map<String, Object> mapping, Map<String, Object> settings) {
        try {
            // 创建新索引
            createIndex(targetIndex, mapping, settings);

            // 执行reindex操作
            ReindexRequest request = new ReindexRequest();
            request.setSourceIndices(sourceIndex);
            request.setDestIndex(targetIndex);
            request.setSourceBatchSize(1000);

            client.reindex(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchOperationException("重建索引失败", e);
        }
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
        try {
            ForceMergeRequest request = new ForceMergeRequest(index);
            request.maxNumSegments(maxNumSegments);
            client.indices().forcemerge(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchOperationException("优化索引失败: " + index, e);
        }
    }

    /**
     * 刷新索引
     * 使最近的更改对搜索可见
     *
     * @param index 索引名称
     * @throws ElasticsearchOperationException 当刷新索引操作失败时抛出
     */
    public void refreshIndex(String index) {
        try {
            RefreshRequest request = new RefreshRequest(index);
            client.indices().refresh(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchOperationException("刷新索引失败: " + index, e);
        }
    }
}
