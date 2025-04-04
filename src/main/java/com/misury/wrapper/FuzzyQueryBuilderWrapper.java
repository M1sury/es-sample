package com.misury.wrapper;

import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.FuzzyQueryBuilder;

/**
 * 模糊查询构建器包装类
 * 用于构建Elasticsearch的模糊查询，支持对文本进行容错搜索
 * 可以匹配与搜索词相似但不完全相同的内容
 *
 * @author misury
 * @see BaseQueryBuilder
 * @see FuzzyQueryBuilder
 */
public class FuzzyQueryBuilderWrapper extends BaseQueryBuilder<FuzzyQueryBuilderWrapper> {
    private final FuzzyQueryBuilder fuzzyQueryBuilder;

    /**
     * 创建模糊查询构建器
     *
     * @param fieldName 要查询的字段名称
     * @param value 要匹配的值
     * @throws IllegalArgumentException 当字段名或值为空时抛出
     */
    public FuzzyQueryBuilderWrapper(String fieldName, Object value) {
        validateFieldName(fieldName);
        validateValue(value);
        this.fuzzyQueryBuilder = new FuzzyQueryBuilder(fieldName, value);
        this.queryBuilder = fuzzyQueryBuilder;
    }

    /**
     * 设置模糊度
     * 控制允许的最大编辑距离（字符替换、插入或删除的次数）
     *
     * @param fuzziness 模糊度设置，推荐使用Fuzziness.AUTO
     * @return 当前构建器实例
     * @example
     *   fuzziness(Fuzziness.AUTO)
     *   fuzziness(Fuzziness.ONE)
     */
    public FuzzyQueryBuilderWrapper fuzziness(Fuzziness fuzziness) {
        fuzzyQueryBuilder.fuzziness(fuzziness);
        return this;
    }

    /**
     * 设置前缀长度
     * 指定不参与模糊匹配的起始字符数
     *
     * @param prefixLength 前缀长度，必须大于等于0
     * @return 当前构建器实例
     */
    public FuzzyQueryBuilderWrapper prefixLength(int prefixLength) {
        fuzzyQueryBuilder.prefixLength(prefixLength);
        return this;
    }

    /**
     * 设置最大扩展次数
     * 限制查询可以扩展的最大词项数
     *
     * @param maxExpansions 最大扩展次数，必须大于0
     * @return 当前构建器实例
     */
    public FuzzyQueryBuilderWrapper maxExpansions(int maxExpansions) {
        fuzzyQueryBuilder.maxExpansions(maxExpansions);
        return this;
    }
}

