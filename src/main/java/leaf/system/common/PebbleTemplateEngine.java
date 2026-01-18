package leaf.system.common;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.cache.template.ConcurrentMapTemplateCache;
import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.lexer.Syntax;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import leaf.common.Log;
import leaf.common.constant.KeyWordArray;
import leaf.common.util.StrUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PebbleTemplateEngine {
    /**
     * 生成模板
     *
     * @param template 模板位置
     * @param context 上下文
     * @return 生成后的模板字符串
     */
    public static String generate(String template, Map<String, Object> context) {
        // 创建Pebble引擎（带缓存和优化）
        PebbleEngine engine = new PebbleEngine.Builder()
                .cacheActive(true)  // 模板编译缓存
                .templateCache(new ConcurrentMapTemplateCache())  // 并发缓存
                .autoEscaping(false)  // 代码生成不需要HTML转义
                .newLineTrimming(true) // 不保留换行
                .strictVariables(true) // 严格模式，变量未定义时报错
                // 自定义所有分隔符
                .syntax(new Syntax.Builder()
                        .setPrintOpenDelimiter("{$")   // 输出开始
                        .setPrintCloseDelimiter("$}")  // 输出结束
//                        .setExecuteOpenDelimiter("[%") // 执行开始
//                        .setExecuteCloseDelimiter("%]") // 执行结束
//                        .setCommentOpenDelimiter("[#") // 注释开始
//                        .setCommentCloseDelimiter("#]") // 注释结束
                        .build())
                // 注册自定义函数
                .extension(new AbstractExtension() {
                    @Override
                    public Map<String, Function> getFunctions() {
                        Map<String, Function> functions = new HashMap<>();
                        // 处理Java关键字
                        functions.put("javaKeyWord", new Function() {
                            // 形参
                            @Override
                            public List<String> getArgumentNames() {
                                return List.of("str");
                            }
                            // 执行方法
                            @Override
                            public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
                                return javaKeyword(args.get("str").toString());
                            }
                        });
                        // 处理MySQL关键字
                        functions.put("mysqlKeyWord", new Function() {
                            // 形参
                            @Override
                            public List<String> getArgumentNames() {
                                return List.of("str");
                            }
                            // 执行方法
                            @Override
                            public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
                                return mysqlKeyword(args.get("str").toString());
                            }
                        });
                        return functions;
                    }
                })
                .build();

        // 编译模板
        PebbleTemplate entityTemplate = engine.getTemplate(template);

        // 渲染实体类
        Writer entityWriter = new StringWriter();
        try {
            entityTemplate.evaluate(entityWriter, context);
        } catch (IOException e) {
            Log.write("Error", Log.getException(e));
            return  "";
        }

        return entityWriter.toString();
    }

    /**
     * 处理Java关键字（如果是Java关键字，变量前加下划线）
     */
    private static String javaKeyword(String fieldName) {
        return Arrays.asList(KeyWordArray.JAVA).contains(fieldName) ? "_" + fieldName : fieldName;
    }
    /**
     * 处理MySQL关键字（如果是MySQL关键字，加反引号）
     */
    private static String mysqlKeyword(String fieldName) {
        return Arrays.asList(KeyWordArray.MYSQL).contains(fieldName.toUpperCase()) ? "`" + fieldName + "`" : fieldName;
    }
}
