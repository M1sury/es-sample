package com.misury.wrapper;

import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.FuzzyQueryBuilder;

/**
 * 模糊查询构建器包装类
 * 用于构建Elasticsearch的模糊查询，支持对文本进行容错搜索
 *
 * <p>模糊查询基于Levenshtein编辑距离算法，可以匹配与搜索词相似但不完全相同的内容。
 * 编辑距离是将一个字符串转换为另一个字符串所需的最小操作次数，操作包括：
 * <ul>
 *   <li>替换一个字符</li>
 *   <li>插入一个字符</li>
 *   <li>删除一个字符</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * QueryBuilder query = fuzzy("title", "elasticsearch")
 *     .fuzziness(2)          // 允许最多2个字符的编辑距离
 *     .prefixLength(3)       // 前3个字符必须精确匹配
 *     .maxExpansions(50)     // 最多生成50个变体
 *     .transpositions(true)  // 允许相邻字符交换
 *     .boost(1.5f)           // 提升查询权重
 *     .build();
 * }</pre>
 *
 * <p>性能优化建议：
 * <ul>
 *   <li>使用prefixLength减少需要进行模糊匹配的字符数</li>
 *   <li>通过maxExpansions限制查询的复杂度</li>
 *   <li>对于简单的前缀匹配，优先使用prefix查询</li>
 *   <li>考虑使用更专业的文本分析器来处理常见的拼写错误</li>
 * </ul>
 *
 * <p>模糊度设置说明：
 * <ul>
 *   <li>AUTO：根据词项长度自动确定（推荐）</li>
 *   <li>0：不允许编辑</li>
 *   <li>1：允许一个编辑</li>
 *   <li>2：允许两个编辑</li>
 * </ul>
 *
 * @author misury
 * @see BaseQueryBuilder
 * @see FuzzyQueryBuilder
 * @see org.elasticsearch.index.query.QueryBuilders#fuzzyQuery(String, Object)
 */
public class FuzzyQueryBuilderWrapper extends BaseQueryBuilder<FuzzyQueryBuilderWrapper> {
    private final FuzzyQueryBuilder fuzzyQueryBuilder;

    /**
     * 创建模糊查询构建器
     *
     * @param fieldName 要查询的字段名称
     * @param value 要匹配的值
     * @throws IllegalArgumentException 当字段名或值为空时抛出
     * @example new FuzzyQueryBuilderWrapper("title", "elasticsarch")
     */
    public FuzzyQueryBuilderWrapper(String fieldName, Object value) {
        validateFieldName(fieldName);
        validateValue(value);
        this.fuzzyQueryBuilder = new FuzzyQueryBuilder(fieldName, value);
        this.queryBuilder = fuzzyQueryBuilder;
    }

    /**
     * 设置模糊度
     * 控制允许的最大编辑距离（字符更改次数）
     *
     * @param fuzziness 模糊度对象，可以是AUTO或具体数值
     *                  - AUTO：根据词长自动确定编辑距离（推荐）
     *                  - 0：精确匹配
     *                  - 1：允许1个字符的差异
     *                  - 2：允许2个字符的差异
     * @return 当前构建器实例，支持链式调用
     * @example fuzziness(Fuzziness.AUTO)    // 自动确定模糊度
     * @example fuzziness(Fuzziness.ONE)     // 允许1个字符的差异
     * @example fuzziness(Fuzziness.TWO)     // 允许2个字符的差异
     */
    public FuzzyQueryBuilderWrapper fuzziness(Fuzziness fuzziness) {
        if (fuzziness == null) {
            throw new IllegalArgumentException("模糊度设置不能为空");
        }
        fuzzyQueryBuilder.fuzziness(fuzziness);
        return this;
    }

    /**
     * 设置前缀长度
     * 指定不参与模糊匹配的起始字符数
     *
     * @param prefixLength 前缀长度，必须大于等于0
     * @return 当前构建器实例，支持链式调用
     * @throws IllegalArgumentException 当前缀长度小于0时抛出
     * @example prefixLength(2) // 前两个字符必须精确匹配
     */
    public FuzzyQueryBuilderWrapper prefixLength(int prefixLength) {
        if (prefixLength < 0) {
            throw new IllegalArgumentException("前缀长度不能为负数");
        }
        fuzzyQueryBuilder.prefixLength(prefixLength);
        return this;
    }

    /**
     * 设置最大扩展次数
     * 限制查询可以扩展到的模糊变体的数量
     *
     * @param maxExpansions 最大扩展次数，必须大于0
     * @return 当前构建器实例，支持链式调用
     * @throws IllegalArgumentException 当最大扩展次数小于等于0时抛出
     * @example maxExpansions(50) // 最多生成50个模糊变体
     */
    public FuzzyQueryBuilderWrapper maxExpansions(int maxExpansions) {
        if (maxExpansions <= 0) {
            throw new IllegalArgumentException("最大扩展次数必须大于0");
        }
        fuzzyQueryBuilder.maxExpansions(maxExpansions);
        return this;
    }
}

