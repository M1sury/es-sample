package com.misury.wrapper;

import org.elasticsearch.index.query.QueryBuilder;

/**
 * 查询构建器基类
 * 为所有Elasticsearch查询构建器提供通用功能和链式调用支持
 *
 * @param <T> 具体的查询构建器类型，用于支持方法链式调用
 * @author misury
 */
public abstract class BaseQueryBuilder<T extends BaseQueryBuilder<T>> {
    /** 底层的Elasticsearch查询构建器 */
    protected QueryBuilder queryBuilder;

    /** 查询提升因子，用于调整查询的相关性得分 */
    protected Float boost;

    /** 查询的名称，用于标识特定的查询 */
    protected String name;

    /**
     * 获取当前实例，用于支持子类的链式调用
     *
     * @return 当前查询构建器实例
     */
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    /**
     * 设置查询的提升因子
     * 提升因子用于调整查询的相关性得分，值越大表示越重要
     *
     * @param boost 提升因子值，必须大于0
     * @return 当前查询构建器实例
     */
    public T boost(float boost) {
        this.boost = boost;
        return self();
    }

    /**
     * 设置查询的名称
     * 查询名称用于在复杂查询中标识特定的查询部分
     *
     * @param name 查询名称
     * @return 当前查询构建器实例
     */
    public T name(String name) {
        this.name = name;
        return self();
    }

    /**
     * 构建最终的查询对象
     * 应用所有配置的参数（boost、name等）并返回Elasticsearch原生查询构建器
     *
     * @return 配置完成的查询构建器
     */
    public QueryBuilder build() {
        if (boost != null) {
            queryBuilder.boost(boost);
        }
        if (name != null) {
            queryBuilder.queryName(name);
        }
        return queryBuilder;
    }

    /**
     * 验证字段名称是否有效
     *
     * @param fieldName 要验证的字段名称
     * @throws IllegalArgumentException 当字段名称为null或空时抛出
     */
    protected static void validateFieldName(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new IllegalArgumentException("字段名称不能为空");
        }
    }

    /**
     * 验证查询值是否有效
     *
     * @param value 要验证的查询值
     * @throws IllegalArgumentException 当值为null时抛出
     */
    protected static void validateValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("查询值不能为空");
        }
    }

    /**
     * 验证嵌套查询路径是否有效
     *
     * @param path 要验证的嵌套路径
     * @throws IllegalArgumentException 当路径为null或空时抛出
     */
    protected static void validatePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("嵌套路径不能为空");
        }
    }

    /**
     * 验证查询构建器是否有效
     *
     * @param query 要验证的查询构建器
     * @throws IllegalArgumentException 当查询构建器为null时抛出
     */
    protected static void validateQuery(QueryBuilder query) {
        if (query == null) {
            throw new IllegalArgumentException("查询条件不能为空");
        }
    }
}

