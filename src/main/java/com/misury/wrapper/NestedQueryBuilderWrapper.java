package com.misury.wrapper;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

/**
 * 嵌套查询构建器包装类
 * 用于构建Elasticsearch的嵌套查询，支持对嵌套类型的文档进行查询
 * 适用于处理包含嵌套对象的文档结构
 *
 * @author misury
 * @see BaseQueryBuilder
 * @see NestedQueryBuilder
 */
public class NestedQueryBuilderWrapper extends BaseQueryBuilder<NestedQueryBuilderWrapper> {
    private final NestedQueryBuilder nestedQueryBuilder;

    /**
     * 创建嵌套查询构建器（使用默认评分模式）
     *
     * @param path 嵌套对象的路径
     * @param query 要在嵌套对象上执行的查询
     * @throws IllegalArgumentException 当路径或查询为空时抛出
     */
    public NestedQueryBuilderWrapper(String path, QueryBuilder query) {
        validatePath(path);
        validateQuery(query);
        this.nestedQueryBuilder = new NestedQueryBuilder(path, query, ScoreMode.None);
        this.queryBuilder = nestedQueryBuilder;
    }

    /**
     * 创建嵌套查询构建器（指定评分模式）
     *
     * @param path 嵌套对象的路径
     * @param query 要在嵌套对象上执行的查询
     * @param scoreMode 评分模式，控制如何计算嵌套文档的得分
     * @throws IllegalArgumentException 当路径或查询为空时抛出
     */
    public NestedQueryBuilderWrapper(String path, QueryBuilder query, ScoreMode scoreMode) {
        validatePath(path);
        validateQuery(query);
        this.nestedQueryBuilder = new NestedQueryBuilder(path, query, scoreMode);
        this.queryBuilder = nestedQueryBuilder;
    }

    /**
     * 设置内部命中
     * 用于返回匹配的嵌套文档详情
     *
     * @param name 内部命中的名称
     * @return 当前构建器实例
     */
    public NestedQueryBuilderWrapper innerHit(String name) {
        nestedQueryBuilder.innerHit(new InnerHitBuilder(name));
        return this;
    }

    /**
     * 设置内部命中（使用自定义构建器）
     *
     * @param innerHitBuilder 内部命中构建器
     * @return 当前构建器实例
     */
    public NestedQueryBuilderWrapper innerHit(InnerHitBuilder innerHitBuilder) {
        nestedQueryBuilder.innerHit(innerHitBuilder);
        return this;
    }
}

