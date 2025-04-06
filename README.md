# Elasticsearch Java API Demo

基于 Elasticsearch 7.4.2 的 Java API 示例项目，提供完整的 Elasticsearch 操作封装和使用示例。

## 功能特性

- 文档操作：增删改查、批量处理
- 索引管理：创建、删除、更新配置
- 高级查询：Term、Match、Range、Bool、Nested等
- 全文搜索：Fuzzy、Wildcard查询
- 相关性优化：Boost、Function Score
- 聚合分析：统计、分组、指标计算

## 项目结构

```
es-sample/
├── src/
│   ├── main/
│   │   ├── java/com/misury/
│   │   │   ├── config/            # 配置类
│   │   │   │   └── ElasticsearchConfig.java
│   │   │   ├── operation/         # 基础操作实现
│   │   │   │   ├── DocumentOperations.java
│   │   │   │   ├── IndexOperations.java
│   │   │   │   └── BulkOperations.java
│   │   │   ├── service/          # 业务服务封装
│   │   │   │   └── ElasticsearchService.java
│   │   │   └── utils/            # 工具类
│   │   │       └── ElasticsearchQueryUtils.java
│   │   └── resources/
│   └── test/                     # 测试用例和示例
├── mapping/                      # 索引映射配置
└── data/                        # 测试数据
```

## 快速开始

### 1. 环境要求

- JDK 8+
- Maven 3.6+
- Elasticsearch 7.4.2

### 2. 配置说明

项目使用 Java 配置类方式配置 Elasticsearch 客户端。主要配置在 `ElasticsearchConfig.java` 中：

```java
@Configuration
public class ElasticsearchConfig {
    
    @Bean
    public RestHighLevelClient restClient() {
        return new RestHighLevelClient(
            RestClient.builder(
                new HttpHost("localhost", 9200, "http")
            )
        );
    }
}
```

如需修改连接配置，直接修改 `ElasticsearchConfig` 类中的相关参数：

- hosts：Elasticsearch 服务器地址
- port：服务端口
- scheme：连接协议（http/https）

对于生产环境，建议添加以下配置：

```java
@Configuration
public class ElasticsearchConfig {
    
    @Bean
    public RestHighLevelClient restClient() {
        // 配置多个节点
        RestClientBuilder builder = RestClient.builder(
            new HttpHost("es-node1", 9200, "http"),
            new HttpHost("es-node2", 9200, "http")
        );
        
        // 配置连接参数
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(5000);
            requestConfigBuilder.setSocketTimeout(60000);
            requestConfigBuilder.setConnectionRequestTimeout(0);
            return requestConfigBuilder;
        });
        
        // 配置认证信息（如果需要）
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials("elastic", "your_password"));
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            return httpClientBuilder;
        });
        
        return new RestHighLevelClient(builder);
    }
}
```

### 3. 基础使用示例

#### 文档操作

```java
@Autowired
private ElasticsearchService esService;

// 索引文档
Post post = new Post();
post.setTitle("测试文档");
post.setContent("这是一个测试文档内容");
String docId = esService.index("posts", post);

// 更新文档
post.setContent("更新后的内容");
esService.update("posts", docId, post);

// 获取文档
GetResponse response = esService.get("posts", docId);
Map<String, Object> source = response.getSourceAsMap();

// 删除文档
esService.delete("posts", docId);
```

#### 批量操作

```java
// 批量索引
List<Post> posts = Arrays.asList(
    new Post("标题1", "内容1"),
    new Post("标题2", "内容2")
);
BulkResponse bulkResponse = esService.bulkIndex("posts", posts);

// 批量更新
Map<String, Post> updates = new HashMap<>();
updates.put("doc1", new Post("更新1", "新内容1"));
updates.put("doc2", new Post("更新2", "新内容2"));
esService.bulkUpdate("posts", updates);
```

#### 搜索查询

```java
// Term查询
QueryBuilder termQuery = QueryBuilders.termQuery("title", "测试");
SearchResponse response = esService.search("posts", termQuery);

// Bool查询
BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
    .must(QueryBuilders.matchQuery("title", "测试"))
    .filter(QueryBuilders.rangeQuery("createTime").gte("2024-01-01"))
    .should(QueryBuilders.termQuery("tags", "java"));
SearchResponse response = esService.search("posts", boolQuery);

// 嵌套查询
QueryBuilder nestedQuery = QueryBuilders.nestedQuery(
    "appInfo",
    QueryBuilders.termQuery("appInfo.custNo", "C012345"),
    ScoreMode.None
);
SearchResponse response = esService.search("posts", nestedQuery);
```

#### 相关性优化

```java
// 字段权重提升
QueryBuilder boostQuery = QueryBuilders.multiMatchQuery("搜索关键词")
    .field("title", 3.0f)    // title字段权重为3
    .field("content", 1.0f); // content字段权重为1

// Function Score查询
FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
    QueryBuilders.matchQuery("title", "搜索关键词"),
    ScoreFunctionBuilders.exponentialDecayFunction("createTime", "now", "30d")
);
```

## 高级特性

### 1. 索引管理

```java
// 创建索引
Map<String, Object> mapping = new HashMap<>();
// 设置映射配置...
Map<String, Object> settings = new HashMap<>();
// 设置索引配置...
esService.createIndex("new_index", mapping, settings);

// 更新索引设置
Settings settings = Settings.builder()
    .put("index.number_of_replicas", 2)
    .build();
esService.updateIndexSettings("posts", settings);

// 重建索引
esService.reindex("old_index", "new_index", newMapping, newSettings);
```

### 2. 聚合分析

```java
// 构建聚合查询
SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
sourceBuilder.aggregation(AggregationBuilders
    .terms("by_tag")
    .field("tags")
    .subAggregation(AggregationBuilders
        .avg("avg_score")
        .field("score")
    )
);

SearchResponse response = esService.search("posts", sourceBuilder);
```

## 最佳实践

1. **异常处理**
   - 使用统一的异常处理机制
   - 适当的日志记录
   - 优雅降级策略

```java
try {
    esService.index("posts", document);
} catch (ElasticsearchOperationException e) {
    log.error("索引文档失败", e);
    // 实现降级逻辑
}
```

2. **性能优化**
   - 批量操作代替单条操作
   - 合理设置分片数
   - 使用过滤器减少评分计算

3. **索引设计**
   - 合理规划字段类型
   - 适当使用嵌套对象
   - 控制字段数量

## 测试

项目包含完整的单元测试和集成测试用例，位于 `src/test/java/com/misury/es/` 目录下：

- `DocumentOperationsTest.java` - 文档操作测试
- `IndexOperationsTest.java` - 索引管理测试
- `SearchQueryTest.java` - 搜索查询测试
- `BulkOperationsTest.java` - 批量操作测试

运行测试：

```bash
mvn test
```

## 常见问题

1. **连接超时**
   - 检查 Elasticsearch 服务是否正常运行
   - 确认网络连接和防火墙设置
   - 适当调整超时配置

2. **查询无结果**
   - 检查索引名称是否正确
   - 验证查询条件是否合适
   - 查看分词器设置

3. **性能问题**
   - 优化查询语句
   - 调整索引设置
   - 使用批量操作

## 版本兼容性

- Elasticsearch: 7.4.2
- Spring Boot: 2.3.12.RELEASE
- Java: 8+

## 贡献指南

1. Fork 项目
2. 创建特性分支
3. 提交变更
4. 推送到分支
5. 创建 Pull Request

## 许可证

[MIT License](LICENSE)
