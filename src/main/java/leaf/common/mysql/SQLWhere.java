package leaf.common.mysql;

import leaf.common.DB;
import leaf.common.util.StrUtil;
import leaf.common.util.Valid;

/**
 * SQL条件语句类（已弃用，请使用leaf.common.mysql.Where）
 */
@Deprecated
public class SQLWhere {
    /**
     * sql字符串
     */
    private String sql = "";

    /**
     * 是否去空值
     */
    private boolean isRemoveEmptyValue = true;

    /**
     * 等于
     */
    public static final String Equal = "=";

    /**
     * like，固定通配符 %{value}%
     */
    public static final String Like = "like";

    /**
     * 自定义like后面的语句，可以自定义通配符
     */
    public static final String LikeCustom = "likeCustom";

    /**
     * in
     */
    public static final String In = "in";

    /**
     * 不等于
     */
    public static final String NotEqual = "!=";

    /**
     * 无参构造器
     */
    public SQLWhere() {}

    /**
     * 有参构造器
     * @param isRemoveEmptyValue 是否去空值
     */
    public SQLWhere(boolean isRemoveEmptyValue) {
        this.isRemoveEmptyValue = isRemoveEmptyValue;
    }

    /**
     * 添加条件
     * @param field 字段
     * @param value 值
     * @param conjunction 连词（and、or）
     * @param relationship 关系（=、!=、like、in）
     * @return 生成条件语句后添加到SQLWhere对象的sql属性并返回该SQLWhere对象
     */
    public SQLWhere add(String field, String value, String conjunction, String relationship) {
        if(!Valid.isEmpty(value) || !isRemoveEmptyValue) {
            switch(relationship.toLowerCase().trim()) {
                case Equal:
                case NotEqual:
                    value = DB.e(value);
                    break;
                case Like:
                    value = "%"+DB.el(value)+"%";
                    break;
                case LikeCustom:
                    relationship = Like;
                    break;
                case In:
                    sql += conjunction + " " + field + " " + relationship + " (" + value + ") ";
                    return this;
            }

            sql += conjunction + " " + field + " " + relationship + " '" + value + "' ";
        }
        return this;
    }

    /**
     * 添加and条件
     * @param field 字段
     * @param value 值
     * @param relationship 关系（=、!=、like、in）
     * @return 生成条件语句后添加到SQLWhere对象的sql属性并返回该SQLWhere对象
     */
    public SQLWhere addAnd(String field, String value, String relationship) {
        return add(field,value,"and",relationship);
    }

    /**
     * 添加or条件
     * @param field 字段
     * @param value 值
     * @param relationship 关系（=、!=、like、in）
     * @return 生成条件语句后添加到SQLWhere对象的sql属性并返回该SQLWhere对象
     */
    public SQLWhere addOr(String field, String value, String relationship) {
        return add(field,value,"or",relationship);
    }

    /**
     * 添加括号
     * @param logicalOperator 逻辑运算符（and、or）
     * @return 生成条件语句后添加到SQLWhere对象的sql属性并返回该SQLWhere对象
     */
    public SQLWhere addBracket(String logicalOperator) {
        if(!Valid.isBlank(sql))
            sql = logicalOperator + " (" + StrUtil.removePrefix(sql,"and","or") + ") ";
        return this;
    }

    /**
     * 追加SQL对象
     * @param sqlWhere SQL
     * @return 生成条件语句后添加到SQLWhere对象的sql属性并返回该SQLWhere对象
     */
    public SQLWhere append(SQLWhere sqlWhere) {
        this.sql += sqlWhere.sql;
        return this;
    }

    /**
     * 获取条件
     * @return 条件SQL语句
     */
    @Override
    public String toString() {
        if(Valid.isEmpty(sql)) {
            return "";
        }

        return " where"+ StrUtil.removePrefix(sql,"and","or");
    }
}
