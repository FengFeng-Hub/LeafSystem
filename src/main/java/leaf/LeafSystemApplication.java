package leaf;

import leaf.common.Config;
import leaf.common.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableRedis  // 启动Redis
//@EnableMail
//@EnableCaching//开启缓存
public class LeafSystemApplication {
    //后台：http://127.0.0.1:8081/backend/index
    public static void main(String[] args) {
        SpringApplication.run(LeafSystemApplication.class);
        System.out.println(String.format("\033[36;1m%s\033[0m",Config.author()));
        System.out.println(Log.success("Leaf系统启动成功：http://localhost:8081/backend/index"));
    }
}
