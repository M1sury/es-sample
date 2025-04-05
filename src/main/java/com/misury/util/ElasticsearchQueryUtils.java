package com.misury.util;

import com.misury.wrapper.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;

/**
 * Elasticsearch查询工具类
 * 提供各种查询构建器的工厂方法和快捷方法
 */
@Slf4j
public class ElasticsearchQueryUtils {

    /**
     * 创建范围查询构建器
     * 用于创建针对数值、日期等可比较类型的范围查询
     *
     * @param fieldName 要查询的字段名称
     * @return RangeQueryBuilderWrapper 范围查询构建器包装类
     * @throws IllegalArgumentException 当字段名为空时抛出
     * @example
     *   range("age")
     *     .from(18, true)    // 大于等于18
     *     .to(30, false)     // 小于30
     *     .build();
     */
    public static RangeQueryBuilderWrapper range(String fieldName) {
        return new RangeQueryBuilderWrapper(fieldName);
    }

    /**
     * 创建模糊查询构建器
     * 用于创建容错查询，可以匹配与搜索词相似的词
     *
     * @param fieldName 要查询的字段名称
     * @param value 要匹配的值
     * @return FuzzyQueryBuilderWrapper 模糊查询构建器包装类
     * @throws IllegalArgumentException 当字段名或值为空时抛出
     * @example
     *   fuzzy("name", "john")
     *     .fuzziness(Fuzziness.AUTO)
     *     .build();
     */
    public static FuzzyQueryBuilderWrapper fuzzy(String fieldName, Object value) {
        return new FuzzyQueryBuilderWrapper(fieldName, value);
    }

    /**
     * 创建通配符查询构建器
     * 支持使用通配符 * 和 ? 进行模式匹配
     * * 匹配零个或多个字符
     * ? 匹配一个字符
     *
     * @param fieldName 要查询的字段名称
     * @param pattern 通配符模式
     * @return WildcardQueryBuilderWrapper 通配符查询构建器包装类
     * @throws IllegalArgumentException 当字段名或模式为空时抛出
     * @example
     *   wildcard("title", "intro*")  // 匹配所有以"intro"开头的标题
     */
    public static WildcardQueryBuilderWrapper wildcard(String fieldName, String pattern) {
        return new WildcardQueryBuilderWrapper(fieldName, pattern);
    }

    /**
     * 创建嵌套查询构建器（使用默认评分模式）
     * 用于查询嵌套类型的文档
     *
     * @param path 嵌套对象的路径
     * @param query 要在嵌套对象上执行的查询
     * @return NestedQueryBuilderWrapper 嵌套查询构建器包装类
     * @throws IllegalArgumentException 当路径或查询为空时抛出
     */
    public static NestedQueryBuilderWrapper nested(String path, QueryBuilder query) {
        return new NestedQueryBuilderWrapper(path, query, ScoreMode.None);
    }

    /**
     * 创建嵌套查询构建器（指定评分模式）
     * 用于查询嵌套类型的文档，并可以控制评分计算方式
     *
     * @param path 嵌套对象的路径
     * @param query 要在嵌套对象上执行的查询
     * @param scoreMode 评分模式（None, Avg, Max, Total, Min）
     * @return NestedQueryBuilderWrapper 嵌套查询构建器包装类
     * @throws IllegalArgumentException 当路径或查询为空时抛出
     */
    public static NestedQueryBuilderWrapper nested(String path, QueryBuilder query, ScoreMode scoreMode) {
        return new NestedQueryBuilderWrapper(path, query, scoreMode);
    }

    /**
     * 创建精确匹配查询
     * 用于创建完全匹配查询，不会对搜索词进行分析
     *
     * @param fieldName 要查询的字段名称
     * @param value 要精确匹配的值
     * @return TermQueryBuilder 精确查询构建器
     * @throws IllegalArgumentException 当字段名或值为空时抛出
     * @example
     *   term("status", "active")  // 精确匹配状态为"active"的文档
     */
    public static TermQueryBuilder term(String fieldName, Object value) {
        validateFieldName(fieldName);
        validateValue(value);
        return new TermQueryBuilder(fieldName, value);
    }

    /**
     * 创建针对keyword类型字段的通配符查询
     * 自动在字段名后添加.keyword后缀
     *
     * @param fieldName 要查询的字段名称（不需要包含.keyword后缀）
     * @param pattern 通配符模式
     * @return WildcardQueryBuilderWrapper 通配符查询构建器包装类
     * @throws IllegalArgumentException 当字段名或模式为空时抛出
     * @example
     *   keywordWildcard("email", "*@gmail.com")  // 查询所有gmail邮箱
     */
    public static WildcardQueryBuilderWrapper keywordWildcard(String fieldName, String pattern) {
        return wildcard(fieldName + ".keyword", pattern);
    }

    /**
     * 创建日期范围查询（使用默认日期格式：yyyy-MM-dd HH:mm:ss）
     *
     * @param fieldName 日期字段名称
     * @return RangeQueryBuilderWrapper 范围查询构建器包装类
     * @throws IllegalArgumentException 当字段名为空时抛出
     * @example
     *   dateRange("createTime")
     *     .from("2024-01-01 00:00:00", true)
     *     .to("2024-12-31 23:59:59", true)
     *     .build();
     */
    public static RangeQueryBuilderWrapper dateRange(String fieldName) {
        return dateRange(fieldName, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 创建日期范围查询（指定日期格式）
     *
     * @param fieldName 日期字段名称
     * @param datePattern 日期格式模式
     * @return RangeQueryBuilderWrapper 范围查询构建器包装类
     * @throws IllegalArgumentException 当字段名为空时抛出
     * @example
     *   dateRange("createTime", "yyyy-MM-dd")
     *     .from("2024-01-01", true)
     *     .to("2024-12-31", true)
     *     .build();
     */
    public static RangeQueryBuilderWrapper dateRange(String fieldName, String datePattern) {
        return range(fieldName).format(datePattern);
    }

    /**
     * 创建带默认配置的模糊查询
     * 使用自动模糊度、前缀长度2、最大扩展次数10
     *
     * @param fieldName 要查询的字段名称
     * @param value 要模糊匹配的值
     * @return FuzzyQueryBuilderWrapper 模糊查询构建器包装类
     * @throws IllegalArgumentException 当字段名或值为空时抛出
     * @example
     *   defaultFuzzy("name", "smith")  // 可以匹配 "smith"、"smyth" 等相似词
     */
    public static FuzzyQueryBuilderWrapper defaultFuzzy(String fieldName, String value) {
        return fuzzy(fieldName, value)
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(2)
                .maxExpansions(10);
    }

    /**
     * 验证字段名是否有效
     *
     * @param fieldName 要验证的字段名
     * @throws IllegalArgumentException 当字段名为null或空时抛出
     */
    private static void validateFieldName(String fieldName) {
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
    private static void validateValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("查询值不能为空");
        }
    }

    /**
     * 创建布尔查询构建器
     * 用于组合多个查询条件，支持must、should、must_not和filter子句
     *
     * @return BoolQueryBuilderWrapper 布尔查询构建器包装类
     * @example
     *   bool()
     *     .must(term("status", "active"))
     *     .should(range("score").from(4.0))
     *     .filter(range("date").from("2024-01-01"))
     *     .build();
     */
    public static BoolQueryBuilderWrapper bool() {
        return new BoolQueryBuilderWrapper();
    }

    /**
     * 创建匹配所有文档的查询
     *
     * @return MatchAllQueryBuilder 匹配所有查询构建器
     */
    public static MatchAllQueryBuilder matchAll() {
        return QueryBuilders.matchAllQuery();
    }

    /**
     * 创建terms聚合查询构建器
     *
     * @param name 聚合名称
     * @return TermsAggregationBuilder terms聚合构建器
     */
    public static TermsAggregationBuilder terms(String name) {
        validateValue(name);
        return AggregationBuilders.terms(name);
    }
}
