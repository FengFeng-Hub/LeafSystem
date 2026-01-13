package leaf.system;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerationTest {
    @Test
    public void test() {
        // 获取resources文件
        String resources = "/ls/codetemplate/backend/Api.java.vm";
//        LSTemplateEngine.render(IO.readResourcesFile(resources), context);

        String template = """
package {$ packageName $};

public class {$ className $} {
{$ if hasSerializable $}
    private static final long serialVersionUID = 1L;
{$ endif $}
{$ for field in fields $}
    private {$ field.type $} {$ field.name $};
{$ endfor $}
}

{$ packageName $}
{$ packageName $} text
{$ if !hasSerializable $}123{$ endif $}
{$ if packageName=='com.demo' $}11111{$ endif $}
{$ if packageName == 'com.demo2' $}222222{$ endif $}
---
{$ if aa == '123' $}3333333{$ endif $}
{$ if bb == null $}444444{$ endif $}
""";

        Map<String, Object> field1 = Map.of("type", "String", "name", "name");
        Map<String, Object> field2 = Map.of("type", "Integer", "name", "age");

        Map<String, Object> context = new HashMap<>();
        context.put("packageName", "com.demo");
        context.put("className", "User");
        context.put("hasSerializable", false);
        context.put("aa", "123");
        context.put("bb", null);
        context.put("fields", List.of(field1, field2));

//        String result = LSTemplateEngine.render(template, context);
//        System.out.println(result);
    }
}
