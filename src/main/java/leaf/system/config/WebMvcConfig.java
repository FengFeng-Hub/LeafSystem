package leaf.system.config;

import leaf.common.Log;
import leaf.system.interceptor.PageGlobalInterceptor;
import leaf.system.interceptor.ApiGlobalInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * application配置文件参数
     */
    @Autowired
    private Environment environment;

    /**
     * 拦截器配置
     * @param registry InterceptorRegistry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //接口全局拦截器
        registry.addInterceptor(new ApiGlobalInterceptor())
                .addPathPatterns("/api/**")//接口
                .addPathPatterns("/system/api/**");//系统接口
        //页面全局拦截器
        registry.addInterceptor(new PageGlobalInterceptor())
                .addPathPatterns("/backend/index**")
                .addPathPatterns("/backend/pages/**")//拦截后台页面
                .addPathPatterns("/log/**");//拦截日志页面检查登录
    }

    /**
     * Cross-Origin Resource Sharing跨源资源共享配置
     * @param registry registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if("true".equals(environment.getProperty("leaf.global.cors"))) {
            registry.addMapping("/**")
                    .allowCredentials(true)//是否发送Cookie
                    .allowedOrigins("*")//放行哪些原始域
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*");
            System.out.println(Log.info("系统开启全局跨域"));
        } else {
            System.out.println(Log.info("系统未开启全局跨域"));
        }
    }
}
