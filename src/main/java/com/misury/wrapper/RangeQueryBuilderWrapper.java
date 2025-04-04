package com.misury.wrapper;

import org.elasticsearch.index.query.RangeQueryBuilder;

/**
 * 范围查询构建器包装类
 * 用于构建Elasticsearch的范围查询，支持数值、日期等可比较类型的范围查询
 * 提供流式API，支持链式调用
 *
 * @author misury
 * @see BaseQueryBuilder
 * @see RangeQueryBuilder
 */
public class RangeQueryBuilderWrapper extends BaseQueryBuilder<RangeQueryBuilderWrapper> {
    private final RangeQueryBuilder rangeQueryBuilder;

    /**
     * 创建范围查询构建器
     *
     * @param fieldName 要查询的字段名称
     * @throws IllegalArgumentException 当字段名为空时抛出
     */
    public RangeQueryBuilderWrapper(String fieldName) {
        validateFieldName(fieldName);
        this.rangeQueryBuilder = new RangeQueryBuilder(fieldName);
        this.queryBuilder = rangeQueryBuilder;
    }

    /**
     * 创建带有起止范围的范围查询构建器
     *
     * @param fieldName 要查询的字段名称
     * @param from 范围起始值
     * @param to 范围结束值
     * @param inclusive 是否包含边界值
     * @throws IllegalArgumentException 当字段名为空时抛出
     */
    public RangeQueryBuilderWrapper(String fieldName, String from, String to, boolean inclusive) {
        validateFieldName(fieldName);
        this.rangeQueryBuilder = new RangeQueryBuilder(fieldName);
        if (inclusive) {
            rangeQueryBuilder.gte(from);
            rangeQueryBuilder.lte(to);
        } else {
            rangeQueryBuilder.gt(from);
            rangeQueryBuilder.lt(to);
        }
        this.queryBuilder = rangeQueryBuilder;
    }

    /**
     * 设置范围的起始值
     *
     * @param from 范围起始值
     * @param inclusive 是否包含起始值
     *                 true表示大于等于(gte)，false表示大于(gt)
     * @return 当前构建器实例，支持链式调用
     * @example
     *   from(18, true)  // 大于等于18
     *   from(18, false) // 大于18
     */
    public RangeQueryBuilderWrapper from(Object from, boolean inclusive) {
        if (inclusive) {
            rangeQueryBuilder.gte(from);
        } else {
            rangeQueryBuilder.gt(from);
        }
        return this;
    }

    /**
     * 设置范围的结束值
     *
     * @param to 范围结束值
     * @param inclusive 是否包含结束值
     *                 true表示小于等于(lte)，false表示小于(lt)
     * @return 当前构建器实例，支持链式调用
     * @example
     *   to(30, true)  // 小于等于30
     *   to(30, false) // 小于30
     */
    public RangeQueryBuilderWrapper to(Object to, boolean inclusive) {
        if (inclusive) {
            rangeQueryBuilder.lte(to);
        } else {
            rangeQueryBuilder.lt(to);
        }
        return this;
    }

    /**
     * 设置日期格式
     * 当查询日期字段时，用于指定日期的格式化模式
     *
     * @param format 日期格式模式
     * @return 当前构建器实例，支持链式调用
     * @example
     *   format("yyyy-MM-dd")
     *   format("yyyy-MM-dd HH:mm:ss")
     */
    public RangeQueryBuilderWrapper format(String format) {
        rangeQueryBuilder.format(format);
        return this;
    }

    /**
     * 设置时区
     * 当查询日期字段时，用于指定时区
     *
     * @param timeZone 时区字符串
     * @return 当前构建器实例，支持链式调用
     * @example
     *   timeZone("+08:00")
     *   timeZone("Asia/Shanghai")
     */
    public RangeQueryBuilderWrapper timeZone(String timeZone) {
        rangeQueryBuilder.timeZone(timeZone);
        return this;
    }

    /**
     * 设置关系
     * 用于设置范围查询的关系类型
     *
     * @param relation 关系类型（WITHIN、CONTAINS、INTERSECTS）
     * @return 当前构建器实例，支持链式调用
     * @example
     *   relation("WITHIN")
     */
    public RangeQueryBuilderWrapper relation(String relation) {
        rangeQueryBuilder.relation(relation);
        return this;
    }
}

