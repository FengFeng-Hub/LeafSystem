package leaf.test;

import leaf.common.object.JSONMap;
import leaf.common.util.DateTime;

public class SysTimedTaskTest {
    public static JSONMap test01(String s, Boolean b, Long l, Double d, Integer i) throws InterruptedException {
        String result = DateTime.now("yyyy-MM-dd HH:mm:ss") + "执行成功：leaf.test.SysTimedTaskTest.test01" + "(" + s + "," + b + "," + l + "," + d + "," + i + ")";
        System.out.println(result);
        Thread.sleep(1000);
        return JSONMap.success(result);
    }

    public static JSONMap test02(String s, Boolean b, Long l, Double d, Integer i) {
        String result = DateTime.now("yyyy-MM-dd HH:mm:ss") + "执行成功：leaf.test.SysTimedTaskTest.test02" + "(" + s + "," + b + "," + l + "," + d + "," + i + ")";
        System.out.println(result);
        return JSONMap.success(result);
    }

}
