package com.misury.wrapper;

import org.elasticsearch.index.query.RangeQueryBuilder;

/**
 * 范围查询构建器包装类
 * 用于构建Elasticsearch的范围查询，支持数值、日期等可比较类型的范围查询
 *
 * <p>范围查询允许您搜索落在特定范围内的字段值。支持以下类型的范围查询：
 * <ul>
 *   <li>数值范围：整数、浮点数等</li>
 *   <li>日期范围：支持多种日期格式和相对日期</li>
 *   <li>字符串范围：按字典序比较</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 数值范围查询
 * QueryBuilder query = range("price")
 *     .from(100)
 *     .to(200)
 *     .includeLower(true)    // 包含下界
 *     .includeUpper(false)   // 不包含上界
 *     .boost(1.5f)           // 提升查询权重
 *     .build();
 *
 * // 日期范围查询
 * QueryBuilder dateQuery = range("createTime")
 *     .fromNow()             // 从现在开始
 *     .toDaysFromNow(7)      // 到7天后
 *     .relation("WITHIN")    // 设置范围关系
 *     .build();
 * }</pre>
 *
 * <p>日期处理说明：
 * <ul>
 *   <li>支持相对日期：now, now-1d, now+1M 等</li>
 *   <li>支持标准日期格式：yyyy-MM-dd HH:mm:ss</li>
 *   <li>可以使用时间单位：y(年), M(月), w(周), d(天), h(小时), m(分钟), s(秒)</li>
 * </ul>
 *
 * <p>范围关系类型：
 * <ul>
 *   <li>INTERSECTS：范围存在交集（默认）</li>
 *   <li>CONTAINS：字段范围完全包含查询范围</li>
 *   <li>WITHIN：字段范围被查询范围完全包含</li>
 * </ul>
 *
 * @author misury
 * @see BaseQueryBuilder
 * @see RangeQueryBuilder
 * @see org.elasticsearch.index.query.QueryBuilders#rangeQuery(String)
 */
public class RangeQueryBuilderWrapper extends BaseQueryBuilder<RangeQueryBuilderWrapper> {
    private final RangeQueryBuilder rangeQueryBuilder;

    /**
     * 设置范围起始值为当前时间
     * 用于日期范围查询，将范围的起始时间设置为当前时间
     *
     * @return 当前构建器实例，支持链式调用
     * @example fromNow().toDaysFromNow(7) // 从现在到7天后
     */
    public RangeQueryBuilderWrapper fromNow() {
        rangeQueryBuilder.from("now");
        return this;
    }

    /**
     * 设置范围结束值为当前时间
     * 用于日期范围查询，将范围的结束时间设置为当前时间
     *
     * @return 当前构建器实例，支持链式调用
     * @example fromDaysAgo(7).toNow() // 从7天前到现在
     */
    public RangeQueryBuilderWrapper toNow() {
        rangeQueryBuilder.to("now");
        return this;
    }

    /**
     * 设置范围起始值为指定天数之前
     * 用于日期范围查询，将范围的起始时间设置为相对于当前时间的过去某一时刻
     *
     * @param days 过去的天数
     * @return 当前构建器实例，支持链式调用
     * @example fromDaysAgo(30).toNow() // 最近30天
     */
    public RangeQueryBuilderWrapper fromDaysAgo(int days) {
        rangeQueryBuilder.from("now-" + days + "d");
        return this;
    }

    /**
     * 设置范围结束值为指定天数之后
     * 用于日期范围查询，将范围的结束时间设置为相对于当前时间的将来某一时刻
     *
     * @param days 未来的天数
     * @return 当前构建器实例，支持链式调用
     * @example fromNow().toDaysFromNow(7) // 未来7天
     */
    public RangeQueryBuilderWrapper toDaysFromNow(int days) {
        rangeQueryBuilder.to("now+" + days + "d");
        return this;
    }

    /**
     * 设置范围查询为包含边界值
     * 相当于数学表达式中的 [from, to]
     *
     * @return 当前构建器实例，支持链式调用
     * @example range("age").from(18, true).to(30, true) // 年龄在18到30岁之间，包含18和30
     */
    public RangeQueryBuilderWrapper inclusive() {
        rangeQueryBuilder.includeLower(true).includeUpper(true);
        return this;
    }

    /**
     * 设置范围查询为排除边界值
     * 相当于数学表达式中的 (from, to)
     *
     * @return 当前构建器实例，支持链式调用
     * @example range("age").from(18, false).to(30, false) // 年龄在18到30岁之间，不包含18和30
     */
    public RangeQueryBuilderWrapper exclusive() {
        rangeQueryBuilder.includeLower(false).includeUpper(false);
        return this;
    }

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
     * 用于指定日期字符串的解析格式
     *
     * @param format 日期格式字符串，例如 "yyyy-MM-dd"
     * @return 当前构建器实例，支持链式调用
     * @throws IllegalArgumentException 当格式字符串为空时抛出
     * @example format("yyyy-MM-dd").from("2024-01-01").to("2024-12-31")
     */
    public RangeQueryBuilderWrapper format(String format) {
        if (format == null || format.trim().isEmpty()) {
            throw new IllegalArgumentException("日期格式不能为空");
        }
        rangeQueryBuilder.format(format);
        return this;
    }

    /**
     * 设置时区
     * 用于日期范围查询时的时区转换
     *
     * @param timeZone 时区ID字符串
     *                 常用值：
     *                 - "UTC"：世界标准时间
     *                 - "Asia/Shanghai"：中国标准时间
     *                 - "America/New_York"：美国东部时间
     *                 - "+08:00"：UTC+8时区
     * @return 当前构建器实例，支持链式调用
     * @throws IllegalArgumentException 当时区ID为空时抛出
     * @example timeZone("UTC")              // 使用UTC时间
     * @example timeZone("Asia/Shanghai")    // 使用北京时间
     * @example timeZone("+08:00")          // 使用UTC+8时区
     */
    public RangeQueryBuilderWrapper timeZone(String timeZone) {
        if (timeZone == null || timeZone.trim().isEmpty()) {
            throw new IllegalArgumentException("时区不能为空");
        }
        rangeQueryBuilder.timeZone(timeZone);
        return this;
    }

    /**
     * 设置关系操作符
     * 用于指定范围查询的关系类型
     *
     * @param relation 关系类型，可选值：INTERSECTS（相交）, CONTAINS（包含）, WITHIN（被包含）
     * @return 当前构建器实例，支持链式调用
     * @throws IllegalArgumentException 当关系类型为空时抛出
     * @example relation("CONTAINS")
     */
    public RangeQueryBuilderWrapper relation(String relation) {
        if (relation == null || relation.trim().isEmpty()) {
            throw new IllegalArgumentException("关系类型不能为空");
        }
        rangeQueryBuilder.relation(relation);
        return this;
    }
}

