package leaf.common.mysql;

import leaf.common.DB;
import leaf.common.util.StrUtil;
import leaf.common.util.Valid;

/**
 * SQL条件语句类，用于动态拼接WHERE条件
 */
public class Where {
    private String sql = "";
    private boolean isRemoveEmptyValue = false;
    private String pendingLogic = "";

    /**
     * SQL运算符枚举类，定义所有支持的关系运算符
     */
    public enum Operator {
        EQ("="),                 // 等于
        NOT_EQ("!="),           // 不等于
        GT(">"),                // 大于
        LT("<"),                // 小于
        GT_EQ(">="),            // 大于等于
        LT_EQ("<="),            // 小于等于
        LIKE("like"),           // 模糊匹配
        NOT_LIKE("not like"),   // 非模糊匹配
        LIKE_CUSTOM("like"),    // 自定义模糊匹配
        NOT_LIKE_CUSTOM("not like"), // 自定义非模糊匹配
        IN("in"),               // IN 集合
        NOT_IN("not in"),       // NOT IN 集合
        BETWEEN("between"),     // BETWEEN 范围
        NOT_BETWEEN("not between"), // NOT BETWEEN 范围
        IS_NULL("is null"),     // 为NULL
        IS_NOT_NULL("is not null"), // 不为NULL
        REGEXP("regexp"),       // 正则匹配
        EXISTS("exists"),       // 子查询存在
        NOT_EXISTS("not exists"), // 子查询不存在
        SAFE_EQUAL("<=>");      // NULL安全等于

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    /**
     * 构造器
     */
    public Where() {}

    /**
     * 构造器
     * @param isRemoveEmptyValue 是否移除值为空的条件，默认false
     */
    public Where(boolean isRemoveEmptyValue) {
        this.isRemoveEmptyValue = isRemoveEmptyValue;
    }

    /**
     * 添加 OR 连接符
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where or() {
        this.pendingLogic = "or";
        return this;
    }

    /**
     * 添加 AND 连接符
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where and() {
        this.pendingLogic = "and";
        return this;
    }

    /**
     * 添加条件语句
     * @param column 字段名
     * @param value 值
     * @param operator 关系运算符
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where add(String column, String value, Operator operator) {
        if (!Valid.isEmpty(value) || !isRemoveEmptyValue) {
            switch (operator) {
                case EQ:
                case NOT_EQ:
                case GT:
                case LT:
                case GT_EQ:
                case LT_EQ:
                case LIKE_CUSTOM:
                case NOT_LIKE_CUSTOM:
                case SAFE_EQUAL:
                case REGEXP:
                    appendPendingLogic();
                    this.sql += " " + column + " " + operator.getSymbol() + " '" + DB.e(value) + "' ";
                    break;
                case LIKE:
                case NOT_LIKE:
                    appendPendingLogic();
                    this.sql += " " + column + " " + operator.getSymbol() + " '%" + DB.el(value) + "%' ";
                    break;
                case IN:
                case NOT_IN:
                    if(!Valid.isBlank(value)) {
                        appendPendingLogic();
                        this.sql += " " + column + " " + operator.getSymbol() + " (" + value + ") ";
                    }
                    break;
                case BETWEEN:
                case NOT_BETWEEN:
                    appendPendingLogic();
                    String[] parts = value.split(",");

                    if (parts.length == 2) {
                        this.sql += " " + column + " " + operator.getSymbol() + " '" + DB.e(parts[0]) + "' and '" + DB.e(parts[1]) + "' ";
                    }
                    break;
                case EXISTS:
                case NOT_EXISTS:
                    appendPendingLogic();
                    this.sql += " " + operator.getSymbol() + " (" + value + ") ";
                    break;
                case IS_NULL:
                case IS_NOT_NULL:
                    appendPendingLogic();
                    this.sql += " " + column + " " + operator.getSymbol() + " ";
                default:
                    break;
            }

            this.pendingLogic = "";
        }
        return this;
    }

    /**
     * 附加待处理逻辑，在条件前面添加 and 或者 or 关键字
     */
    private void appendPendingLogic() {
        if (!Valid.isEmpty(sql) && !Valid.isEmpty(pendingLogic)) {
            this.sql += this.pendingLogic;
        }
    }

    /**
     * 添加等于条件语句
     * @param column 字段
     * @param value 值
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where eq(String column, String value) {
        return add(column, value, Operator.EQ);
    }

    /**
     * 添加不等于条件语句
     * @param column 字段
     * @param value 值
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where notEq(String column, String value) {
        return add(column, value, Operator.NOT_EQ);
    }

    /**
     * 添加大于条件语句
     * @param column 字段
     * @param value 值
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where gt(String column, String value) {
        return add(column, value, Operator.GT);
    }

    /**
     * 添加小于条件语句
     * @param column 字段
     * @param value 值
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where lt(String column, String value) {
        return add(column, value, Operator.LT);
    }

    /**
     * 添加大于等于条件语句
     * @param column 字段
     * @param value 值
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where gtEq(String column, String value) {
        return add(column, value, Operator.GT_EQ);
    }

    /**
     * 添加小于等于条件语句
     * @param column 字段
     * @param value 值
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where ltEq(String column, String value) {
        return add(column, value, Operator.LT_EQ);
    }

    /**
     * 添加模糊匹配条件语句
     * @param column 字段
     * @param value 值
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where like(String column, String value) {
        return add(column, value, Operator.LIKE);
    }

    /**
     * 添加非模糊匹配条件语句
     * @param column 字段
     * @param value 值
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where notLike(String column, String value) {
        return add(column, value, Operator.NOT_LIKE);
    }

    /**
     * 添加自定义模糊匹配条件语句
     * @param column 字段
     * @param value 值
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where likeCustom(String column, String value) {
        return add(column, value, Operator.LIKE_CUSTOM);
    }

    /**
     * 添加自定义非模糊匹配条件语句
     * @param column 字段
     * @param value 值
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where notLikeCustom(String column, String value) {
        return add(column, value, Operator.NOT_LIKE_CUSTOM);
    }

    /**
     * 添加正则匹配条件语句
     * @param column 字段
     * @param value 值
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where regexp(String column, String value) {
        return add(column, value, Operator.REGEXP);
    }

    /**
     * 添加in集合条件语句
     * @param column 字段
     * @param valueList 值，例如 1,2,3
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where in(String column, String valueList) {
        return add(column, valueList, Operator.IN);
    }

    /**
     * 添加not in集合条件语句
     * @param column 字段
     * @param valueList 值
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where notIn(String column, String valueList) {
        return add(column, valueList, Operator.NOT_IN);
    }

    /**
     * 添加范围between and条件语句
     * @param column 字段
     * @param range 范围，例如 2,6
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where between(String column, String range) {
        return add(column, range, Operator.BETWEEN);
    }

    /**
     * 添加范围not between and条件语句
     * @param column 字段
     * @param range 范围，例如 2,6
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where notBetween(String column, String range) {
        return add(column, range, Operator.NOT_BETWEEN);
    }

    /**
     * 添加为null条件语句
     * @param column 字段
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where isNull(String column) {
        return add(column, null, Operator.IS_NULL);
    }

    /**
     * 添加非null条件语句
     * @param column 字段
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where isNotNull(String column) {
        return add(column, null, Operator.IS_NOT_NULL);
    }

    /**
     * 添加子查询存在条件语句
     * @param subQuery 子查询语句
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where exists(String subQuery) {
        return add(null, subQuery, Operator.EXISTS);
    }

    /**
     * 添加子查询不存在条件语句
     * @param subQuery 子查询语句
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where notExists(String subQuery) {
        return add(null, subQuery, Operator.NOT_EXISTS);
    }

    /**
     * 添加不等于条件语句
     * @param column 字段
     * @param value 值
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where safeEqual(String column, String value) {
        return add(column, value, Operator.SAFE_EQUAL);
    }

    /**
     * 添加自定义SQL片段
     * @param sql 自定义的sql片段
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where custom(String sql) {
        if (!Valid.isEmpty(sql)) {
            appendPendingLogic();
            this.sql += " " + sql + " ";
            pendingLogic = "";
        }
        return this;
    }

    /**
     * 追加另一个 Where 条件
     * @param Where Where条件
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where append(Where Where) {
        if (Where != null && !Valid.isEmpty(Where.sql)) {
            appendPendingLogic();
            this.sql += Where.sql;
            pendingLogic = "";
        }
        return this;
    }

    /**
     * 前置插入 where 关键字
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where prependWhere() {
        if(!Valid.isBlank(this.sql)) {
            this.sql = " where"+ StrUtil.removePrefix(sql,"and","or");
        }
        return this;
    }

    /**
     * 将 Where 添加分组（使用括号包起来）
     * @return 当前 Where 对象（用于链式调用）
     */
    public Where group() {
        if(!Valid.isEmpty(this.sql)) {
            this.sql = " (" + this.sql + ") ";
        }

        return this;
    }

    /**
     * 将 Where 添加分组（使用括号包起来）
     * @param subWhere 需要分组的 Where 条件
     * @return 当前 Where 对象（用于链式调用）
     */
    public static Where group(Where subWhere) {
        if(!Valid.isEmpty(subWhere.sql)) {
            subWhere.sql = " (" + subWhere.sql + ") ";
        }

        return subWhere;
    }

    /**
     * 拼接in条件字符串
     * @param arr 需要拼接的数组
     * @return 拼接后的字符串
     */
    public static String joinIn(String[] arr) {
        if (arr == null) return "";
        String str = "";
        for (int i = 0;i < arr.length;i++) {
            if(i == arr.length - 1) {
                str += "'"+DB.e(arr[i])+"'";
            } else {
                str += "'"+DB.e(arr[i])+"',";
            }
        }
        return str;
    }

    /**
     * 获取最终拼接的SQL条件字符串
     * @return 最终拼接的SQL条件字符串
     */
    @Override
    public String toString() {
        return sql.trim();
    }
}
