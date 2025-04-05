package com.misury.wrapper;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * Bool查询构建器包装类
 * 用于创建复合布尔查询，支持must、should、must_not和filter子句
 *
 * <p>布尔查询允许将多个查询条件组合在一起，每种子句类型的作用如下：
 * <ul>
 *   <li>must：文档必须匹配这些条件，类似于AND</li>
 *   <li>should：文档应该匹配这些条件，类似于OR</li>
 *   <li>must_not：文档必须不匹配这些条件，类似于NOT</li>
 *   <li>filter：必须匹配，但不参与评分，用于过滤</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * QueryBuilder query = bool()
 *     .must(term("status", "active"))          // 状态必须是active
 *     .should(range("score").from(4.0))        // 分数最好大于4.0
 *     .mustNot(term("deleted", true))          // 不能是已删除的
 *     .filter(range("date").from("2024-01-01")) // 过滤出2024年之后的数据
 *     .minimumShouldMatch(1)                    // should子句至少匹配一个
 *     .boost(1.5f)                             // 提升整个查询的权重
 *     .build();
 * }</pre>
 *
 * <p>评分说明：
 * <ul>
 *   <li>must和should子句会影响文档的相关性评分</li>
 *   <li>filter和must_not子句不会影响评分，只用于过滤文档</li>
 *   <li>可以通过boost方法调整整个布尔查询的权重</li>
 * </ul>
 *
 * @author misury
 * @see BaseQueryBuilder
 * @see BoolQueryBuilder
 * @see org.elasticsearch.index.query.QueryBuilders#boolQuery()
 */
public class BoolQueryBuilderWrapper extends BaseQueryBuilder<BoolQueryBuilderWrapper> {
    private final BoolQueryBuilder boolQueryBuilder;

    /**
     * 创建一个新的布尔查询构建器
     * 初始化一个空的布尔查询，可以通过链式调用添加各种子句
     */
    public BoolQueryBuilderWrapper() {
        this.boolQueryBuilder = QueryBuilders.boolQuery();
        this.queryBuilder = this.boolQueryBuilder;
    }

    /**
     * 添加must子句（必须匹配）
     * 文档必须匹配这些条件，类似于SQL中的AND
     * 每个must子句都会影响文档的相关性评分
     *
     * @param queryBuilder 查询条件
     * @return 当前查询构建器实例，支持链式调用
     * @throws IllegalArgumentException 如果queryBuilder为null
     * @example must(term("status", "active"))
     */
    public BoolQueryBuilderWrapper must(QueryBuilder queryBuilder) {
        this.boolQueryBuilder.must(queryBuilder);
        return this;
    }

    /**
     * 添加should子句（应该匹配）
     * 文档应该匹配这些条件，类似于SQL中的OR
     * should子句会影响文档的相关性评分，匹配的should子句越多，相关性评分越高
     *
     * @param queryBuilder 查询条件
     * @return 当前查询构建器实例，支持链式调用
     * @throws IllegalArgumentException 如果queryBuilder为null
     * @example should(range("score").from(4.0))
     */
    public BoolQueryBuilderWrapper should(QueryBuilder queryBuilder) {
        this.boolQueryBuilder.should(queryBuilder);
        return this;
    }

    /**
     * 添加must_not子句（必须不匹配）
     * 文档必须不匹配这些条件，类似于SQL中的NOT IN
     * must_not子句只用于过滤文档，不会影响相关性评分
     *
     * @param queryBuilder 查询条件
     * @return 当前查询构建器实例，支持链式调用
     * @throws IllegalArgumentException 如果queryBuilder为null
     * @example mustNot(term("deleted", true))
     */
    public BoolQueryBuilderWrapper mustNot(QueryBuilder queryBuilder) {
        this.boolQueryBuilder.mustNot(queryBuilder);
        return this;
    }

    /**
     * 添加filter子句（过滤条件）
     * 文档必须匹配这些条件，但不会影响相关性评分
     * 适用于需要过滤但不需要评分的场景，执行效率高于must
     *
     * @param queryBuilder 查询条件
     * @return 当前查询构建器实例，支持链式调用
     * @throws IllegalArgumentException 如果queryBuilder为null
     * @example filter(range("date").from("2024-01-01"))
     */
    public BoolQueryBuilderWrapper filter(QueryBuilder queryBuilder) {
        this.boolQueryBuilder.filter(queryBuilder);
        return this;
    }

    /**
     * 设置should子句的最小匹配数
     * 指定文档必须匹配的should子句的最小数量
     *
     * <p>使用场景：
     * <ul>
     *   <li>当只有should子句时，默认必须匹配一个</li>
     *   <li>当有must或filter子句时，默认should子句可以一个都不匹配</li>
     *   <li>通过此方法可以改变默认行为，强制要求匹配指定数量的should子句</li>
     * </ul>
     *
     * @param minimumShouldMatch 最小匹配数量
     * @return 当前查询构建器实例，支持链式调用
     * @example minimumShouldMatch(2) // 至少要匹配2个should子句
     */
    public BoolQueryBuilderWrapper minimumShouldMatch(int minimumShouldMatch) {
        this.boolQueryBuilder.minimumShouldMatch(minimumShouldMatch);
        return this;
    }

    @Override
    public QueryBuilder build() {
        if (boost != null) {
            boolQueryBuilder.boost(boost);
        }
        if (name != null) {
            boolQueryBuilder.queryName(name);
        }
        return boolQueryBuilder;
    }
}
