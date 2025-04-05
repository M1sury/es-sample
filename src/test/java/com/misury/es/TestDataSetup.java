package com.misury.es;

import com.misury.dto.AppInfo;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestDataSetup {
    private static final String INDEX_NAME = "es-index.posts";
    
    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void setupTestData() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        
        // 创建测试文档
        List<Map<String, Object>> documents = Arrays.asList(
            createDocument(
                "doc1",
                Arrays.asList(
                    createAppInfo("CONT001", "张三", "C001"),
                    createAppInfo("CONT002", "李四", "C002")
                ),
                "测试文档1",
                "示例内容1",
                Arrays.asList("ID001", "ID002"),
                "2024-01-01"
            ),
            createDocument(
                "doc2",
                Arrays.asList(
                    createAppInfo("CONT003", "John Smith", "C003"),
                    createAppInfo("CONT004", "Mary Johnson", "C004")
                ),
                "Test Document",
                "Example Content",
                Arrays.asList("ID003", "ID004"),
                "2024-01-15"
            ),
            createDocument(
                "doc3",
                Arrays.asList(
                    createAppInfo("CONT005", "王五", "C012345"),
                    createAppInfo("CONT006", "赵六", "C067890")
                ),
                "测试文档3",
                "模糊搜索测试",
                Arrays.asList("ID005", "ID006"),
                "2024-02-01"
            )
        );

        // 添加到批量请求
        for (Map<String, Object> document : documents) {
            bulkRequest.add(new IndexRequest(INDEX_NAME)
                .source(document, XContentType.JSON));
        }

        // 执行批量索引
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        if (bulkResponse.hasFailures()) {
            throw new RuntimeException("批量索引失败: " + bulkResponse.buildFailureMessage());
        }
    }

    private Map<String, Object> createDocument(
            String id,
            List<Map<String, Object>> appInfoList,
            String field1,
            String field2,
            List<String> idNoList,
            String createTime
    ) {
        Map<String, Object> document = new HashMap<>();
        document.put("appInfo", appInfoList);
        document.put("field1", field1);
        document.put("field2", field2);
        document.put("idNoList", idNoList);
        document.put("createTime", createTime);
        return document;
    }

    private Map<String, Object> createAppInfo(String contNo, String custName, String custNo) {
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("contNo", contNo);
        appInfo.put("custName", custName);
        appInfo.put("custNo", custNo);
        return appInfo;
    }
}