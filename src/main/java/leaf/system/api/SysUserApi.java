package leaf.system.api;

import jakarta.servlet.http.Part;
import leaf.common.IO;
import leaf.common.mysql.Where;
import leaf.common.util.Num;
import leaf.common.util.Rdm;
import leaf.system.common.Http;
import leaf.system.model.ApiResponse;
import leaf.system.model.SysUser;
import leaf.common.DB;
import leaf.common.Log;
import leaf.common.object.JSONList;
import leaf.common.object.JSONMap;
import leaf.common.util.DateTime;
import leaf.common.util.Valid;
import leaf.system.annotate.LoginToken;
import leaf.system.common.SysCommon;
import leaf.system.interceptor.ApiGlobalInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

/**
 * 用户模块
 */
@RestController
public class SysUserApi extends Http {
    @Autowired
    private Environment environment;

    /**
     * 登录
     */
    @PostMapping("/system/api/user/login")
    public ApiResponse login() {
        String account = param("account");
        String password = param("password");
        String isBackend = param("IsBackend","0");
        String isSaveLoginStatus = param("IsSaveLoginStatus","0");

        if(account.isEmpty() || password.isEmpty()) {
            return error("账号或密码不能为空");
        }

        password = SysUser.md5Pwd(password);

//        String sql = "" +
//                "select a.user_id,a.is_disable,c.is_disable 'role_is_disable',d.is_allow_login_backend " +
//                "from sys_user a " +
//                "   left join sys_user_role_rel b on a.user_id = b.user_id " +
//                "   left join sys_role c on b.role_id = c.role_id and c.is_disable = '1' " +
//                "   left join sys_role d on b.role_id = d.role_id and d.is_allow_login_backend = '1' " +
//                "where a.account = '"+DB.e(account)+"' and a.password = '"+DB.e(password)+"'";
        String sql = "" +
                "select a.user_id, a.is_disable, " +
//                角色是否禁用 (如果所有角色禁用，返回1，否则返回0)
                "    case " +
                "        when count(case when c.is_disable = 1 then 1 end) = count(c.role_id) then 1 " +
                "        else 0 " +
                "    end as role_is_disable, " +
//                是否允许后台登录 (在未禁用的角色中至少有一个允许后台登录，返回1，否则返回0)
                "    case " +
                "        when count(case when (ifnull(c.is_disable,0) = 0 and c.is_allow_login_backend = 1) then 1 end) > 0 then 1 " +
                "        else 0 " +
                "    end as is_allow_login_backend " +
                "from sys_user a " +
                "left join sys_user_role_rel b on a.user_id = b.user_id " +
                "left join sys_role c on b.role_id = c.role_id " +
                "where  a.account = '" + DB.e(account) + "' and a.password = '" + DB.e(password) + "' " +
                "group by a.user_id";

        //查询用户信息
        JSONMap user = DB.queryFirst(sql);

        if(user == null) {
            return error("登录失败，账号或密码错误");
        }

        if("1".equals(user.get("is_disable")) || "1".equals(user.get("role_is_disable"))) {
            return error("该用户已被禁用");
        }

        //检查后台用户的验证码
        if(isBackend.equals("1") && "1".equals(SysCommon.getSystemConfig("EnableBackendLoginValid"))) {
            int result = SysCommon.checkValidCode(param("ValidParam"),param("Text"));

            if(result == 2) {
                return error("验证码有误");
            } else if(result == 3) {
                return error("验证超时，请刷新验证码后重新验证");
            }

            if (!"1".equals(user.get("is_allow_login_backend"))) {
                return error("该用户不允许登录后台");
            }
        }

        String userId = user.getString("user_id");
        int time = -1;

        if("1".equals(isBackend)) {
            if("1".equals(isSaveLoginStatus)) {
                try {
                    time = Integer.parseInt(environment.getProperty("leaf.login.backend_time", "2592000"));
                } catch(Exception ignored) {}
            }

            SysUser.setBackendLoginId(userId,time);
        } else {
            if("1".equals(isSaveLoginStatus)) {
                try {
                    time = Integer.parseInt(environment.getProperty("leaf.login.frontend_time", "7776000"));
                } catch(Exception ignored) {}
            }
            SysUser.setFrontendLoginId(userId,time);
        }

        //修改登录IP以及登录时间
        if(DB.update("" +
                "update sys_user " +
                "set login_ip = '"+getIpAdrress()+"',login_time = '"+DateTime.now("yyyy-MM-dd HH:mm:ss")+"' " +
                "where user_id = '"+userId+"'") == 0) {
            Log.write("Error_system","修改用户登录IP和用户登录时间失败，用户代码：" + userId);
        }

        return success();
    }
    /**
     * 退出登录
     */
    @GetMapping("/system/api/user/logout")
    public ApiResponse logout() {
        if("1".equals(param("IsBackend", "0"))) {
            SysUser.removeBackendLoginId();
        } else {
            SysUser.removeFrontendLoginId();
        }

        return success();
    }
    /**
     * 是否登录
     */
    @GetMapping("/system/api/user/isLogin")
    public ApiResponse isLogin() {
        String isBackend = param("IsBackend", "0");
        String userId;

        if("1".equals(isBackend)) {
            userId = SysUser.getBackendLoginId();
        } else {
            userId = SysUser.getFrontendLoginId();
        }

        JSONList userAll = DB.query("" +
                "select a.name,a.account,a.personal_signature,a.avatar,a.birthday,a.phone,a.email,a.sex,a.is_disable,c.role_id,c.role_name " +
                "from sys_user a " +
                "   left join sys_user_role_rel b on a.user_id = b.user_id " +
                "   left join sys_role c on b.role_id = c.role_id " +
                "where a.user_id = '" + userId + "'");

        if(userAll == null || userAll.isEmpty()) {
            return error("未登录");
        }

        JSONMap user = null;
        JSONMap role = null;

        for (int i = 0;i < userAll.size();i++) {
            if (i == 0) {
                user = userAll.getMap(0);
                user.put("role_list", new JSONList());
            }

            String roleId = userAll.getMap(i).getString("role_id");

            if (roleId != null) {
                role = new JSONMap();
                role.put("role_id", roleId);
                role.put("role_name", userAll.getMap(i).getString("role_name"));
                user.getList("role_list").add(role);
            }
        }

        user.remove("role_id");
        user.remove("role_name");
        return success(user);
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/system/api/user/getUserList")
    @LoginToken(validBackend = true,permissionKey = "lspk:ls:user:list")
    public ApiResponse getUserList() {
        String userId = param("user_id");
        String name = param("name");
        String account = param("account");
        String phone = param("phone");
        String email = param("email");
        String realName = param("real_name");
        String idcard = param("idcard");
        String sex = param("sex");
        String isDisable = param("is_disable");
        String loginIp = param("login_ip");
        String roleIdArr = param("role_id_arr");
        String sortField = param("SortField");
        String sortOrder = param("SortOrder");
        String pageNo = param("PageNo");
        String pageCount = param("PageCount");

        if (!sex.isEmpty()) {
            sex = Where.joinIn(sex.split(","));
        }

        if (!roleIdArr.isEmpty()) {
            roleIdArr = Where.joinIn(roleIdArr.split(","));
        }

        Where.Operator relationship = Where.Operator.LIKE;

        if("1".equals(param("IsEqual","0"))) {
            relationship = Where.Operator.EQ;
        }

        String sqlWhere = new Where(true)
                .and().add("aa.user_id", userId, Where.Operator.EQ)
                .or().add("aa.name", name, relationship).group()
                .and().add("aa.account", account, relationship)
                .and().add("aa.phone", phone, relationship)
                .and().add("aa.email", email, relationship)
                .and().add("aa.real_name", realName, relationship)
                .and().add("aa.idcard", idcard, relationship)
                .and().in("aa.sex", sex)
                .and().eq("ifnull(aa.is_disable,0)", isDisable)
                .and().add("aa.login_ip", loginIp, relationship)
                .and().in("ab.role_id", roleIdArr).prependWhere().toString();

        //主表SQL语句
        String masterListSql = "" +
                "select aa.user_id,aa.name,aa.account,aa.personal_signature,aa.avatar,aa.birthday,aa.phone,aa.email," +
                "   aa.real_name,aa.idcard,aa.sex,aa.is_disable,aa.login_ip,aa.login_time,ab.role_id " +
                "from sys_user aa " +
                "   left join sys_user_role_rel ab on aa.user_id = ab.user_id ";

        masterListSql += sqlWhere;

        masterListSql += "group by aa.user_id ";

        //排序字段
        switch(sortField) {
            case "user_id":
            case "account":
            case "birthday":
            case "phone":
            case "email":
            case "idcard":
            case "sex":
            case "login_time":
            case "create_time":
            case "is_disable":
                if(sortOrder.equals("asc")) masterListSql += " order by aa." + sortField + " asc";
                else if(sortOrder.equals("desc")) masterListSql += " order by aa." + sortField+" desc";
                else masterListSql += " order by aa.user_id desc";
                break;
            default:
                masterListSql += " order by aa.user_id desc";
        }

        String sql = "" +
                "select a.user_id,name,account,personal_signature,avatar,birthday,phone,email,real_name,idcard," +
                "   sex,a.is_disable,login_ip,login_time,c.role_id,c.role_name " +
                "from (" +
                masterListSql;

        if(Valid.isInteger(pageNo) && Valid.isInteger(pageCount)) {
            sql += " limit "+ Num.integer(pageNo).subtract(Num.integer("1")).multiply(Num.integer(pageCount))+","+pageCount+" ";
        } else {
            sql += " limit 100 ";
        }

        sql += "" +
                "   ) a " +
                "   left join sys_user_role_rel b on a.user_id = b.user_id " +
                "   left join sys_role c on b.role_id = c.role_id ";

        JSONList userList = DB.query(sql);
        JSONList userData = new JSONList();
        JSONMap userTemp = new JSONMap();
        JSONMap user;
        JSONMap role;

        for(int i = 0;i < userList.size();i++) {
            String userId2 = userList.getMap(i).getString("user_id");
            user = userTemp.getMap(userId2);

            if(user == null) {
                user = new JSONMap();
                user.put("user_id",userList.getMap(i).get("user_id"));
                user.put("name",userList.getMap(i).get("name"));
                user.put("account",userList.getMap(i).get("account"));
                user.put("personal_signature",userList.getMap(i).get("personal_signature"));
                user.put("avatar",userList.getMap(i).get("avatar"));
                user.put("birthday",userList.getMap(i).get("birthday"));
                user.put("phone",userList.getMap(i).get("phone"));
                user.put("email",userList.getMap(i).get("email"));
                user.put("real_name",userList.getMap(i).get("real_name"));
                user.put("idcard",userList.getMap(i).get("idcard"));
                user.put("sex",userList.getMap(i).get("sex"));
                user.put("is_disable",userList.getMap(i).get("is_disable"));
                user.put("login_ip",userList.getMap(i).get("login_ip"));
                user.put("login_time",userList.getMap(i).get("login_time"));
                user.put("role_list",new JSONList());
                userData.add(user);
                userTemp.put(userId2,user);
            }

            if(userList.getMap(i).getString("role_id") != null) {
                role = new JSONMap();
                role.put("role_id",userList.getMap(i).get("role_id"));
                role.put("role_name",userList.getMap(i).get("role_name"));
                user.getList("role_list").add(role);
            }
        }

        String count = DB.getQueryCount(masterListSql);

        if(count == null) {
            return error("获取记录条数失败");
        }

        return success(userData).count(count);
    }

    /**
     * 修改用户
     */
    @PostMapping("/system/api/user/updateUser")
    @LoginToken(validBackend = true)
    public ApiResponse updateUser() {
        String updateType = param("UpdateType");
        String userId = param("user_id");
        String name = param("name");
        String account = param("account");
        String password = param("password");
        String personalSignature = param("personal_signature");
        String avatar = param("avatar");
        String birthday = param("birthday");
        String phone = param("phone");
        String email = param("email");
        String realName = param("real_name");
        String idcard = param("idcard");
        String sex = param("sex", "3");
        String isDisable = param("is_disable");
        String loginTime = param("login_time");
        String roleIds = param("role_id_arr");
        String[] roleIdArr = roleIds.split(",");

        if("1".equals(userId)) {
            return error("没有权限操作该用户");
        }

        switch(updateType) {
            case "Edit":
                if(!ApiGlobalInterceptor.permission("lspk:ls:user:edit")) {
                    write(403,error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if(userId.isEmpty()) {
                    return error("用户代码不能为空");
                }

                if(name.isEmpty()) {
                    return error("名称不能为空");
                }

                if(account.isEmpty()) {
                    return error("账号不能为空");
                }

                if (DateTime.strToDate(birthday,"yyyy-MM-dd") == null) {
                    birthday = "null";
                } else {
                    birthday = "'"+birthday+"'";
                }

                if (DateTime.strToDate(loginTime, "yyyy-MM-dd HH:mm:ss") == null) {
                    loginTime = "null";
                } else {
                    loginTime = "'"+loginTime+"'";
                }

                if(!("1".equals(sex) || "2".equals(sex))) {
                    sex = "3";
                }

                if("1".equals(isDisable)) {
                    isDisable = "1";
                } else {
                    isDisable = "null";
                }

                userId = DB.e(userId);
                String sql = "" +
                        "update sys_user " +
                        "set name = '"+DB.e(name)+"',account = '"+DB.e(account)+"',personal_signature = '"+DB.e(personalSignature)+"'," +
                        "   avatar = '"+DB.e(avatar)+"',birthday = "+birthday+",phone = '"+DB.e(phone)+"',email = '"+DB.e(email)+"'," +
                        "   real_name = '"+DB.e(realName)+"',idcard = '"+DB.e(idcard)+"',sex = "+sex+",is_disable = "+isDisable+",login_time=" + loginTime + " " +
                        "where user_id = '"+userId+"';" +
                        "delete from sys_user_role_rel where user_id = '"+userId+"';";

                for(String roleId : roleIdArr) {
                    if(!roleId.isEmpty()) sql += "insert into sys_user_role_rel(user_id,role_id) value('"+userId+"','"+DB.e(roleId)+"');";
                }

                if(DB.updateTransaction(sql) > 0) {
                    return success();
                }
                break;
            case "Add":
                if(!ApiGlobalInterceptor.permission("lspk:ls:user:add")) {
                    write(403,error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if(name.isEmpty()) {
                    return error("名称不能为空");
                }

                if(account.isEmpty()) {
                    return error("账号不能为空");
                }

                if(password.isEmpty()) {
                    return error("密码不能为空");
                }

                password = SysUser.md5Pwd(password);

                if(DateTime.strToDate(birthday,"yyyy-MM-dd") == null) {
                    birthday = "null";
                } else {
                    birthday = "'"+birthday+"'";
                }

                if(!("1".equals(sex) || "2".equals(sex))) {
                    sex = "3";
                }

                if("1".equals(isDisable)) {
                    isDisable = "1";
                } else {
                    isDisable = "null";
                }

                sql = "" +
                        "insert into sys_user(name,account,password,personal_signature,avatar,birthday,phone,email,real_name,idcard,sex,is_disable)" +
                        "value('"+DB.e(name)+"','"+DB.e(account)+"','"+DB.e(password)+"','"+DB.e(personalSignature)+"','"+DB.e(avatar)+"',"+birthday+"," +
                        "   '"+DB.e(phone)+"','"+DB.e(email)+"','"+DB.e(realName)+"','"+DB.e(idcard)+"',"+sex+","+isDisable+");" +
                        "select @new_user_id:=last_insert_id();";

                for(String roleId : roleIdArr) {
                    if(!roleId.isEmpty()) sql += "insert into sys_user_role_rel(user_id,role_id) value(@new_user_id,'"+DB.e(roleId)+"');";
                }

                if(DB.updateTransaction(sql) > 0) {
                    return success();
                }
                break;
            case "Delete":
                if(!ApiGlobalInterceptor.permission("lspk:ls:user:delete")) {
                    write(403,error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if(userId.isEmpty()) {
                    return error("用户代码不能为空");
                }

                if(DB.update("delete from sys_user where user_id = '"+DB.e(userId)+"'") > 0) {
                    return success();
                }
                break;
            case "IsDisable":
                if(!ApiGlobalInterceptor.permission("lspk:ls:user:edit:isDisable")) {
                    write(403,error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if(userId.isEmpty()) {
                    return error("用户代码不能为空");
                }

                if("1".equals(isDisable)) {
                    isDisable = "1";
                } else {
                    isDisable = "null";
                }

                if(DB.update("update sys_user set is_disable = "+isDisable+" where user_id = '"+DB.e(userId)+"'") > 0) {
                    return success();
                }
                break;
            case "ResetPwd":
                if(!ApiGlobalInterceptor.permission("lspk:ls:user:edit:resetPwd")) {
                    write(403,error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if(userId.isEmpty()) {
                    return error("用户代码不能为空");
                }

                String newPassword = param("new_password");

                if(Valid.isBlank(newPassword)) {
                    return error("新密码不能为空");
                }

                if (DB.update("" +
                        "update sys_user " +
                        "set password = '" + DB.e(SysUser.md5Pwd(newPassword)) + "', pwd_update_date = now() " +
                        "where user_id = '" + DB.e(userId) + "'") != -1) {
                    return success();
                }
                break;
            default:
                return error("修改类型有误");
        }
        return error("操作失败");
    }

    /**
     * 批量修改用户
     */
    @PostMapping("/system/api/user/batchUpdateUser")
    @LoginToken(validBackend = true)
    public ApiResponse batchUpdateUser() {
        String updateType = param("UpdateType");
        String sex = param("sex");
        String isDisable = param("is_disable");
        String userIds = param("user_id_arr");

        if(Valid.isBlank(userIds)) {
            return error("请选择用户");
        }

        String[] userIdArr = userIds.split(",");
        String userIdArrStr = "";

        for(int i = 0;i < userIdArr.length;i++) {
            try {
                if(i == userIdArr.length - 1) {
                    userIdArrStr += "'"+DB.e(String.valueOf(Integer.parseInt(userIdArr[i])))+"'";
                } else {
                    userIdArrStr += "'"+DB.e(String.valueOf(Integer.parseInt(userIdArr[i])))+"',";
                }
            } catch(Exception e) {
                return error("用户代码格式有误");
            }
        }

        switch(updateType) {
            case "IsDisable":
                if(!ApiGlobalInterceptor.permission("lspk:ls:user:edit:isDisable")) {
                    write(403,error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if("1".equals(isDisable)) {
                    isDisable = "1";
                } else {
                    isDisable = "null";
                }

                if(DB.update("update sys_user set is_disable = "+isDisable+" where user_id in ("+userIdArrStr+")") > 0) {
                    return success();
                }
                break;
//            case "Sex":
//                if(!ApiGlobalInterceptor.permission("lspk:ls:user:edit:sex")) {
//                    write(403,error("接口执行失败，该用户没有权限"));
//                    return null;
//                }
//
//                if(!("1".equals(sex) || "2".equals(sex))) {
//                    sex = "null";
//                }
//
//                if(DB.update("update sys_user set sex = "+sex+" where user_id in ("+userIdArrStr+")") > 0) {
//                    return success();
//                }
//                break;
            case "Delete":
                if(!ApiGlobalInterceptor.permission("lspk:ls:user:delete")) {
                    write(403,error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if(DB.update("delete from sys_user where user_id in ("+userIdArrStr+")") > 0) {
                    return success();
                }
                break;
        }
        return error("操作失败");
    }

    /**
     * 修改个人信息
     */
    @PostMapping("/system/api/user/updatePersonal")
    @LoginToken(validBackend = true, permissionKey = "lspk:ls:user:updatePersonal")
    public ApiResponse updatePersonal() {
        String updateType = param("UpdateType");

        switch(updateType) {
            case "BaseInfo":
                String name = param("name");
                String personalSignature = param("personal_signature");
                String birthday = param("birthday");
                String phone = param("phone");
                String email = param("email");
                String sex = param("sex");

                if (Valid.isEmpty(name) || name.length() > 50) {
                    return error("名称长度范围为1-50个字符");
                }

                if (personalSignature.length() > 250) {
                    return error("个性签名长度范围为0-250个字符");
                }

                if (!DateTime.valid(birthday, "yyyy-MM-dd")) {
                    return error("生日格式不对");
                }

                if (phone.length() > 50) {
                    return error("手机号长度范围为0-50个字符");
                }

                if (email.length() > 150) {
                    return error("邮箱长度范围为0-50个字符");
                }

                switch (sex) {
                    case "1":
                    case "2":
                        break;
                    default:
                        sex = "3";
                }

                if(DB.update("" +
                        "update sys_user " +
                        "set name = '" + DB.e(name) + "',personal_signature = '" + DB.e(personalSignature) + "',birthday = '" + DB.e(birthday) + "'," +
                        "   phone = '" + DB.e(phone) + "',email = '" + DB.e(email) + "',sex = '" + sex + "' " +
                        "where user_id = '" + SysUser.getBackendLoginId() + "'") != 0) {
                    return success();
                }
                break;
            case "Avatar":
                Part avatarFile = part("avatarFile");

                String filename = avatarFile.getSubmittedFileName();
                String suffix = IO.getSuffix(filename);

                if (avatarFile.getSize() > (1024 * 1024 * 5)) {
                    return error("图片大小不能超过5MB");
                }

                switch (suffix.toLowerCase()) {
                    case "jpg":
                    case "jpeg":
                    case "png":
                        break;
                    default:
                        error("只允许上传PNG和JPG格式的图片");
                }

                String filepath = "upload/system/"+DateTime.now("yyyyMMdd")+"/" +
                        IO.getSuffix(filename)+DateTime.now("-HH_mm_ss_SSS-")+ Rdm.num(8)+"."+IO.getSuffix(filename);
                String path = System.getProperty("user.dir")+environment.getProperty("leaf.resource","/resources/") + filepath;

                try {
                    IO.createFile(new File(path));
                    avatarFile.write(path);
                } catch (IOException e) {
                    Log.write("Error_system",Log.getException(e));
                    return error("上传失败");
                }

                return success("/" + filepath);
            case "Password":
                String password = param("password");
                String newPassword = param("new_password");
                password = SysUser.md5Pwd(password);

                if (Valid.isEmpty(password)) {
                    return error("密码不能为空");
                }

                if (newPassword.length() > 50) {
                    return error("新密码范围是1-50个字符");
                }

                if (!password.equals(
                        DB.queryFirstField("select password from sys_user where user_id = '" + SysUser.getBackendLoginId() + "'")
                )) {
                    return error("密码错误");
                }

                if (DB.update("" +
                        "update sys_user " +
                        "set password = '" + DB.e(SysUser.md5Pwd(newPassword)) + "', pwd_update_date = now() " +
                        "where user_id = '" + SysUser.getBackendLoginId() + "'") != -1) {
                    return success();
                }
                break;
        }
        return error("操作失败");
    }
}
