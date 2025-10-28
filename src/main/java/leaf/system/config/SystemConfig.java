package leaf.system.config;

import jakarta.annotation.PostConstruct;
import leaf.common.DB;
import leaf.common.Log;
import leaf.common.net.Mail;
import leaf.common.object.Redis;
import leaf.system.annotate.EnableMail;
import leaf.system.annotate.EnableRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 系统配置
 */
@Configuration
public class SystemConfig {
    /**
     * application配置文件参数
     */
    @Autowired
    private Environment environment;

    /**
     * 程序启动后最先执行的方法
     */
    @PostConstruct
    public void init() {
        // 获取启动类
        String mainApplicationClassName = System.getProperty("sun.java.command");
        Class<?> mainClass;

        try {
            mainClass = Class.forName(mainApplicationClassName.split(" ")[0]);

            // 配置日志路径
            Log.logPath = System.getProperty("user.dir") + environment.getProperty("leaf.resource");

            DB.druidConfig(
                    environment.getProperty("spring.datasource.driver-class-name"),
                    environment.getProperty("spring.datasource.url"),
                    environment.getProperty("spring.datasource.username"),
                    environment.getProperty("spring.datasource.password"),
                    environment.getProperty("spring.datasource.druid.initialSize"),
                    environment.getProperty("spring.datasource.druid.maxActive"),
                    environment.getProperty("spring.datasource.druid.minIdle")
            );
            System.out.println(Log.info("JDBC配置"));

            // 检查启动类是否有EnableRedis注解
            if (mainClass.isAnnotationPresent(EnableRedis.class)) {
                Redis.config(
                        environment.getProperty("spring.redis.host"),
                        environment.getProperty("spring.redis.port"),
                        environment.getProperty("spring.redis.password"),
                        environment.getProperty("spring.redis.database"),
                        environment.getProperty("spring.redis.jedis.pool.max-active"),
                        environment.getProperty("spring.redis.jedis.pool.max-idle"),
                        environment.getProperty("spring.redis.jedis.pool.min-idle"),
                        environment.getProperty("spring.redis.jedis.pool.max-wait")
                );
                System.out.println(Log.info("Redis配置"));
            }

            // 检查启动类是否有EnableMail注解
            if (mainClass.isAnnotationPresent(EnableMail.class)) {
                Mail.config(
                        environment.getProperty("spring.mail.host"),
                        environment.getProperty("spring.mail.port"),
                        environment.getProperty("spring.mail.username"),
                        environment.getProperty("spring.mail.password")
                );
                System.out.println(Log.info("Mail配置"));
            }
        } catch (ClassNotFoundException e) {
            Log.write("Error", "获取启动类失败");
        }
    }
}
