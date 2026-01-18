package leaf;

import leaf.system.model.SysUser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
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
//        System.out.println(SqlFormatter.format("" +
//                "insert into user(id,name,age) values(1,'a',18);SELECT p.name,p.level_type\n" +
//                "FROM config_wbs_example p" +
//                "   left join test b on p.id = b.id\n" +
//                "WHERE FIND_IN_SET(\n" +
//                "    p.buss_id,\n" +
//                "    REPLACE(\n" +
//                "        (SELECT parent_id_tree FROM config_wbs_example WHERE buss_id = '1993606289868652544'),\n" +
//                "        '/',\n" +
//                "        ','\n" +
//                "    )\n" +
//                ");" +
//                "create table user(id bigint primary key,name varchar(50),age int);" +
//                ""));
        LocalDate localDate = LocalDate.now();
        System.out.println(localDate);
        System.out.println(1);
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
