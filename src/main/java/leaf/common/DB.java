package leaf.common;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.sql.SQLUtils;
import leaf.common.object.JSONList;
import leaf.common.object.JSONMap;
import leaf.common.util.Num;
import leaf.common.util.StrUtil;
import leaf.common.util.Valid;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 数据库的操作类
 * jdbc+连接池
 * 依赖 mysql-connector-java-8.0.29.jar druid-1.0.9.jar
 */
public class DB {
    public static final DruidDataSource DRUID_DATA_SOURCE = new DruidDataSource();

//    /**
//     * 配置连接
//     */
//    public static void config() {
//        DB.DRUID_DATA_SOURCE.setDriverClassName(Config.Properties.getProperty("common.jdbc.driver",""));
//        DB.DRUID_DATA_SOURCE.setUrl(Config.Properties.getProperty("common.jdbc.url",""));
//        DB.DRUID_DATA_SOURCE.setUsername(Config.Properties.getProperty("common.jdbc.username",""));
//        DB.DRUID_DATA_SOURCE.setPassword(Config.Properties.getProperty("common.jdbc.password",""));
//        DB.DRUID_DATA_SOURCE.setMaxActive(Config.parseInteger(Config.Properties.getProperty("common.jdbc.maxActive","8"),"common.jdbc.maxActive"));
//        DB.DRUID_DATA_SOURCE.setMinIdle(Config.parseInteger(Config.Properties.getProperty("common.jdbc.minIdle","0"),"common.jdbc.minIdle"));
//        DB.DRUID_DATA_SOURCE.setInitialSize(Config.parseInteger(Config.Properties.getProperty("common.jdbc.initialSize","0"),"common.jdbc.initialSize"));
//        DB.DRUID_DATA_SOURCE.setValidationQuery("SELECT 1");//设置用于检验连接是否有效的SQL语句
//        DB.DRUID_DATA_SOURCE.setTestOnBorrow(false);//配置从连接池获取连接时，是否检查连接有效性，true每次都检查；false不检查。做了这个配置会降低性能
//        DB.DRUID_DATA_SOURCE.setTestOnReturn(false);//配置向连接池归还连接时，是否检查连接有效性，true每次都检查；false不检查。做了这个配置会降低性能
//        DB.DRUID_DATA_SOURCE.close();//关闭数据源
//    }

    /**
     * 配置连接
     * @param driver Driver
     * @param url url
     * @param root 用户名
     * @param password 密码
     */
    public static void druidConfig(String driver,String url,String root,String password, String initialSize, String maxActive, String minIdle) {
        DRUID_DATA_SOURCE.setDriverClassName(driver);
        DRUID_DATA_SOURCE.setUrl(url);
        DRUID_DATA_SOURCE.setUsername(root);
        DRUID_DATA_SOURCE.setPassword(password);
        DB.DRUID_DATA_SOURCE.setInitialSize(Config.parseInteger(initialSize, "spring.datasource.druid.initialSize"));
        DB.DRUID_DATA_SOURCE.setMaxActive(Config.parseInteger(maxActive, "spring.datasource.druid.maxActive"));
        DB.DRUID_DATA_SOURCE.setMinIdle(Config.parseInteger(minIdle, "spring.datasource.druid.minIdle"));
        DRUID_DATA_SOURCE.setValidationQuery("SELECT 1");//设置用于检验连接是否有效的SQL语句
        DB.DRUID_DATA_SOURCE.setTestOnBorrow(false);//配置从连接池获取连接时，是否检查连接有效性，true每次都检查；false不检查。做了这个配置会降低性能
        DB.DRUID_DATA_SOURCE.setTestOnReturn(false);//配置向连接池归还连接时，是否检查连接有效性，true每次都检查；false不检查。做了这个配置会降低性能
        DRUID_DATA_SOURCE.close();//关闭数据源
    }

    /**
     * 配置连接
     * @param driver Driver
     * @param url url
     * @param root 用户名
     * @param password 密码
     * @param maxActive 最大并发连接数 默认:8
     * @param minIdle 最小空闲连接数 默认:0
     * @param initialSize 初始连接数 默认:0
     */
    public static void druidConfig(String driver,String url,String root,String password,int maxActive,int minIdle,int initialSize) {
        DRUID_DATA_SOURCE.setDriverClassName(driver);
        DRUID_DATA_SOURCE.setUrl(url);//设置数据库连接URL
        DRUID_DATA_SOURCE.setUsername(root);//设置数据库用户名和密码
        DRUID_DATA_SOURCE.setPassword(password);
        DRUID_DATA_SOURCE.setMinIdle(minIdle);//设置最小连接数
        DRUID_DATA_SOURCE.setMaxActive(maxActive);//设置最大活跃连接数
        DRUID_DATA_SOURCE.setInitialSize(initialSize);//设置初始连接数
        DRUID_DATA_SOURCE.setValidationQuery("SELECT 1");//设置用于检验连接是否有效的SQL语句
        DRUID_DATA_SOURCE.setTestOnBorrow(false);//配置向连接池归还连接时，是否检查连接有效性，true每次都检查；false不检查。做了这个配置会降低性能
        DRUID_DATA_SOURCE.setTestOnReturn(false);//配置向连接池归还连接时，是否检查连接有效性，true每次都检查；false不检查。做了这个配置会降低性能
        DRUID_DATA_SOURCE.close();//关闭数据源
    }

    /**
     * escape 参数转义，防止SQL注入，用于字符串拼接
     * @param str 需要转义的字符串
     * @return 转义后的字符串
     */
    public static String e(String str) {
        if(str == null) return "";
        return str.replace("\\","\\\\").replace(" ","\\ ").replace("'","\\'");
    }

    /**
     * escape like 模糊查条件参数转义，防止SQL注入，用于字符串拼接
     * @param str 需要转义的字符串
     * @return 转义后的字符串
     */
    public static String el(String str) {
        if(str == null) return "";
        return e(str).replace("%","\\%").replace("_","\\_");
    }

    /**
     * 执行查询SQL
     * @param sql SQL语句
     * @return 查询后的结果，JSONList类型，如果发生错误则返回null
     */
    public static JSONList query(String sql) {
        Connection conn = DB.getConnection();
        Statement st = null;
        ResultSet rs = null;

        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            return resultSetToList(rs);
        } catch (SQLException e) {
            Log.write("Error_DB",Log.getSQLException(e,sql));
            return null;
        } finally {
            DB.close(rs,st,conn);
        }
    }

    /**
     * 查询第一条数据
     * @param sql SQL语句
     * @return 查询后的结果，JSONMap类型，如果发生错误则返回null
     */
    public static JSONMap queryFirst(String sql) {
        JSONList query = DB.query(sql);

        if(query == null || query.isEmpty()) return null;

        return query.getMap(0);
    }

    /**
     * 查询第一个字段
     * @param sql SQL语句
     * @return 查询后的结果，String类型，如果发生错误则返回null
     */
    public static String queryFirstField(String sql) {
        JSONMap map = queryFirst(sql);

        if(map == null || map.isEmpty()) return null;

        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();

        if(iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            return  (String) entry.getValue();
        }

        return null;
    }

    /**
     * 增删改
     * @param sql SQL语句
     * @return 受影响的行数，如果发生错误则返回-1
     */
    public static int update(String sql) {
        Connection conn = DB.getConnection();
        Statement st = null;

        try {
            st = conn.createStatement();
            return st.executeUpdate(sql);
        } catch (SQLException e) {
            Log.write("Error_DB",Log.getSQLException(e,sql));
            return -1;
        } finally {
            DB.close(null, st, conn);
        }
    }

    /**
     * 通过事务增删改
     * @param sql SQL语句
     * @return 受影响的行数，如果发生错误则返回-1
     */
    public static int updateTransaction(String sql) {
        return updateTransaction(sql, null);
    }

    /**
     * 通过事务增删改
     * @param sql SQL语句
     * @param rollbackCondition 回滚条件，当函数返回值为true时进行回滚操作
     * @return 受影响的行数，如果发生错误则返回-1
     */
    // Supplier表示无参数，有返回值
    public static int updateTransaction(String sql, Supplier<Boolean> rollbackCondition) {
        Connection conn = DB.getConnection();
        Statement st = null;

        try {
            // 禁用自动提交模式
            conn.setAutoCommit(false);
            st = conn.createStatement();
            int n = st.executeUpdate(sql);

            // 检查是否需要回滚
            if (rollbackCondition != null && rollbackCondition.get()) {
                conn.rollback();
                return -1;
            }

            conn.commit();
            return n;
        } catch (SQLException e) {
            Log.write("Error_DB",Log.getSQLException(e,sql));

            try {
                conn.rollback();//使用回滚事务
            } catch (SQLException e1) {
                Log.write("Error_DB",Log.getSQLException(e1,sql));
            }

            return -1;
        } finally {

            //恢复每次DML操作的自动提交功能
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                Log.write("Error_DB",Log.getSQLException(e,sql));
            }

            DB.close(null, st, conn);
        }
    }

    /**
     * 添加数据并返回添加的数据的主键
     * @param sql SQL语句
     * @return 数据的主键，List类型，如果发生错误则返回null
     */
    public static List<Integer> getGeneratedKeys(String sql) {
        List<Integer> ids = new ArrayList<>();
        Connection conn = DB.getConnection();
        Statement st = null;
        ResultSet rs = null;

        try {
            conn.setAutoCommit(false);
            st = conn.createStatement();
            int n = st.executeUpdate(sql,st.RETURN_GENERATED_KEYS);
            conn.commit();

            if(n > 0) {
                rs = st.getGeneratedKeys();

                while(rs.next())
                    ids.add(rs.getInt(1));
            }

            return ids;
        } catch (SQLException e) {
            Log.write("Error_DB",Log.getSQLException(e,sql));

            try {
                conn.rollback();//使用回滚事务
            } catch (SQLException e1) {
                Log.write("Error_DB",Log.getSQLException(e1,sql));
            }

            return null;
        } finally {
            //恢复每次DML操作的自动提交功能
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                Log.write("Error_DB",Log.getSQLException(e,sql));
            }
            DB.close(rs, st, conn);
        }
    }

    /**
     * 执行SQL语句
     * @param sql SQL语句
     * @return 执行每条SQL语句，JSONList类型，如果是查询语句则返回查询后的结果，如果不是查询语句则返回受影响的行数
     * 如果发生错误则返回null
     */
    public static JSONList execute(String sql) {
        return execute(sql, null);
    }

    /**
     * 执行SQL语句
     * @param sql SQL语句
     * @param rollbackCondition 回滚条件，当函数返回值为true时进行回滚操作
     * @return 执行每条SQL语句，JSONList类型，如果是查询语句则返回查询后的结果，如果不是查询语句则返回受影响的行数
     * 如果发生错误则返回null
     */
    // Function表示有参数，有返回值
    public static JSONList execute(String sql, Function<JSONList, Boolean> rollbackCondition) {
        JSONList result = new JSONList();
        Connection conn = DB.getConnection();
        Statement st = null;
        ResultSet rs = null;

        try {
            conn.setAutoCommit(false);
            st = conn.createStatement();
            boolean hasResults = st.execute(sql,st.RETURN_GENERATED_KEYS);

            do {
                if(hasResults) {
                    rs = st.getResultSet();
                    result.add(resultSetToList(rs));//查询的结果集
                } else {
                    result.add(st.getUpdateCount());//增删改的条数
                }

                if(rs != null) rs.close();
                hasResults = st.getMoreResults();
            } while (hasResults || st.getUpdateCount() != -1);

            // 检查是否需要回滚
            if (rollbackCondition != null && rollbackCondition.apply(result)) {
                conn.rollback();
                return null;
            }

            conn.commit();
            return result;
        } catch (SQLException e) {
            Log.write("Error_DB",Log.getSQLException(e,sql));

            try {
                conn.rollback();//使用回滚事务
            } catch (SQLException e1) {
                Log.write("Error_DB",Log.getSQLException(e1,sql));
            }

            return null;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                Log.write("Error_DB",Log.getSQLException(e,sql));
            }

            DB.close(rs, st, conn);
        }
    }

    /**
     * 数据库是否存在某张表
     * @param tableName 表名
     * @return true存在
     */
    public static boolean isExistTable(String tableName) {
        JSONList tables = query("show tables");//查询数据库中的所有表
        if(tables == null) return false;
        boolean existTable = false;

        for(int i = 0;i < tables.size();i++) {
            Iterator<Map.Entry<String, Object>> iterator = tables.getMap(i).entrySet().iterator();

            if (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();

                if(tableName.equals(entry.getValue())) {
                    existTable = true;
                    break;
                }
            }
        }

        return existTable;
    }

    /**
     * 数据库的某张表是否存在某个字段
     * @param tableName 表名
     * @param fieldName 字段名
     * @return true存在
     */
    public static boolean isExistField(String tableName,String fieldName) {
        JSONList fields = DB.query("select column_name from information_schema.COLUMNS WHERE table_name='"+tableName+"'");//查询数据库表中的所有字段
        if(fields == null) return false;
        boolean existField = false;
        for(int i = 0;i < fields.size();i++) {
            if(fieldName.equals(fields.getMap(i).getString("COLUMN_NAME"))) {
                existField = true;
                break;
            }
        }
        return existField;
    }

    /**
     * 通过SQL获取响应的JSONMap
     * @param sql SQL语句
     * @return JSONMap类型
     * 成功：
     * {
     *      "IsSuccess": "1",//是否调用成功 0:否;1:是
     *      "data": [响应数据],
     *      "dataCount": "数据数量",
     *      "Msg": "响应信息"
     * }
     * 失败：
     * {
     *     "IsSuccess": "0",//是否调用成功 0:否;1:是
     *     "Msg": "响应信息"
     * }
     */
    public static JSONMap sqlToJSONMap(String sql) {
        return sqlToJSONMap(sql,null,null,null);
    }

    /**
     * 通过SQL获取响应的JSONMap
     * @param sql SQL语句
     * @param pageNo 页号
     * @param pageCount 页数量
     * @return JSONMap类型
     * 成功：
     * {
     *      "IsSuccess": "1",//是否调用成功 0:否;1:是
     *      "data": [响应数据],
     *      "dataCount": "数据数量",
     *      "Msg": "响应信息"
     * }
     * 失败：
     * {
     *     "IsSuccess": "0",//是否调用成功 0:否;1:是
     *     "Msg": "响应信息"
     * }
     */
    public static JSONMap sqlToJSONMap(String sql,String pageNo,String pageCount) {
        return sqlToJSONMap(sql,pageNo,pageCount,null);
    }

    /**
     * 通过SQL获取响应的JSONMap
     * @param sql SQL语句
     * @param pageNo 页号
     * @param pageCount 页数量
     * @param defaultCount 没有分页时的默认返回条数
     * @return JSONMap类型
     * 成功：
     * {
     *      "IsSuccess": "1",//是否调用成功 0:否;1:是
     *      "data": [响应数据],
     *      "dataCount": "数据数量",
     *      "Msg": "响应信息"
     * }
     * 失败：
     * {
     *     "IsSuccess": "0",//是否调用成功 0:否;1:是
     *     "Msg": "响应信息"
     * }
     */
    public static JSONMap sqlToJSONMap(String sql,String pageNo,String pageCount,String defaultCount) {
        String count = getQueryCount(sql);

        if (count == null) {
            return JSONMap.error("获取记录条数失败");
        }

        sql = StrUtil.removePrefix(sql,";");

        if(Valid.isInteger(pageNo) && Valid.isInteger(pageCount)) {
            sql += " limit "+ Num.integer(pageNo).subtract(Num.integer("1")).multiply(Num.integer(pageCount))+","+pageCount+" ";
        } else if(Valid.isInteger(defaultCount)) {
            sql += " limit "+defaultCount+" ";
        }

        JSONList list = query(sql);

        if(list == null) {
            return JSONMap.error("SQL执行失败");
        }

        return JSONMap.success(list,count,"SQL执行成功");
    }

    /**
     * 获取查询语句记录条数
     * @param sql 查询语句SQL语句
     * @return 查询语句记录条数
     */
    public static String getQueryCount(String sql) {
//        // 转换为小写
//        sql = sql.toLowerCase();
////        Pattern pattern = Pattern.compile("((\\sfrom\\s+(.*))|(\\sFROM\\s+(.*)))");//使用正则表达式匹配 "from" 或者 "FROM" 后面的内容（带from）
//        Pattern pattern = Pattern.compile("\\sfrom\\s+(.*)",Pattern.CASE_INSENSITIVE);//使用正则表达式匹配 "from" 后面的内容，不区分大小写（不带from）
//        Matcher matcher = pattern.matcher(sql.replaceAll("\\r|\\n|\\t"," "));
//
//        if (matcher.find()) {
//            sql = matcher.group(1);
//            System.out.println(sql);
//        }

        if(Valid.isBlank(sql)) {
            return null;
        }

        sql = "select count(1) 'count' from (" + sql + ") leaf_system_list_data_count";//构查询条数的 SQL 语句
        JSONMap count = queryFirst(sql);

        if(count == null || count.size() == 0) {
            Log.write("Error_DB",Log.content("ERROR","ErrorMsg:获取记录条数失败\n------\nSQL:\n"+SQLUtils.formatMySql(sql)+"\n"));
            return null;
        }

        return count.getString("count");
    }

    /**
     * ResultSet转换为JSONMap类型
     * @param rs 需要转换的ResultSet类型
     * @return 转换后的JSONMap类型
     * @throws SQLException 抛出SQL异常
     */
    public static JSONList resultSetToList(ResultSet rs) throws SQLException {
        JSONList result = new JSONList();
        JSONMap row;
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();//字段数量
        String[] columnNames = new String[columnCount];//字段名数组

        for(int i = 0;i < columnCount;i++) {
            columnNames[i] = metaData.getColumnLabel(i+1);//字段名
        }

        while(rs.next()) {
            row = new JSONMap();

            for(String columnName : columnNames) {
                row.put(columnName, rs.getString(columnName));
            }

            result.add(row);
        }

        return result;
    }

    /**
     * 获取Connection对象
     * @return 获取到的Connection对象，如果发生错误则返回null
     */
    public static Connection getConnection() {
        try {
            return DRUID_DATA_SOURCE.getConnection();//获取连接
        } catch (SQLException e1) {
            Log.write("Error_DB",Log.getSQLException(e1));
        }
        Log.write("Error_DB",Log.content("ERROR","jdbc获取连接失败，connection为空"));
        return null;
    }

    /**
     * 释放连接
     * @param rs ResultSet对象
     * @param st Statement对象
     * @param conn Connection对象
     */
    public static void close(ResultSet rs,Statement st,Connection conn) {
        try {
            if(rs != null) rs.close();
            if(st != null) st.close();
            if(conn != null)conn.close();
        } catch (SQLException e) {
            Log.write("Error_DB",Log.getSQLException(e));
        }
    }
}
