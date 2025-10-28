package leaf.system.api;

import leaf.common.mysql.Where;
import leaf.system.common.Http;
import leaf.system.common.SysUser;
import leaf.common.DB;
import leaf.common.object.JSONList;
import leaf.common.object.JSONMap;
import leaf.common.util.Valid;
import leaf.system.annotate.LoginToken;
import leaf.system.common.SysCommon;
import leaf.system.interceptor.ApiGlobalInterceptor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜单模块
 */
@RestController
public class SysMenuApi {
    /**
     * 获取菜单tree
     */
    @GetMapping("/system/api/menu/getMenuTree")
    @LoginToken(validBackend = true)
//    @Cacheable(cacheNames = "SpringCache_system_menu",key = "'MenuTree'")//如果使用该注解，第一次从数据库拿数据，之后从缓存拿数据
    public JSONMap getMenuTree() {
//        System.out.println(Log.content("DEBUG","获取菜单tree没有走缓存"));
        String sql = "",userRoleIdStr = SysUser.getUserRoleIdStr();
        if("1".equals(userRoleIdStr) || userRoleIdStr.startsWith("1,")) {
            sql = "" +
                    "select menu_id,menu_name,parent_menu_id,menu_icon,url,type,is_show " +
                    "from sys_menu " +
                    "where type in (1,2,3,4,5) " +
                    "order by sort desc";
        } else {
            sql = "" +
                    "select distinct a.menu_id,a.menu_name,a.parent_menu_id,a.menu_icon,a.url,a.type,a.is_show " +
                    "from sys_menu a " +
                    "   left join sys_role_menu_rel b on a.menu_id = b.menu_id " +
                    "where type in (1,2,3,4,5) and b.role_id in (" + DB.e(userRoleIdStr) + ") " +
                    "order by sort desc";
        }
        JSONList menus = DB.query(sql);
        menus = menus.listToTree("menu_id","parent_menu_id","child_menu",
                false,true,false);
        return JSONMap.success(menus);
    }
    /**
     * 获取菜单列表
     */
    @GetMapping("/system/api/menu/getMenuList")
    @LoginToken(validBackend = true,permissionKey = "lspk:ls:menu:list")
    public JSONMap getMenuList() {
        String menuId = Http.param("menu_id");
        String menuName = Http.param("menu_name");
        String parentMenuId = Http.param("parent_menu_id");
        String parentMenuName = Http.param("parent_menu_name");
        String typeArr = Http.param("type_arr");
        String isShow = Http.param("is_show");
        String sortField = Http.param("SortField");
        String sortOrder = Http.param("SortOrder");

        //验证type参数
        if(!typeArr.isEmpty()) {
            String[] typeArrSplit = typeArr.split(",");
            for(String type:typeArrSplit) {
                if(!Valid.isIntType(type)) return JSONMap.error("获取菜单列表失败，type参数不正确");
            }
        }

        String sql = "" +
                "select a.menu_id,a.menu_name,a.parent_menu_id,b.menu_name parent_menu_name,a.menu_icon,a.url,a.type,a.permission_key,a.is_show,a.sort,a.memo, " +
                "   a.ls_create_time,c.name 'ls_create_by',a.ls_update_time,d.name 'ls_update_by' " +
                "from sys_menu a " +
                "   left join sys_menu b on a.parent_menu_id = b.menu_id " +
                "   left join sys_user c on a.ls_create_by = c.user_id " +
                "   left join sys_user d on a.ls_update_by = d.user_id ";
//        String relationship = SQLWhere.Like;
//
//        if("1".equals(Http.param("IsEqual","0"))) {
//            relationship = SQLWhere.Equal;
//        }
//
//        sql += new SQLWhere().addOr("a.menu_id",menuId,relationship)
//                .addOr("a.menu_name",menuName,relationship)
//                .addOr("a.parent_menu_id",parentMenuId,relationship)
//                .addOr("b.menu_name",parentMenuName,relationship).addBracket("and")
//                .addAnd("a.type",typeArr,SQLWhere.In)
//                .addAnd("a.is_show",isShow,"=").toString();

        Where.Operator relationship = Where.Operator.LIKE;

        if("1".equals(Http.param("IsEqual","0"))) {
            relationship = Where.Operator.EQ;
        }

        sql += new Where(true)
                .or().add("a.menu_id",menuId, Where.Operator.EQ)
                .or().add("a.menu_name",menuName,relationship)
                .or().add("a.parent_menu_id",parentMenuId,relationship)
                .or().add("b.menu_name",parentMenuName,relationship).group()
                .and().in("a.type",typeArr)
                .and().eq("a.is_show",isShow).prependWhere().toString();

        //排序字段
        switch(sortField) {
            case "menu_id":
            case "parent_menu_id":
            case "type":
            case "is_show":
            case "sort":
                if(sortOrder.equals("asc")) sql += " order by a." + sortField + " asc ";
                else if(sortOrder.equals("desc")) sql += " order by a." + sortField+" desc ";
                else sql += " order by a.sort desc ";
                break;
            default:
                sql += " order by a.sort desc ";
        }
        return DB.sqlToJSONMap(sql,Http.param("PageNo"),Http.param("PageCount"));
    }
    /**
     * 修改菜单
     */
    @PostMapping("/system/api/menu/updateMenu")
    @LoginToken(validBackend = true)
//    @CacheEvict(cacheNames = "SpringCache_system_menu",key = "'MenuTree'")//修改菜单后会删除getMenuTree接口返回值的缓存
    public JSONMap updateMenu() {
        String updateType = Http.param("UpdateType");
        String menuId = Http.param("menu_id");
        String menuName = Http.param("menu_name");
        String parentMenuId = Http.param("parent_menu_id");
        String menuIcon = Http.param("menu_icon");
        String url = Http.param("url");
        String type = Http.param("type");
        String permissionKey = Http.param("permission_key");
        String isShow = Http.param("is_show");
        String memo = Http.param("memo");
        String newSort = Http.param("new_sort");
        String backendLoginId = SysUser.getBackendLoginId();

        switch(updateType) {
            case "Edit":
                if(!ApiGlobalInterceptor.permission("lspk:ls:menu:edit")) {
                    Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if(menuId.isEmpty()) {
                    return JSONMap.error("菜单代码不能为空");
                }

                if(menuName.isEmpty()) {
                    return JSONMap.error("菜单名称不能为空");
                }

                if(!"0".equals(isShow) && !"1".equals(isShow)) {
                    return JSONMap.error("是否显示参数类型有误");
                }

                if(!Valid.isIntType(type)) {
                    return JSONMap.error("类型参数类型有误");
                }

                String sql = "" +
                        "update sys_menu " +
                        "set menu_name = '"+DB.e(menuName)+"',parent_menu_id = "+(parentMenuId.isEmpty() || !Valid.isInteger(parentMenuId)?"null":parentMenuId)+"," +
                        "   menu_icon = '"+DB.e(menuIcon)+"',url = '"+DB.e(url)+"',type = '"+type+"',permission_key = '"+DB.e(permissionKey)+"',is_show = '"+isShow+"',memo = '" + memo + "'," +
                        "   ls_update_time = now(),ls_update_by = '" + backendLoginId + "' " +
                        "where menu_id = '"+DB.e(menuId)+"'";

                if(DB.update(sql) > 0) {
                    return JSONMap.success();
                }
                break;
            case "Add":
                if(!ApiGlobalInterceptor.permission("lspk:ls:menu:add")) {
                    Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if(menuName.isEmpty()) {
                    return JSONMap.error("菜单名称不能为空");
                }

                if(!"0".equals(isShow) && !"1".equals(isShow)) {
                    return JSONMap.error("是否显示参数类型有误");
                }

                if(!Valid.isIntType(type)) {
                    return JSONMap.error("类型参数类型有误");
                }

                sql = "" +
                        "insert into sys_menu(menu_name,parent_menu_id,menu_icon,url,type,permission_key,is_show,memo,ls_create_time,ls_create_by)" +
                        "value('"+DB.e(menuName)+"',"+(parentMenuId.isEmpty() || !Valid.isInteger(parentMenuId)?"null":parentMenuId)+"," +
                        "   '"+DB.e(menuIcon)+"','"+DB.e(url)+"','"+type+"','"+DB.e(permissionKey)+"','"+isShow+"','" + memo + "'," +
                        "   now(),'" + backendLoginId + "');" +
                        "select @new_menu_id:=last_insert_id() 'new_menu_id';" +
                        "update sys_menu set sort = @new_menu_id where menu_id = @new_menu_id;";
                JSONList result = DB.execute(sql);

                if(result != null) return JSONMap.success(result.getList(1).get(0));
//                if(DB.updateTransaction(sql) > 0) return JSONMap.success();
                break;
            case "Delete":
                if(!ApiGlobalInterceptor.permission("lspk:ls:menu:delete")) {
                    Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if(menuId.isEmpty()) {
                    return JSONMap.error("菜单代码不能为空");
                }

                if(DB.update("delete from sys_menu where menu_id = '"+DB.e(menuId)+"'") > 0) {
                    return JSONMap.success();
                }
                break;
            case "Sort":
                if(!ApiGlobalInterceptor.permission("lspk:ls:menu:edit:sort")) {
                    Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                    return null;
                }

                synchronized(this) {
                    if(menuId.isEmpty()) {
                        return JSONMap.error("菜单代码不能为空");
                    }

                    if(newSort.isEmpty()) {
                        return JSONMap.error("新排序不能为空");
                    }

                    return SysCommon.updateSort("sys_menu","menu_id",menuId,newSort);
                }
            case "IsShow":
                if(!ApiGlobalInterceptor.permission("lspk:ls:menu:edit:isShow")) {
                    Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if(menuId.isEmpty()) {
                    return JSONMap.error("菜单代码不能为空");
                }

                if("1".equals(isShow)) {
                    isShow = "1";
                } else {
                    isShow = "0";
                }

                if(DB.update("" +
                        "update sys_menu " +
                        "set is_show = "+isShow+", ls_update_time = now(), ls_update_by = '" + backendLoginId + "' " +
                        "where menu_id = '"+DB.e(menuId)+"'") > 0) {
                    return JSONMap.success();
                }
                break;
            default:
                return JSONMap.error("修改类型有误");
        }
        return JSONMap.error("操作失败");
    }
}
