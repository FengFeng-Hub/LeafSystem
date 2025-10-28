package leaf.common.net;

import leaf.common.util.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT)
public class MailTest {
    @Autowired
    private Environment environment;

    @Test
    public void test() {
        System.out.println(1);
//        Mail.config(
//                environment.getProperty("spring.mail.host"),
//                environment.getProperty("spring.mail.port"),
//                environment.getProperty("spring.mail.username"),
//                environment.getProperty("spring.mail.password")
//        );
//
//        System.out.println(Mail.sendEmail("3186786629@qq.com","Java邮件测试","Time:"+ DateTime.now() +" 测试成功！"));
    }
}
