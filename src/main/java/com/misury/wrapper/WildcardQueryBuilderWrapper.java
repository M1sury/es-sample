package com.misury.wrapper;

import org.elasticsearch.index.query.WildcardQueryBuilder;

/**
 * 通配符查询构建器包装类
 * 用于构建Elasticsearch的通配符查询，支持使用通配符进行模式匹配
 *
 * <p>通配符查询支持两种特殊字符：
 * <ul>
 *   <li>*：匹配零个或多个任意字符</li>
 *   <li>?：匹配一个任意字符</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 基本通配符查询
 * QueryBuilder query = wildcard("title")
 *     .pattern("spring*")     // 匹配以spring开头的标题
 *     .boost(1.2f)           // 提升查询权重
 *     .rewrite("constant_score") // 使用常量评分重写
 *     .build();
 *
 * // 使用便捷方法
 * QueryBuilder prefixQuery = startsWith("title", "spring"); // 等同于 pattern("spring*")
 * QueryBuilder suffixQuery = endsWith("title", "boot");    // 等同于 pattern("*boot")
 * QueryBuilder containsQuery = contains("title", "cloud"); // 等同于 pattern("*cloud*")
 * }</pre>
 *
 * <p>性能注意事项：
 * <ul>
 *   <li>通配符查询可能会很慢，特别是当模式以*开头时</li>
 *   <li>建议使用rewrite方法选择合适的重写策略来优化性能</li>
 *   <li>对于前缀匹配，优先考虑使用专门的prefix查询</li>
 * </ul>
 *
 * <p>重写策略说明：
 * <ul>
 *   <li>constant_score：将查询转换为常量评分查询</li>
 *   <li>constant_score_boolean：转换为布尔查询</li>
 *   <li>top_terms_N：保留评分最高的N个词项</li>
 * </ul>
 *
 * @author misury
 * @see BaseQueryBuilder
 * @see WildcardQueryBuilder
 * @see org.elasticsearch.index.query.QueryBuilders#wildcardQuery(String, String)
 */
public class WildcardQueryBuilderWrapper extends BaseQueryBuilder<WildcardQueryBuilderWrapper> {
    private final WildcardQueryBuilder wildcardQueryBuilder;

    /**
     * 创建以指定前缀开头的通配符查询
     * 等同于SQL中的 LIKE 'prefix%'
     *
     * @param fieldName 要查询的字段名称
     * @param prefix 匹配的前缀字符串
     * @return 通配符查询构建器实例
     * @throws IllegalArgumentException 当字段名或前缀为空时抛出
     * @example startsWith("title", "Spring") // 匹配所有以"Spring"开头的标题
     */
    public static WildcardQueryBuilderWrapper startsWith(String fieldName, String prefix) {
        return new WildcardQueryBuilderWrapper(fieldName, prefix + "*");
    }

    /**
     * 创建以指定后缀结尾的通配符查询
     * 等同于SQL中的 LIKE '%suffix'
     *
     * @param fieldName 要查询的字段名称
     * @param suffix 匹配的后缀字符串
     * @return 通配符查询构建器实例
     * @throws IllegalArgumentException 当字段名或后缀为空时抛出
     * @example endsWith("email", "@gmail.com") // 匹配所有Gmail邮箱
     */
    public static WildcardQueryBuilderWrapper endsWith(String fieldName, String suffix) {
        return new WildcardQueryBuilderWrapper(fieldName, "*" + suffix);
    }

    /**
     * 创建包含指定文本的通配符查询
     * 等同于SQL中的 LIKE '%text%'
     * 注意：使用此方法可能会影响查询性能，建议配合rewrite方法使用
     *
     * @param fieldName 要查询的字段名称
     * @param text 要包含的文本
     * @return 通配符查询构建器实例
     * @throws IllegalArgumentException 当字段名或文本为空时抛出
     * @example contains("description", "elasticsearch")
     *         .rewrite("constant_score")    // 包含"elasticsearch"且使用常量评分
     * @example contains("title", "java")
     *         .boost(2.0f)                  // 包含"java"且提升相关性评分
     */
    public static WildcardQueryBuilderWrapper contains(String fieldName, String text) {
        return new WildcardQueryBuilderWrapper(fieldName, "*" + text + "*");
    }

    /**
     * 验证通配符模式的有效性
     * 检查模式是否为空以及是否包含通配符字符
     *
     * @param pattern 要验证的通配符模式
     * @throws IllegalArgumentException 当模式无效时抛出
     */
    private void validatePattern(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("通配符模式不能为空");
        }
        if (!pattern.contains("*") && !pattern.contains("?")) {
            throw new IllegalArgumentException("通配符模式必须包含'*'或'?'");
        }
    }

    /**
     * 创建通配符查询构建器
     * 支持使用 * 和 ? 通配符构建模式匹配查询
     *
     * @param fieldName 要查询的字段名称
     * @param pattern 通配符模式
     * @throws IllegalArgumentException 当字段名或模式为空时抛出
     * @example
     *   new WildcardQueryBuilderWrapper("title", "intro*")    // 匹配以"intro"开头的标题
     *   new WildcardQueryBuilderWrapper("email", "*@gmail.com") // 匹配Gmail邮箱
     *   new WildcardQueryBuilderWrapper("code", "A?B*")       // 匹配第一个字符为A，第三个字符为B的代码
     */
    public WildcardQueryBuilderWrapper(String fieldName, String pattern) {
        validateFieldName(fieldName);
        validateValue(pattern);
        validatePattern(pattern);
        this.wildcardQueryBuilder = new WildcardQueryBuilder(fieldName, pattern);
        this.queryBuilder = wildcardQueryBuilder;
    }

    /**
     * 设置查询重写方法
     * 用于优化查询性能的重写策略
     * 常用的重写方法包括：
     * - constant_score：将查询转换为常量评分查询
     * - constant_score_boolean：将查询转换为布尔查询
     * - top_terms_N：保留评分最高的N个词项
     *
     * @param rewriteMethod 重写方法名称
     * @return 当前构建器实例，支持链式调用
     * @throws IllegalArgumentException 当重写方法为空时抛出
     * @example rewrite("constant_score_boolean")
     */
    public WildcardQueryBuilderWrapper rewrite(String rewriteMethod) {
        if (rewriteMethod == null || rewriteMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("重写方法不能为空");
        }
        wildcardQueryBuilder.rewrite(rewriteMethod);
        return this;
    }
}

