package leaf.common;

import org.junit.jupiter.api.Test;

public class ClazzTest {
    @Test
    public void test01() {
//        leaf.test.SysTimedTaskTest.test01("leaf.test.SysTimedTaskTest.test01", true, 20000L, 316.50D, 100);
        Object o = Clazz.invokeStaticByMethodPath("leaf.test.SysTimedTaskTest.test01", "测试任务1", true, 20000L, 316.50D, 100);
        System.out.println(o);
    }
}
