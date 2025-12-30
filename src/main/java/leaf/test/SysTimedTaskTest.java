package leaf.test;

import leaf.common.object.JSONMap;
import leaf.common.util.DateTime;
import leaf.system.model.ApiResponse;

public class SysTimedTaskTest {
    public static ApiResponse test01(String s, Boolean b, Long l, Double d, Integer i) throws InterruptedException {
        String result = DateTime.now("yyyy-MM-dd HH:mm:ss") + "执行成功：leaf.test.SysTimedTaskTest.test01" + "(" + s + "," + b + "," + l + "," + d + "," + i + ")";
        System.out.println(result);
        Thread.sleep(1000);
        return ApiResponse.success(result);
    }

    public static ApiResponse test02(String s, Boolean b, Long l, Double d, Integer i) {
        String result = DateTime.now("yyyy-MM-dd HH:mm:ss") + "执行成功：leaf.test.SysTimedTaskTest.test02" + "(" + s + "," + b + "," + l + "," + d + "," + i + ")";
        System.out.println(result);
        return ApiResponse.success(result);
    }

}
