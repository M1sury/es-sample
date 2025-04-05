package com.misury.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.misury.util.ElasticsearchQueryUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@DisplayName("Elasticsearch查询构建器测试")
public class WrapperTest {
    private static final String INDEX_NAME = "es-index.posts";

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Test
    @DisplayName("通配符查询 - 测试文档标题匹配")
    public void testWildcardQueryWrapper() throws IOException {
        // 测试通配符查询 - 查找标题中包含"文档"的记录
        QueryBuilder wildcardQuery = ElasticsearchQueryUtils.wildcard("field1.keyword", "*文档*")  // 使用.keyword
                .boost(2.0f)
                .build();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(wildcardQuery);
        // 添加调试日志
        log.info("Query DSL: {}", searchSourceBuilder);

        SearchResponse response = executeQuery(wildcardQuery);
        assertSearchResponse(response, hit -> {
            Map<String, Object> source = hit.getSourceAsMap();
            String field1 = (String) source.get("field1");
            log.info("Found document with field1: {}", field1);  // 添加调试日志
            Assert.assertTrue("文档标题应包含'文档'关键字", field1.contains("文档"));
        });
    }

    @Test
    @DisplayName("嵌套查询 - 测试客户编号精确匹配")
    public void testNestedQueryWrapper() throws IOException {
        String targetCustNo = "C001"; // 修改为实际存在的客户编号

        // 修改查询路径，使用完整的嵌套路径
        QueryBuilder termQuery = ElasticsearchQueryUtils.term("appInfo.custNo.keyword", targetCustNo);
        QueryBuilder nestedQuery = ElasticsearchQueryUtils.nested("appInfo", termQuery, ScoreMode.None)
                .innerHit("app_hits")
                .build();

        // 添加调试日志
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(nestedQuery);
        log.info("Nested Query DSL: {}", searchSourceBuilder);

        SearchResponse response = executeQuery(nestedQuery);
        assertSearchResponse(response, (SearchHit hit) -> {
            Map<String, Object> source = hit.getSourceAsMap();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> appInfoList = (List<Map<String, Object>>) source.get("appInfo");
            log.info("Found document with appInfo: {}", appInfoList); // 添加调试日志

            Assert.assertTrue("应至少包含一个匹配的客户编号",
                    appInfoList.stream().anyMatch(info -> targetCustNo.equals(info.get("custNo"))));
        });
    }

    @Test
    @DisplayName("模糊查询 - 测试客户姓名近似匹配")
    public void testFuzzyQueryWrapper() throws IOException {
        String searchName = "张三";

        // 创建模糊查询，使用正确的嵌套路径
        QueryBuilder fuzzyQuery = ElasticsearchQueryUtils.defaultFuzzy("appInfo.custName.keyword", searchName)
                .boost(1.5f)
                .build();

        // 包装成嵌套查询
        QueryBuilder nestedQuery = ElasticsearchQueryUtils.nested("appInfo", fuzzyQuery, ScoreMode.None)
                .innerHit("app_hits")
                .build();

        // 添加调试日志
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(nestedQuery);
        log.info("Nested Fuzzy Query DSL: {}", searchSourceBuilder);

        SearchResponse response = executeQuery(nestedQuery);
        assertSearchResponse(response, hit -> {
            Map<String, Object> source = hit.getSourceAsMap();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> appInfoList = (List<Map<String, Object>>) source.get("appInfo");

            // 添加调试日志
            log.info("Found document with appInfo: {}", appInfoList);

            Assert.assertTrue("应包含相似的客户姓名",
                    appInfoList.stream().anyMatch(info -> {
                        String custName = (String) info.get("custName");
                        int distance = levenshteinDistance(custName, searchName);
                        log.info("Comparing '{}' with '{}', distance: {}", custName, searchName, distance);
                        return distance <= 2;
                    }));
        });
    }

    @Test
    @DisplayName("日期范围查询 - 测试指定时间段内的文档")
    public void testRangeQueryWrapper() throws IOException {
        String startDate = "2024-01-01";
        String endDate = "2024-01-15";

        QueryBuilder rangeQuery = ElasticsearchQueryUtils.dateRange("createTime")
                .from(startDate, true)
                .to(endDate, true)
                .format("yyyy-MM-dd")
                .timeZone("+08:00")
                .build();

        SearchResponse response = executeQuery(rangeQuery);
        assertSearchResponse(response, hit -> {
            Map<String, Object> source = parseSource(hit);
            String createTime = (String) source.get("createTime");
            Assert.assertTrue("创建时间应在指定范围内",
                    createTime.compareTo(startDate) >= 0 && createTime.compareTo(endDate) <= 0);
        });
    }

    @Test
    @DisplayName("复合查询 - 测试多条件组合查询")
    public void testCombinedQuery() throws IOException {
        // 1. 修改通配符查询，使用keyword子字段
        QueryBuilder wildcardQuery = ElasticsearchQueryUtils.wildcard("field1.keyword", "*测试*").build();

        // 2. 日期范围查询添加格式化
        QueryBuilder rangeQuery = ElasticsearchQueryUtils.dateRange("createTime")
                .from("2024-01-01", true)
                .to("2024-02-01", true)
                .format("yyyy-MM-dd")
                .timeZone("+08:00")
                .build();

        // 3. 修改嵌套查询中的term查询路径
        QueryBuilder termQuery = ElasticsearchQueryUtils.term("appInfo.custNo.keyword", "C012345");
        QueryBuilder nestedQuery = ElasticsearchQueryUtils.nested("appInfo", termQuery, ScoreMode.None)
                .innerHit("app_hits")
                .build();

        // 4. 添加调试日志
        QueryBuilder boolQuery = ElasticsearchQueryUtils.bool()
                .must(wildcardQuery)
                .must(rangeQuery)
                .must(nestedQuery)
                .build();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQuery);
        log.info("Combined Query DSL: {}", searchSourceBuilder);

        SearchResponse response = executeQuery(boolQuery);
        assertSearchResponse(response, hit -> {
            Map<String, Object> source = parseSource(hit);
            String field1 = (String) source.get("field1");
            String createTime = (String) source.get("createTime");

            log.info("Found document - field1: {}, createTime: {}", field1, createTime);

            Assert.assertTrue("标题应包含'测试'关键字", field1.contains("测试"));
            Assert.assertTrue("创建时间应在指定范围内",
                    createTime.compareTo("2024-01-01") >= 0 &&
                            createTime.compareTo("2024-02-01") <= 0);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> appInfoList = (List<Map<String, Object>>) source.get("appInfo");
            log.info("Found appInfo: {}", appInfoList);

            Assert.assertTrue("应包含指定的客户编号",
                    appInfoList.stream().anyMatch(info -> "C012345".equals(info.get("custNo"))));
        });
    }

    @Test
    @DisplayName("排序和分页查询 - 测试结果排序和分页功能")
    public void testSortAndPagination() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 1. 添加一些基本过滤条件，避免匹配所有数据
        QueryBuilder rangeQuery = ElasticsearchQueryUtils.dateRange("createTime")
                .from("2024-01-01",true)
                .to("2024-12-31",true)
                .format("yyyy-MM-dd")
                .timeZone("+08:00")
                .build();

        searchSourceBuilder.query(rangeQuery);

        // 2. 添加多字段排序
        searchSourceBuilder.sort("createTime", SortOrder.DESC);
        searchSourceBuilder.sort("_score", SortOrder.DESC);  // 次要排序字段

        // 3. 设置分页参数
        int pageSize = 3;
        int pageNum = 1;
        int from = (pageNum - 1) * pageSize;
        searchSourceBuilder.from(from).size(pageSize);

        // 4. 添加调试日志
        log.info("Search query DSL: {}", searchSourceBuilder);

        SearchResponse response = executeQuery(searchSourceBuilder);
        SearchHits hits = response.getHits();

        // 5. 增强断言和日志
        Assert.assertTrue("总命中数应大于0", hits.getTotalHits().value > 0);
        log.info("Total hits: {}, returned size: {}", hits.getTotalHits().value, hits.getHits().length);
        Assert.assertTrue("返回结果数不应超过页面大小", hits.getHits().length <= pageSize);

        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String createTime = parseSourceField(hit, "createTime");
            log.info("Document createTime: {}, score: {}", createTime, hit.getScore());
        }

        // 6. 验证排序顺序
        if (searchHits.length > 1) {
            for (int i = 0; i < searchHits.length - 1; i++) {
                String currentDate = parseSourceField(searchHits[i], "createTime");
                String nextDate = parseSourceField(searchHits[i + 1], "createTime");

                Assert.assertTrue(
                    String.format("排序错误: %s 应该大于 %s", currentDate, nextDate),
                    currentDate.compareTo(nextDate) >= 0
                );
            }
        }
    }

    @Test
    @DisplayName("聚合查询 - 测试字段值聚合")
    public void testAggregationQuery() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 构建聚合查询
        searchSourceBuilder.aggregation(
                ElasticsearchQueryUtils.terms("field1_agg")
                        .field("field1.keyword")
                        .size(10)
        );

        // 设置不返回搜索结果，只返回聚合结果
        searchSourceBuilder.size(0);

        SearchResponse response = executeQuery(searchSourceBuilder);

        // 获取聚合结果
        Terms terms = response.getAggregations().get("field1_agg");
        Assert.assertNotNull("聚合结果不应为空", terms);

        // 获取聚合桶
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        log.info("Found {} unique terms", buckets.size());

        // 遍历聚合桶
        for (Terms.Bucket bucket : buckets) {
            String key = bucket.getKeyAsString();      // 获取聚合字段的值
            long docCount = bucket.getDocCount();      // 获取文档数量

            log.info("Term: '{}', Count: {}", key, docCount);

            // 验证文档数量大于0
            Assert.assertTrue(
                String.format("Term '%s' 的文档数量应大于0", key),
                docCount > 0
            );
        }

        // 可选：验证特定值的存在
        if (!buckets.isEmpty()) {
            // 获取文档数量最多的term
            Terms.Bucket topBucket = buckets.get(0);
            log.info("Most common term: '{}' with {} documents",
                    topBucket.getKeyAsString(), topBucket.getDocCount());
        }
    }

    private SearchResponse executeQuery(QueryBuilder queryBuilder) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        return executeQuery(searchSourceBuilder);
    }

    private SearchResponse executeQuery(SearchSourceBuilder searchSourceBuilder) throws IOException {
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        searchRequest.source(searchSourceBuilder);

        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        log.info("Query executed, total hits: {}", response.getHits().getTotalHits().value);
        return response;
    }

    private void assertSearchResponse(SearchResponse response, SearchHitAssertion assertion) {
        Assert.assertNotNull("搜索响应不应为空", response);
        Assert.assertNotNull("搜索结果不应为空", response.getHits());
        Assert.assertTrue("应至少有一个匹配结果", response.getHits().getTotalHits().value > 0);

        for (SearchHit hit : response.getHits().getHits()) {
            log.debug("Document found: {}", hit.getSourceAsString());
            assertion.assertHit(hit);  // 修改这里
        }
    }

    @FunctionalInterface
    private interface SearchHitAssertion {
        void assertHit(SearchHit hit);
    }

    // 计算两个字符串之间的编辑距离
    private static int levenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return -1;
        }

        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1])) + 1;
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * 解析搜索结果为Map
     */
    private Map<String, Object> parseSource(SearchHit hit) {
        try {
            String sourceAsString = hit.getSourceAsString();
            if (sourceAsString == null) {
                return new HashMap<>(); // 返回空Map而不是null
            }
            return JSON.parseObject(sourceAsString, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.error("解析搜索结果失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 从搜索结果中获取指定字段的值
     */
    private String parseSourceField(SearchHit hit, String fieldName) {
        Map<String, Object> source = parseSource(hit);
        return source.get(fieldName) != null ? source.get(fieldName).toString() : null;
    }
}
