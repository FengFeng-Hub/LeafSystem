package leaf.system.config;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.util.Properties;

/**
 * 系统bean
 */
@Configuration
public class SystemBean {
    /**
     * 扫描有 @ServerEndpoint 注解的 Bean
     * @return ServerEndpointExporter
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * Scheduler
     * @return Scheduler
     * @throws SchedulerException
     */
    @Bean
    public Scheduler scheduler() throws SchedulerException {
        StdSchedulerFactory factory = new StdSchedulerFactory();

        // 简单配置
        Properties props = new Properties();
        props.put("org.quartz.scheduler.instanceName", "DefaultQuartzScheduler");
        props.put("org.quartz.threadPool.threadCount", "5");
        props.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");

        factory.initialize(props);
        return factory.getScheduler();
    }
}
