package com.misury.wrapper;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

/**
 * 嵌套查询构建器包装类
 * 用于构建Elasticsearch的嵌套查询，支持对嵌套类型的文档进行查询
 *
 * <p>嵌套查询用于处理包含嵌套对象的文档结构。在Elasticsearch中，嵌套对象是文档中的特殊字段，
 * 它允许将数组中的对象作为独立文档进行索引和查询。
 *
 * <p>使用示例：
 * <pre>{@code
 * // 基本嵌套查询
 * QueryBuilder query = nested("comments")
 *     .query(term("comments.author", "john"))
 *     .scoreMode(ScoreMode.Avg)
 *     .boost(1.5f)
 *     .innerHit("matched_comments")
 *     .build();
 *
 * // 复杂嵌套查询
 * QueryBuilder complexQuery = nested("products")
 *     .query(
 *         bool()
 *             .must(range("products.price").from(100).to(200))
 *             .must(term("products.status", "active"))
 *     )
 *     .scoreMode(ScoreMode.Max)
 *     .innerHit(
 *         new InnerHitBuilder()
 *             .setSize(5)
 *             .setFetchSourceContext(true)
 *     )
 *     .build();
 * }</pre>
 *
 * <p>评分模式说明：
 * <ul>
 *   <li>Avg: 使用所有匹配的嵌套文档的平均分</li>
 *   <li>Max: 使用最高匹配分数</li>
 *   <li>Min: 使用最低匹配分数</li>
 *   <li>None: 不计算得分（性能最优）</li>
 *   <li>Total: 使用所有匹配分数的总和</li>
 * </ul>
 *
 * <p>性能考虑：
 * <ul>
 *   <li>嵌套查询比普通查询更消耗资源，因为每个嵌套文档都作为独立文档处理</li>
 *   <li>使用 ScoreMode.None 可以提高查询性能</li>
 *   <li>合理设置 inner_hits 的 size 参数可以控制返回的嵌套文档数量</li>
 * </ul>
 *
 * @author misury
 * @see BaseQueryBuilder
 * @see NestedQueryBuilder
 * @see ScoreMode
 * @see InnerHitBuilder
 */
public class NestedQueryBuilderWrapper extends BaseQueryBuilder<NestedQueryBuilderWrapper> {
    private final NestedQueryBuilder nestedQueryBuilder;

    /**
     * 创建嵌套查询构建器
     * 使用默认评分模式（ScoreMode.Avg）构建嵌套查询
     *
     * @param path 嵌套对象的路径，例如 "comments" 或 "product.variants"
     * @param query 要在嵌套对象上执行的查询条件
     * @throws IllegalArgumentException 当路径或查询为空时抛出
     * @example
     * <pre>{@code
     * // 简单示例
     * new NestedQueryBuilderWrapper("comments",
     *     termQuery("comments.status", "approved"))
     *
     * // 组合查询示例
     * new NestedQueryBuilderWrapper("products",
     *     bool()
     *         .must(range("products.price").gt(100))
     *         .must(term("products.inStock", true)))
     * }</pre>
     */
    public NestedQueryBuilderWrapper(String path, QueryBuilder query) {
        this(path, query, null);
    }

    /**
     * 创建嵌套查询构建器
     * 支持指定评分模式的完整构造方法
     *
     * @param path 嵌套对象的路径，例如 "comments" 或 "product.variants"
     * @param query 要在嵌套对象上执行的查询条件
     * @param scoreMode 评分模式，控制嵌套文档得分的计算方式：
     *                 - Avg：所有匹配的嵌套文档的平均分（默认）
     *                 - Max：最高匹配分数
     *                 - Min：最低匹配分数
     *                 - None：不计算得分（性能最优）
     *                 - Total：所有匹配分数的总和
     * @throws IllegalArgumentException 当路径或查询为空时抛出
     * @example
     * <pre>{@code
     * // 使用最大得分模式
     * new NestedQueryBuilderWrapper(
     *     "reviews",                           // 嵌套字段路径
     *     range("reviews.rating").gte(4),      // 查询条件
     *     ScoreMode.Max                        // 使用最高评分
     * )
     *
     * // 不计算得分模式（性能优化）
     * new NestedQueryBuilderWrapper(
     *     "variants",                          // 嵌套字段路径
     *     term("variants.color", "red"),       // 查询条件
     *     ScoreMode.None                       // 不计算得分
     * )
     * }</pre>
     */
    public NestedQueryBuilderWrapper(String path, QueryBuilder query, ScoreMode scoreMode) {
        validatePath(path);
        validateQuery(query);
        this.nestedQueryBuilder = new NestedQueryBuilder(path, query, scoreMode);
        this.queryBuilder = nestedQueryBuilder;
    }

    /**
     * 设置内部命中配置
     * 用于返回匹配的嵌套文档详情，支持快速配置
     *
     * @param name 内部命中的标识名称，用于在结果中区分不同的内部命中
     * @return 当前构建器实例，支持链式调用
     * @throws IllegalArgumentException 当名称为空时抛出
     * @example
     * <pre>{@code
     * nested("comments")
     *     .query(term("comments.status", "approved"))
     *     .innerHit("matched_comments")    // 设置内部命中
     *     .build()
     * }</pre>
     */
    public NestedQueryBuilderWrapper innerHit(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("内部命中名称不能为空");
        }
        nestedQueryBuilder.innerHit(new InnerHitBuilder(name));
        return this;
    }

    /**
     * 设置自定义内部命中构建器
     * 支持详细配置内部命中的行为，如大小限制、源过滤等
     *
     * @param innerHitBuilder 内部命中构建器，用于详细配置内部命中行为
     * @return 当前构建器实例，支持链式调用
     * @throws IllegalArgumentException 当构建器为null时抛出
     * @example
     * <pre>{@code
     * nested("products")
     *     .query(range("products.price").gt(100))
     *     .innerHit(
     *         new InnerHitBuilder()
     *             .setName("matched_products")
     *             .setSize(5)                     // 限制返回数量
     *             .setFetchSourceContext(true)    // 返回源文档
     *     )
     *     .build()
     * }</pre>
     */
    public NestedQueryBuilderWrapper innerHit(InnerHitBuilder innerHitBuilder) {
        if (innerHitBuilder == null) {
            throw new IllegalArgumentException("内部命中构建器不能为null");
        }
        nestedQueryBuilder.innerHit(innerHitBuilder);
        return this;
    }
}

