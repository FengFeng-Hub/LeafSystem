package leaf;

import leaf.system.common.SysUser;

import java.util.ArrayList;
import java.util.List;

public class Test {
    @org.junit.jupiter.api.Test
    public void test01() {
        List<Object> list = new ArrayList<>();
        list.add("11");
        list.add("22");
        list.add("33");
        list.forEach(name -> {
            name = name + ",";
            System.out.println(name);
        });
    }

    @org.junit.jupiter.api.Test
    public void test02() {
        System.out.println("123(12".split("\\(")[0]);
    }

    @org.junit.jupiter.api.Test
    public void md5Pwd() {
        System.out.println(SysUser.md5Pwd("123456"));
        System.out.println(SysUser.md5Pwd("admin"));
    }

    @org.junit.jupiter.api.Test
    public void threadLocalTest() {

        SysUser.setFrontendLoginId("1", -1);
    }
}
