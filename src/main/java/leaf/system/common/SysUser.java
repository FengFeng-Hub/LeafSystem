package leaf.system.common;

import jakarta.servlet.http.HttpServletResponse;
import leaf.common.DB;
import leaf.common.net.Servlet;
import leaf.common.object.JSONList;
import leaf.common.object.JSONMap;
import leaf.common.util.DateTime;
import leaf.common.util.Lock;
import leaf.common.util.Valid;
import leaf.system.pojo.SysCurrentUser;

/**
 * 系统用户
 */
public class SysUser {
    public final static String LS_PASSWORD_SALT = "LSPWDSALT";
    public static String md5Pwd(String password) {
        return Lock.md5(LS_PASSWORD_SALT + "::" + password);
    }

    /**
     * 当前登录用户
     */
    private final static ThreadLocal<SysCurrentUser> CURRENT_USER = new ThreadLocal<>();

    /**
     * 设置当前用户
     * @param currentUser 当前用户
     */
    public static void setCurrent(SysCurrentUser currentUser) {
        CURRENT_USER.set(currentUser);
    }

    /**
     * 获取当前用户
     * @return 当前用户
     */
    public static SysCurrentUser getCurrent() {
        return CURRENT_USER.get();
    }

    /**
     * 移除当前用户，防止内存泄漏
     */
    public static void removeCurrentUser() {
        CURRENT_USER.remove();
    }

    /**
     * 设置前台登录
     * @param userId 用户代码
     * @param duration 过期时间的时间戳（秒级）
     *                 -1表示浏览器关闭后失效
     */
    public static void setFrontendLoginId(String userId,int duration) {
        HttpServletResponse response = Http.response();
        //lyfsys_frontend_用户代码_当前时间时间戳（毫秒级）_过期时间的时间戳（毫秒级）
        long currentTime = System.currentTimeMillis();

        if(duration == -1) {
            Servlet.setCookie(response,"UserToken",
                    Lock.aesEncrypt("ls-frontend_"+userId+"_"+currentTime+"_"+
                            (currentTime + (86400 * 1000))),duration);
        } else {
            Servlet.setCookie(response,"UserToken",
                    Lock.aesEncrypt("ls-frontend_"+userId+"_"+currentTime+"_"+
                            (currentTime + (duration * 1000L))),duration);
        }

        SysCurrentUser sysCurrentUser = CURRENT_USER.get();

        if (sysCurrentUser != null) {
            sysCurrentUser.setFrontendUserId(userId);
        } else {
            sysCurrentUser = new SysCurrentUser(userId, null);
            CURRENT_USER.set(sysCurrentUser);
        }
    }
    /**
     * 获取前台登录
     * @return 登录用户代码
     */
    public static String getFrontendLoginId() {
        return getCurrent().getBackendUserId();
    }
    /**
     * 移除前台登录
     */
    public static void removeFrontendLoginId() {
        Servlet.clearCookie(Http.response(),"UserToken");
    }
    /**
     * 设置后台登录
     * @param userId 用户代码
     * @param duration 过期时间的时间戳（秒级）
     *                 -1表示浏览器关闭后失效
     */
    public static void setBackendLoginId(String userId,int duration) {
        HttpServletResponse response = Http.response();
        //lyfsys_backend_用户代码_当前时间时间戳（毫秒级）_过期时间的时间戳（毫秒级）
        long currentTime = System.currentTimeMillis();

        if(duration == -1) {
            Servlet.setCookie(response,"AdminToken",
                    Lock.aesEncrypt("ls-backend_"+userId+"_"+currentTime+
                            "_"+(currentTime + (86400 * 1000))),duration);
        } else {
            Servlet.setCookie(response,"AdminToken",
                    Lock.aesEncrypt("ls-backend_"+userId+"_"+currentTime+
                            "_"+(currentTime + (duration * 1000L))),duration);
        }

        SysCurrentUser sysCurrentUser = CURRENT_USER.get();

        if (sysCurrentUser != null) {
            sysCurrentUser.setBackendUserId(userId);
        } else {
            sysCurrentUser = new SysCurrentUser(null, userId);
            CURRENT_USER.set(sysCurrentUser);
        }

    }
    /**
     * 获取后台登录
     * @return 登录用户代码
     */
    public static String getBackendLoginId() {
        return getCurrent().getBackendUserId();
    }
    /**
     * 移除后台登录
     */
    public static void removeBackendLoginId() {
        Servlet.clearCookie(Http.response(),"AdminToken");
    }
    /**
     * 通过token获取用户代码，未登录则返回空
     * @param isBackend 是否是后台用户
     * @return 用户代码
     */
    public static String getUserByToken(boolean isBackend) {
        // 获取后台用户Token
        String token = null;

        if (isBackend) {
            token = Servlet.getCookie(Http.request(), "AdminToken");
        } else {
            token = Servlet.getCookie(Http.request(), "UserToken");
        }

        if(token != null) {
            token = Lock.aesDncrypt(token);

            if(token != null) {
                String[] userSplit = token.split("_");

                if(userSplit.length == 4 &&
                        (isBackend?"ls-backend":"ls-frontend").equals(userSplit[0])) {
                    // 登录时间时间戳
                    long loginTime = Long.parseLong(userSplit[2]);
                    // 过期时间时间戳
                    long expireTime = Long.parseLong(userSplit[3]);
                    // 登录用户代码
                    String loginUserId = userSplit[1];
                    // 查看用户是否禁用和密码最后更新时间
                    JSONMap user = DB.queryFirst("select is_disable,pwd_update_date from sys_user where user_id = '" + loginUserId + "'");

                    if (user == null) {
                        return null;
                    }

                    String pwdUpdateDateStr = user.getString("pwd_update_date");
                    long pwdUpdateDate = Valid.isEmpty(pwdUpdateDateStr)?-1L:DateTime.dataToTimeStamp(DateTime.strToDate(pwdUpdateDateStr, "yyyy-MM-dd HH:mm:ss"));
                    // 过期时间小于当前时间 && 没有禁用 && 密码最后更新时间大于登录时间
                    if (System.currentTimeMillis() <= expireTime && !"1".equals(user.getString("is_disable")) && pwdUpdateDate < loginTime) {
                        return loginUserId;
                    }
                }
            }
        }

        return null;
    }
    /**
     * 获取用户角色代码字符串
     */
    public static String getUserRoleIdStr() {
        String userRoleIdStr = "",frontendLoginId = SysUser.getFrontendLoginId(),backendLoginId = SysUser.getBackendLoginId();
        JSONList userRoleList = DB.query("" +
                "select a.role_id from sys_user_role_rel a " +
                "left join sys_role b on a.role_id = b.role_id " +
                "where a.user_id = '" + backendLoginId + "' or a.user_id = '" + frontendLoginId + "' and b.is_disable != 1 "+
                "order by role_id");

        if(userRoleList == null) return "";

        for(int i = 0;i < userRoleList.size();i++) {
            if(i == userRoleList.size() - 1) {
                userRoleIdStr += userRoleList.getMap(i).getString("role_id");
            } else {
                userRoleIdStr += userRoleList.getMap(i).getString("role_id")+",";
            }
        }
        return userRoleIdStr;
    }
}
