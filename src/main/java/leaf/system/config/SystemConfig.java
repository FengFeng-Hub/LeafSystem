package leaf.system.config;

import jakarta.annotation.PostConstruct;
import leaf.common.DB;
import leaf.common.Log;
import leaf.common.net.Mail;
import leaf.system.annotate.EnableMail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

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

    @Autowired
    private DataSource dataSource;

    // ✅ 安全地推断主类
    public final static Class<?> MainClass = mainApplicationClass();

    /**
     * 程序启动后最先执行的方法
     */
    @PostConstruct
    public void init() {
        if (MainClass != null) {
            System.out.println(Log.info("主类检测：" + MainClass.getName()));
        } else {
            System.out.println(Log.error("未检测到主类，可能在特殊运行环境下"));
        }

        // 配置日志路径
        Log.logPath = System.getProperty("user.dir") + environment.getProperty("leaf.resource");

        System.out.println("driver:"+environment.getProperty("spring.datasource.driver-class-name"));

//            DB.druidConfig(
//                    environment.getProperty("spring.datasource.driver-class-name"),
//                    environment.getProperty("spring.datasource.url"),
//                    environment.getProperty("spring.datasource.username"),
//                    environment.getProperty("spring.datasource.password"),
//                    environment.getProperty("spring.datasource.druid.initialSize"),
//                    environment.getProperty("spring.datasource.druid.maxActive"),
//                    environment.getProperty("spring.datasource.druid.minIdle")
//            );
        DB.druidConfig(dataSource);
        System.out.println(Log.info("JDBC配置"));

        // 检查启动类是否有EnableMail注解
        if (MainClass.isAnnotationPresent(EnableMail.class)) {
            Mail.config(
                    environment.getProperty("spring.mail.host"),
                    environment.getProperty("spring.mail.port"),
                    environment.getProperty("spring.mail.username"),
                    environment.getProperty("spring.mail.password")
            );
            System.out.println(Log.info("Mail配置"));
        }
    }

    private static Class<?> mainApplicationClass() {
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if ("main".equals(element.getMethodName())) {
                    return Class.forName(element.getClassName());
                }
            }
        } catch (ClassNotFoundException ex) {
            // ignore
        }
        return null;
    }
}
