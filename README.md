# Elasticsearch Java API Demo

基于 Elasticsearch 7.4.2 的 Java API 示例项目，展示常用查询和操作。

## 项目结构

- `mapping/` - Elasticsearch 索引映射配置
- `data/es-index.posts.json` - 测试数据集

## 主要功能演示

- 基础查询（Term、Match、Range等）
- 复合查询（Bool Query）
- 全文搜索（Fuzzy、Wildcard）
- 嵌套查询（Nested Query）
- 聚合分析
- 相关性优化（Boost）

## 快速开始

1. 确保 Elasticsearch 7.4.2 正在运行
2. 导入索引映射：`mapping/`
3. 导入测试数据：`data/es-index.posts.json`
4. 运行测试用例查看示例

## 技术栈

- Spring Boot
- Elasticsearch Java REST High Level Client
- JUnit 5
