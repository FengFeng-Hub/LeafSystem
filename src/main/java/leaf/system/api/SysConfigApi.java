package leaf.system.api;

import leaf.common.DB;
import leaf.common.mysql.Where;
import leaf.common.object.JSONList;
import leaf.common.object.JSONMap;
import leaf.common.util.Num;
import leaf.common.util.Valid;
import leaf.system.annotate.LoginToken;
import leaf.system.common.Http;
import leaf.system.common.SysUser;
import leaf.system.interceptor.ApiGlobalInterceptor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * 系统配置模块
 */
@RestController
public class SysConfigApi {
    /**
     * 获取系统配置
     */
    @GetMapping("/system/api/systemConfig/getSystemConfig")
    public JSONMap getSystemConfig() {
        String configKeys = Http.param("ConfigKeys");
        JSONMap result = new JSONMap();
        int i;

        if(Valid.isEmpty(configKeys)) {
            return JSONMap.success(result);
        }

        String[] configKeyArr = configKeys.split(",");
        String configKeyStr = "",userRoleIdStr = SysUser.getUserRoleIdStr();

        for(i = 0;i < configKeyArr.length;i++) {
            if(i == configKeyArr.length - 1) {
                configKeyStr += "'"+DB.e(configKeyArr[i])+"'";
            } else {
                configKeyStr += "'"+DB.e(configKeyArr[i])+"',";
            }
        }

        // 访问等级
        String accessLevel = "";

        if (!Valid.isEmpty(SysUser.getBackendLoginId())) {
            accessLevel += "2,3,";
        } else if (!Valid.isEmpty(SysUser.getFrontendLoginId())) {
            accessLevel += "3,";
        }

        accessLevel += "4";

        String sql = "" +
                "select config_key,config_value " +
                "from sys_config " +
                "where access_level in (" + accessLevel + ") and config_key in (" + configKeyStr + ")";

        JSONList configs = DB.query(sql);

        if(configs == null) {
            return JSONMap.error("获取系统配置失败");
        }

        for(i = 0;i < configs.size();i++) {
            result.put(configs.getMap(i).getString("config_key"),configs.getMap(i).getString("config_value"));
        }

        return JSONMap.success(result);
    }
    /**
     * 获取系统配置列表
     */
    @GetMapping("/system/api/systemConfig/getSystemConfigList")
    @LoginToken(validBackend = true,permissionKey = "lspk:ls:systemConfig:list")
    public JSONMap getSystemConfigList() {
        String configId = Http.param("config_id");
        String configKey = Http.param("config_key");
        String configValue = Http.param("config_value");
        String configDesc = Http.param("config_desc");
        String configType = Http.param("config_type");
        String accessLevel = Http.param("access_level");
        String sortField = Http.param("SortField");
        String sortOrder = Http.param("SortOrder");
        String pageNo = Http.param("PageNo");
        String pageCount = Http.param("PageCount");

//        String sql = "" +
//                "select a.config_id,config_key,config_value,config_desc,config_type,a.access_level,a.memo," +
//                "   a.ls_create_time,a.ls_create_by,a.ls_update_time,a.ls_update_by,c.role_id,c.role_name " +
//                "from (" +
//                "       select aa.config_id,aa.config_key,aa.config_value,aa.config_desc,aa.config_type,aa.access_level,aa.memo," +
//                "           aa.ls_create_time,ab.name 'ls_create_by',aa.ls_update_time,ac.name 'ls_update_by' " +
//                "       from sys_config aa " +
//                "           left join sys_user ab on aa.ls_create_by = ab.user_id " +
//                "           left join sys_user ac on aa.ls_update_by = ac.user_id ";
//
//        if(Valid.isInteger(pageNo) && Valid.isInteger(pageCount)) {
//            sql += "limit "+ Num.integer(pageNo).subtract(Num.integer("1")).multiply(Num.integer(pageCount))+","+pageCount+" ";
//        } else {
//            sql += "limit 100 ";
//        }
//
//        sql += "   ) a " +
//                "   left join sys_config_role_rel b on a.config_id = b.config_id " +
//                "   left join sys_role c on b.role_id = c.role_id ";

        String sql = "" +
                "select a.config_id,config_key,config_value,config_desc,config_type,a.access_level,a.memo," +
                "   a.ls_create_time,b.name 'ls_create_by',a.ls_update_time,c.name 'ls_update_by' " +
                "from sys_config a " +
                "   left join sys_user b on a.ls_create_by = b.user_id " +
                "   left join sys_user c on a.ls_update_by = c.user_id ";

        Where.Operator relationship = Where.Operator.LIKE;

        if("1".equals(Http.param("IsEqual","0"))) {
            relationship =  Where.Operator.EQ;
        }
        sql += new Where(true)
                .and().add("a.config_id", configId, Where.Operator.EQ)
                .or().add("a.config_key", configKey, relationship)
                .or().add("a.config_value", configValue, relationship)
                .or().add("config_desc", configDesc, relationship).group()
                .and().eq("ifnull(config_type,1)", configType)
                .and().eq("access_level", accessLevel).prependWhere().toString();

        //排序字段
        switch(sortField) {
            case "config_id":
            case "config_key":
                if(sortOrder.equals("asc")) sql += " order by a." + sortField + " asc ";
                else if(sortOrder.equals("desc")) sql += " order by a." + sortField+" desc ";
                else sql += " order by a.config_id desc ";
                break;
            default:
                sql += " order by a.config_id desc ";
        }

        return DB.sqlToJSONMap(sql,Http.param("PageNo"),Http.param("PageCount"));
    }
    /**
     * 修改系统配置列表
     */
    @PostMapping("/system/api/systemConfig/updateSystemConfig")
    @LoginToken(validBackend = true)
    public JSONMap updateSystemConfig() {
        String updateType = Http.param("UpdateType");
        String configId = Http.param("config_id");
        String configKey = Http.param("config_key");
        String configValue = Http.param("config_value");
        String configDesc = Http.param("config_desc");
        String configType = Http.param("config_type");
        String accessLevel = Http.param("access_level");
        String memo = Http.param("memo");
        String roleIds = Http.param("role_id_arr");
        String[] roleIdArr = roleIds.split(",");
        String backendLoginId = SysUser.getBackendLoginId();

        switch(updateType) {
            case "Edit":
                if (!ApiGlobalInterceptor.permission("lspk:ls:systemConfig:edit")) {
                    Http.write(403, JSONMap.error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if (configId.isEmpty()) {
                    return JSONMap.error("配置代码不能为空");
                }

                if (configKey.isEmpty()) {
                    return JSONMap.error("配置键不能为空");
                }

                switch (configType) {
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                    case "5":
                    case "6":
                        break;
                    default:
                        return JSONMap.error("配置类型有误");
                }

                switch (accessLevel) {
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                        break;
                    default:
                        return JSONMap.error("访问等级类型有误");
                }

                configId = DB.e(configId);
                String sql = "" +
                        "update sys_config " +
                        "set config_key = '" + DB.e(configKey) + "',config_value = '" + DB.e(configValue) + "',config_desc = '" + DB.e(configDesc) +"'," +
                        "   config_type = '" + configType + "',access_level = '" + accessLevel + "',memo = '" + DB.e(memo) + "'," +
                        "   ls_update_time = now(),ls_update_by = '" + backendLoginId + "'" +
                        "where config_id = '" + configId + "';" +
                        "delete from sys_config_role_rel where config_id = '" + configId + "';";

                for (String roleId : roleIdArr) {
                    if (!roleId.isEmpty()) sql += "insert into sys_config_role_rel(config_id,role_id) value('" + configId + "','" + DB.e(roleId) + "');";
                }

                if (DB.updateTransaction(sql) > 0) {
                    return JSONMap.success();
                }
                break;
            case "Add":
                if(!ApiGlobalInterceptor.permission("lspk:ls:systemConfig:add")) {
                    Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if (configKey.isEmpty()) {
                    return JSONMap.error("配置键不能为空");
                }

                switch (configType) {
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                    case "5":
                    case "6":
                        break;
                    default:
                        return JSONMap.error("配置类型有误");
                }


                switch (accessLevel) {
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                        break;
                    default:
                        return JSONMap.error("访问等级类型有误");
                }

                sql = "" +
                        "insert into sys_config(config_key,config_value,config_desc,config_type,access_level,memo,ls_create_time,ls_create_by)" +
                        "value('"+DB.e(configKey)+"','"+DB.e(configValue)+"','"+DB.e(configDesc)+"','"+configType+"','" + accessLevel + "','" + DB.e(memo) + "'," +
                        "   now(),'" + backendLoginId + "');" +
                        "select @new_config_id:=last_insert_id();";

                for(String roleId : roleIdArr) {
                    if(!roleId.isEmpty()) sql += "insert into sys_config_role_rel(config_id,role_id) value(@new_config_id,'"+DB.e(roleId)+"');";
                }

                if(DB.updateTransaction(sql) > 0) {
                    return JSONMap.success();
                }
                break;
            case "Delete":
                if(!ApiGlobalInterceptor.permission("lspk:ls:systemConfig:delete")) {
                    Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if (configKey.isEmpty()) {
                    return JSONMap.error("配置键不能为空");
                }

                if(DB.update("delete from sys_config where config_id = '"+DB.e(configId)+"'") > 0) {
                    return JSONMap.success();
                }
                break;
            default:
                return JSONMap.error("修改类型有误");
        }
        return JSONMap.error("操作失败");
    }
}
