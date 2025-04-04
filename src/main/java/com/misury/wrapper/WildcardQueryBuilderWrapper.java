package com.misury.wrapper;

import org.elasticsearch.index.query.WildcardQueryBuilder;

/**
 * 通配符查询构建器包装类
 * 用于构建Elasticsearch的通配符查询，支持使用通配符进行模式匹配
 * 支持的通配符：
 * - *：匹配零个或多个字符
 * - ?：匹配一个字符
 *
 * @author misury
 * @see BaseQueryBuilder
 * @see WildcardQueryBuilder
 */
public class WildcardQueryBuilderWrapper extends BaseQueryBuilder<WildcardQueryBuilderWrapper> {
    private final WildcardQueryBuilder wildcardQueryBuilder;

    /**
     * 创建通配符查询构建器
     *
     * @param fieldName 要查询的字段名称
     * @param pattern 通配符模式
     * @throws IllegalArgumentException 当字段名或模式为空时抛出
     * @example
     *   new WildcardQueryBuilderWrapper("title", "intro*")
     *   new WildcardQueryBuilderWrapper("email", "*@gmail.com")
     */
    public WildcardQueryBuilderWrapper(String fieldName, String pattern) {
        validateFieldName(fieldName);
        validateValue(pattern);
        this.wildcardQueryBuilder = new WildcardQueryBuilder(fieldName, pattern);
        this.queryBuilder = wildcardQueryBuilder;
    }

    /**
     * 设置重写方法
     * 用于优化查询性能的重写策略
     *
     * @param rewriteMethod 重写方法名称
     * @return 当前构建器实例
     */
    public WildcardQueryBuilderWrapper rewrite(String rewriteMethod) {
        wildcardQueryBuilder.rewrite(rewriteMethod);
        return this;
    }
}

