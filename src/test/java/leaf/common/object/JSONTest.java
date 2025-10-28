package leaf.common.object;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JSONTest {
    @Test
    public void test01() {
        //毫秒ms：
        long startTime=System.currentTimeMillis(); //获取开始时间

        JSONMap stus = new JSONMap();
        JSONMap zhangsan = new JSONMap();
        JSONList zhangsanHobbies = new JSONList();
        JSONList zhangsanHobbiesSub = new JSONList();
        JSONMap lisi = new JSONMap();
        JSONMap wangwu = new JSONMap();
        stus.put("zhangsan",zhangsan);
        stus.put("lisi",lisi);
        stus.put("wangwu",wangwu);
        zhangsan.put("age",18);
        zhangsan.put("hobbies",zhangsanHobbies);
        zhangsan.put("class","2110\"");
        zhangsan.put("grade",'A');
        zhangsan.put("score",88.5);
        zhangsanHobbies.add("sing");
        zhangsanHobbies.add("dance");
        zhangsanHobbies.add("basketball");
        zhangsanHobbies.add(wangwu);
        zhangsanHobbies.add(zhangsanHobbiesSub);
        zhangsanHobbiesSub.add("篮球");
        zhangsanHobbiesSub.add("游泳");
        lisi.put("age",20);
        lisi.put("class","2110");
        wangwu.put("age",19);
        wangwu.put("class","2110");
        zhangsanHobbies.add(1,"football");
        System.out.println(stus);
        System.out.println(stus.toMap());
        System.out.println(stus.getMap("zhangsan").getList("hobbies").get(2));
        System.out.println(stus.getMap("zhangsan").getList("hobbies").getMap(4).get("age"));
        System.out.println(stus.getMap("zhangsan").getList("hobbies").getList(5).get(1));

        long endTime=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
    }

    @Test
    public void test02() {
        //毫秒ms：
        long startTime=System.currentTimeMillis(); //获取开始时间

        Map stus = new LinkedHashMap();
        Map zhangsan = new LinkedHashMap();
        List zhangsanHobbies = new ArrayList();
        List zhangsanHobbiesSub = new ArrayList();
        Map lisi = new LinkedHashMap();
        Map wangwu = new LinkedHashMap();
        stus.put("zhangsan",zhangsan);
        stus.put("lisi\"",lisi);
        stus.put("wangwu",wangwu);
        zhangsan.put("age", 18);
        zhangsan.put("hobbies",zhangsanHobbies);
        zhangsan.put("class","2110");
        zhangsan.put("grade",'A');
        zhangsan.put("score",88.5);
        zhangsanHobbies.add("sing");
        zhangsanHobbies.add("dance");
        zhangsanHobbies.add("basketball");
        zhangsanHobbies.add(wangwu);
        zhangsanHobbies.add(zhangsanHobbiesSub);
        zhangsanHobbiesSub.add("篮球");
        zhangsanHobbiesSub.add("游泳");
        lisi.put("age",20);
        lisi.put("class","2110");
        wangwu.put("age",19);
        wangwu.put("class","2110");
        zhangsanHobbies.add(1,"football");
        System.out.println(stus);

        System.out.println(JSON.toJson(stus));

        System.out.println(JSONMap.toJSONMap(stus));
        long endTime=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
    }

    @Test
    public void test03() {
        JSONMap result = new JSONMap();

        JSONList list = new JSONList();
        JSONMap item1 = new JSONMap();
        JSONMap item2 = new JSONMap();
        JSONMap item3 = new JSONMap();
        JSONMap item4 = new JSONMap();
        JSONMap item5 = new JSONMap();
        JSONMap item6 = new JSONMap();
        JSONMap item7 = new JSONMap();
        JSONMap item8 = new JSONMap();
        JSONMap item9 = new JSONMap();
        JSONMap item10 = new JSONMap();

        item1.put("id", "1");
        item1.put("parent_id", "");
        item1.put("name", "A");

        item2.put("id", "2");
        item2.put("parent_id", "1");
        item2.put("name", "A-A");

        item3.put("id", "3");
        item3.put("parent_id", "1");
        item3.put("name", "A-B");

        item4.put("id", "4");
        item4.put("parent_id", "2");
        item4.put("name", "A-A-A");

        item5.put("id", "5");
        item5.put("parent_id", "5");
        item5.put("name", "B");

        item6.put("id", "6");
        item6.put("parent_id", "3");
        item6.put("name", "A-B-A");

        item7.put("id", "7");
        item7.put("parent_id", "3");
        item7.put("name", "A-B-B");

        item8.put("id", "8");
        item8.put("parent_id", "3");
        item8.put("name", "A-B-C");

        item9.put("id", "9");
        item9.put("parent_id", "3");
        item9.put("name", "A-B-D");

        item10.put("id", "10");
        item10.put("parent_id", "1");
        item10.put("name", "A-C");

        list.add(item4);
        list.add(item1);
        list.add(item7);
        list.add(item2);
        list.add(item8);
        list.add(item3);
        list.add(item5);
        list.add(item9);
        list.add(item6);
        list.add(item10);
        list.add("tree");

        result.put("result",list);
        System.out.println(JSON.formatJSON(result.toString()));

        list = list.listToTree("id","parent_id","staff", true,true,false);
//        list = list.listToTree("id","parent_id","staff");

        result.put("result",list);
        System.out.println(JSON.formatJSON(result.toString()));
    }
}
