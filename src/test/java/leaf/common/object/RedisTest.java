package leaf.common.object;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

// @SpringBootTest会加载完整的应用程序上下文，测试速度较慢
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RedisTest {
    @Autowired
    private Environment environment;

    @Test
    public void fun() {
        System.out.println(environment.getProperty("spring.redis.host", "123"));
    }

    @Test
    public void test() {
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
        Redis redis = new Redis(1);
        System.out.println(redis.clearAll());
        System.out.println(redis.set("name","zhangsan"));
        System.out.println(redis.get("name"));
        redis.close();
    }
}
