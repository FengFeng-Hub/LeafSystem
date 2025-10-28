package leaf.system.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启动Redis
 */
@Target(ElementType.TYPE) // 只能用于类上
@Retention(RetentionPolicy.RUNTIME) // 运行时保留
public @interface EnableRedis {
}
