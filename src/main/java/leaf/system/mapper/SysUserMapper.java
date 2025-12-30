package leaf.system.mapper;

import leaf.common.object.JSONMap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserMapper {
    /**
     * 获取用户列表
     *
     * @param params 参数
     * @return 用户列表
     */
    List<Map<String, Object>> getUserList(JSONMap params);

    /**
     * 获取用户数量
     *
     * @param params 参数
     * @return 用户数量
     */
    String getUserCount(JSONMap params);

    /**
     * 获取用户信息By账号和密码
     *
     * @param account 账号
     * @param password 密码
     * @return 用户信息
     */
    @Select("select a.user_id, a.is_disable, " +
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
            "where  a.account = #{account} and a.password = #{password} " +
            "group by a.user_id")
    JSONMap getUserByAccountAndPassword(String account, String password);

    /**
     * 获取用户信息By用户ID
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Select("select a.name,a.account,a.personal_signature,a.avatar,a.birthday,a.phone,a.email,a.sex,a.is_disable,c.role_id,c.role_name " +
            "from sys_user a " +
            "   left join sys_user_role_rel b on a.user_id = b.user_id " +
            "   left join sys_role c on b.role_id = c.role_id " +
            "where a.user_id = #{user_id}")
    JSONMap getUserAndRoleByUserId(@Param("user_id") String userId);

    /**
     * 更新用户登录信息
     *
     * @param userId 用户ID
     * @param loginIp 登录IP
     * @return 更新结果
     */
    @Update("update sys_user " +
            "set login_ip = #{login_ip},login_time = now() " +
            "where user_id = #{user_id}")
    int updateLoginIpAndLoginTimeByUserId(@Param("user_id") String userId, @Param("login_ip") String loginIp);
}
