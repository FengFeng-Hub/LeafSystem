package leaf.system.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginToken {
//    /**
//     * 可以调用该接口的用户组
//     * @return ints
//     */
//    int[] userGroupIds() default {};
    /**
     * 权限键
     * @return permissionKey
     */
    String permissionKey() default "";
    /**
     * 是否验证后台登录
     * @return boolean
     */
    boolean validBackend() default false;
    /**
     * 是否验证前台登录
     * @return boolean
     */
    boolean validFrontend() default false;
}
