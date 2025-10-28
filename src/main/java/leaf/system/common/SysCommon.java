package leaf.system.common;

import leaf.common.Config;
import leaf.common.DB;
import leaf.common.Log;
import leaf.common.mysql.SQLWhere;
import leaf.common.object.*;
import leaf.common.util.DateTime;
import leaf.common.util.Lock;

/**
 * 系统通用
 */
public class SysCommon {
    /**
     * 获取系统配置
     * @param key 配置键
     * @return 配置值
     */
    public static String getSystemConfig(String key) {
        String sql = "" +
                "select config_value " +
                "from sys_config " +
                "where config_key = '" + key + "'";

        return DB.queryFirstField(sql);
    }
    /**
     * 获取系统配置
     * @param keys 配置键
     * @return 配置值
     */
    public static JSONMap getSystemConfigs(String ... keys) {
        JSONMap result = new JSONMap();
        if(keys.length == 0) {
            return result;
        }

        String sql = "" +
                "select config_key,config_value " +
                "from sys_config ";
        SQLWhere conditionSql = new SQLWhere();

        for(String key:keys) {
            conditionSql = conditionSql.addOr("config_key",key,"=");
        }

        sql += conditionSql.toString();
        JSONList configs = DB.query(sql);

        if(configs == null) {
            Log.write("Error_system","=================== Error ===================\n" +
                    "Time:"+ DateTime.now("yyyy-MM-dd HH:mm:ss")+"\nMessage:获取系统配置失败");
            return result;
        }
        for(int i = 0;i < configs.size();i++) {
            result.put(configs.getMap(i).getString("config_key"),configs.getMap(i).getString("config_value"));
        }
        return result;
    }

    /**
     * 检查验证码
     * @param validParam 验证参数
     * @return 1:验证成功 2:验证失败 3:验证超时
     */
    public static int checkValidCode(String validParam,String text) {
        validParam = Lock.aesDncrypt(validParam);

        if(validParam == null || validParam.equals("") || text == null || text.equals("")) return 2;

        String _validParam = Cache.getString(validParam);

        if(_validParam == null) return 3;
        //群全部转成小写后比较
        if(text.toLowerCase().equals(_validParam.toLowerCase())) {
            Cache.remove(validParam);
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * 检查验证拼图
     * @param validParam 验证参数
     * @param x 横坐标
     * @param range 有效范围
     * @return 1:验证成功 2:验证失败 3:验证超时
     */
    public static int checkValidPuzzle(String validParam, int x, String range) {
        validParam = Lock.aesDncrypt(validParam);

        if(validParam == null) return 2;

        String _validParam = Cache.getString(validParam);

        if(_validParam == null) return 3;

        int _x = 0;

        try {
            _x = Integer.parseInt(_validParam);
        } catch(Exception e) {
            return 2;
        }

        int _range = 5;

        try {
            _range = Integer.parseInt(range);
        } catch(Exception e) {}

        if(Math.abs(x - _x) <= _range) {
            Cache.remove(validParam);
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * 修改排序
     * @param tableName 表名
     * @param primaryKeyFieldName 主键字段名
     * @param id 主键代码
     * @param newSort 新排序
     * @return JSONMap响应信息
     */
    public static JSONMap updateSort(String tableName,String primaryKeyFieldName,String id, String newSort) {
        String sort = DB.queryFirstField("select sort from "+tableName+" where "+primaryKeyFieldName+" = '"+DB.e(id)+"'");

        if(sort == null) {
            return JSONMap.error("获取排序信息失败");
        }

        int _sort,_newSort;//当前排序和新排序

        try {
            _sort = Integer.parseInt(sort);
            _newSort = Integer.parseInt(newSort);
        } catch(Exception e) {
            return JSONMap.error("获取排序信息失败");
        }

        if(_sort < _newSort) {//向上移
            sort = DB.queryFirstField("select max(sort) from "+tableName);//最大排序

            if(sort == null) {
                return JSONMap.error("获取排序信息失败");
            }

            int max;

            try {
                max = Integer.parseInt(sort);
            } catch(Exception e) {
                return JSONMap.error("获取排序信息失败");
            }

            if(_sort >= max) {
                return JSONMap.error("排序失败，这已经是第一条数据");
            }

            if(DB.updateTransaction("update "+tableName+" set sort = sort - 1 where sort > "+_sort+" and sort <= "+_newSort+";" +
                    "update "+tableName+" set sort = "+newSort+" where "+primaryKeyFieldName+" = '"+DB.e(id)+"'") != -1) {
                return JSONMap.success("排序成功");
            }
        } else if(_sort > _newSort) {//向下移
            if(_sort <= 1) {
                return JSONMap.error("排序失败，该行已经到底部了");
            }

            if(DB.updateTransaction("update "+tableName+" set sort = sort + 1 where sort < "+_sort+" and sort >= "+_newSort+";" +
                    "update "+tableName+" set sort = "+_newSort+" where "+primaryKeyFieldName+" = '"+DB.e(id)+"'") != -1) {
                return JSONMap.success("排序成功");
            }
        } else {
            return JSONMap.success("排序成功");
        }

        return JSONMap.error("排序失败");
    }
//    /**
//     * 添加排序SQL
//     * @param tableName 数据库表名
//     * @return order by SQL语句
//     */
//    public static String addOrderBySQL(String tableName) {
//        return addOrderBySQL(tableName,null);
//    }
//    /**
//     * 添加排序SQL
//     * @param tableName 数据库表名
//     * @param tableAlias 表格别名
//     * @return order by SQL语句
//     */
//    public static String addOrderBySQL(String tableName,String tableAlias) {
//        String sortField = Http.param("SortField");
//        String sortOrder = Http.param("SortOrder");
//
//        if(!Valid.isEmpty(tableAlias)) tableAlias = tableAlias + ".";
//        else tableAlias = "";
//
//        //按字段排序
//        if(!Valid.isEmptyAll(sortField,sortOrder)) {
//            //查看字段是否存在
//            if(DB.isExistField(tableName,sortField)) {
//                if(sortOrder.equals("asc")) {
//                    return " order by " + tableAlias + sortField + " asc ";
//                } else if(sortOrder.equals("desc")) {
//                    return " order by " + tableAlias + sortField+" desc ";
//                }
//            }
//        }
//        return "";
//    }
}
