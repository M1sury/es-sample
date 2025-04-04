package com.misury.es;

import com.alibaba.fastjson.JSON;
import com.misury.dto.AppInfo;
import com.misury.util.ElasticsearchQueryUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RunWith(org.springframework.test.context.junit4.SpringRunner.class)
@SpringBootTest
public class EsIndexTest {
    private static final String INDEX_NAME = "es-index.posts";
    private static final String NESTED_PATH = "appInfo";

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Test
    @DisplayName("测试创建包含嵌套对象的文档")
    public void testCreateDocumentWithNestedObject() throws IOException {
        // 创建主文档数据
        Map<String, Object> document = createDocumentData();

        // 创建并添加嵌套对象
        List<AppInfo> appInfoList = createAppInfoList();
        document.put(NESTED_PATH, convertAppInfoToMapList(appInfoList));

        // 创建并发送索引请求
        IndexResponse response = indexDocument(document);
        log.info("Index Response: {}", JSON.toJSONString(response));
    }

    @Test
    @DisplayName("测试嵌套对象的通配符查询")
    public void testNestedWildcardQuery() throws IOException {
        // 构建查询
        SearchRequest searchRequest = buildNestedWildcardQuery("*012*");

        // 执行查询并处理结果
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        processSearchResponse(response);
    }

    private Map<String, Object> createDocumentData() {
        Map<String, Object> document = new HashMap<>();
        document.put("field1", "test4");
        document.put("field2", "test5");
        document.put("createTime", new Date());
        document.put("idNoList", Arrays.asList("你好", "你怎么样"));
        return document;
    }

    private List<AppInfo> createAppInfoList() {
        List<AppInfo> appInfoList = new ArrayList<>();

        AppInfo appInfo1 = new AppInfo();
        appInfo1.setContNo("123");
        appInfo1.setCustName("nihao");
        appInfo1.setCustNo("C12003405");
        appInfoList.add(appInfo1);

        AppInfo appInfo2 = new AppInfo();
        appInfo2.setContNo("456");
        appInfo2.setCustName("world");
        appInfo2.setCustNo("C6789012");
        appInfoList.add(appInfo2);

        return appInfoList;
    }

    private List<Map<String, Object>> convertAppInfoToMapList(List<AppInfo> appInfoList) {
        return appInfoList.stream()
                .map(app -> {
                    Map<String, Object> appInfoMap = new HashMap<>();
                    appInfoMap.put("contNo", app.getContNo());
                    appInfoMap.put("custName", app.getCustName());
                    appInfoMap.put("custNo", app.getCustNo());
                    return appInfoMap;
                })
                .collect(Collectors.toList());
    }

    private IndexResponse indexDocument(Map<String, Object> document) throws IOException {
        IndexRequest indexRequest = new IndexRequest(INDEX_NAME).source(document, XContentType.JSON);
        return restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    private SearchRequest buildNestedWildcardQuery(String wildcardPattern) {
        //WildcardQueryBuilder wildcardQuery = new WildcardQueryBuilder("appInfo.custNo.keyword", wildcardPattern);
        QueryBuilder build = ElasticsearchQueryUtils.wildcard("appInfo.custNo.keyword", wildcardPattern).build();

        QueryBuilder queryBuilder = ElasticsearchQueryUtils.nested("appInfo", build, ScoreMode.None).build();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);

        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        searchRequest.source(searchSourceBuilder);

        return searchRequest;
    }

    private void processSearchResponse(SearchResponse response) {
        log.info("Total hits: {}", response.getHits().getTotalHits());
        for (SearchHit hit : response.getHits().getHits()) {
            log.info("Document found: {}", hit.getSourceAsString());
        }
    }

    @Test
    @DisplayName("测试优化后的查询构建器")
    public void testImprovedQueryBuilders() throws IOException {
        // 1. 范围查询示例
        QueryBuilder rangeQuery = ElasticsearchQueryUtils.range("age")
                .from(18, true)    // 大于等于18
                .to(30, false)     // 小于30
                .boost(2.0f)
                .build();

        // 2. 模糊查询示例
        QueryBuilder fuzzyQuery = ElasticsearchQueryUtils.defaultFuzzy("name", "john")
                .boost(1.5f)
                .build();

        // 3. 通配符查询示例
        QueryBuilder wildcardQuery = ElasticsearchQueryUtils.keywordWildcard("title", "*test*")
                .build();

        // 4. 嵌套查询示例
        QueryBuilder termQuery = ElasticsearchQueryUtils.term("custNo", "12345");
        QueryBuilder nestedQuery = ElasticsearchQueryUtils.nested("appInfo", termQuery)
                .innerHit("app_hits")
                .build();

        // 5. 日期范围查询示例
        QueryBuilder dateRangeQuery = ElasticsearchQueryUtils.dateRange("createTime")
                .from("2024-01-01 00:00:00", true)
                .to("2024-12-31 23:59:59", true)
                .build();

        // 组合查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(rangeQuery)
                .should(fuzzyQuery)
                .must(wildcardQuery)
                .must(nestedQuery)
                .filter(dateRangeQuery);

        // 执行查询
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        searchRequest.source(new SearchSourceBuilder().query(boolQuery));

        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        processSearchResponse(response);
    }
}
