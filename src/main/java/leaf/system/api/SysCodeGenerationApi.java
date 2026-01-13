package leaf.system.api;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import leaf.common.DB;
import leaf.common.constant.KeyWordArray;
import leaf.common.object.JSONList;
import leaf.common.object.JSONMap;
import leaf.common.util.StrUtil;
import leaf.system.annotate.LoginToken;
import leaf.system.common.Http;
import leaf.system.common.PebbleTemplateEngine;
import leaf.system.config.SystemConfig;
import leaf.system.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 代码生成模块
 */
@RestController
public class SysCodeGenerationApi extends Http {

    /**
     * 获取数据库表格信息
     */
    @GetMapping("/system/api/codeGeneration/getDBTableInfo")
    @LoginToken(validBackend = true,permissionKey = "lspk:ls:codeGeneration:DBTableInfo")
    public ApiResponse getDBTableInfo() {
        String tableName = param("table_name");
        String tableComment = param("table_comment");
        String sortField = param("SortField");
        String sortOrder = param("SortOrder");
        tableName = DB.e(tableName);
        tableComment = DB.e(tableComment);
        String sql = "" +
                "select table_name 'table_name',table_comment 'table_comment',table_schema 'table_schema'," +
                "   create_time 'create_time',update_time 'update_time',table_rows 'table_rows',auto_increment 'auto_increment' " +
                "from information_schema.tables " +
                "where table_schema = (select database()) ";

        if("1".equals(param("IsEqual","0"))) {
            if(!tableName.isEmpty()) {
                sql += "and table_name = '" + tableName + "'";
            }

            if(!tableComment.isEmpty()) {
                sql += "and table_comment = '" + tableComment + "'";
            }
        } else {
            if(!tableName.isEmpty()) {
                sql += "and table_name like '%" + tableName + "%'";
            }

            if(!tableComment.isEmpty()) {
                sql += "and table_comment like '%" + tableComment + "%'";
            }
        }

        //排序字段
        switch(sortField) {
            case "table_name":
            case "create_time":
            case "update_time":
            case "table_rows":
            case "auto_increment":
                if(sortOrder.equals("asc")) sql += " order by " + sortField + " asc ";
                else if(sortOrder.equals("desc")) sql += " order by " + sortField+" desc ";
                break;
        }
        return DB.sqlToJSONMap(sql,param("PageNo"),param("PageCount"),"100");
    }
    /**
     * 获取数据库表格字段信息
     */
    @GetMapping("/system/api/codeGeneration/getDBTableFieldInfo")
    @LoginToken(validBackend = true,permissionKey = "lspk:ls:codeGeneration:DBTableFieldInfo")
    public ApiResponse getDBTableFieldInfo() {
        return success(DB.query("" +
                "select column_name 'column_name',column_comment 'column_comment',column_type 'column_type'," +
                "   is_nullable 'is_nullable',column_default 'column_default',column_key 'column_key' " +
                "from information_schema.columns " +
                "where table_schema = (select database()) and table_name = '" + DB.e(param("table_name")) + "' " +
                "order by ordinal_position"))
                // 获取主类包路径
                .put("package", SystemConfig.MainClass.getPackage().getName());
    }
    /**
     * 生成代码
     */
    @PostMapping("/system/api/codeGeneration/generationCode")
    @LoginToken(validBackend = true,permissionKey = "lspk:ls:codeGeneration:generationCode")
    public ApiResponse generationCode(@RequestBody JSONMap requestJSON) {
        // 参数验证
        String tableName = requestJSON.getString("table_name");
        String tableDesc = requestJSON.getString("table_desc");
        String moduleName = requestJSON.getString("module_name");
        String apiRootAddress = requestJSON.getString("api_root_address");
        String primaryKeyColumnName = requestJSON.getString("primary_key_column_name");
        String packageName = requestJSON.getString("package");
        List<Map<String, Object>> columnInfoList = (List<Map<String, Object>>) requestJSON.get("column_info_list");

        if (StringUtils.isEmpty(tableName)) {
            return error("表名不能为空");
        }
        if (StringUtils.isEmpty(tableDesc)) {
            return error("表描述不能为空");
        }
        if (StringUtils.isEmpty(moduleName)) {
            return error("模块名不能为空");
        }
        if (StringUtils.isEmpty(apiRootAddress)) {
            return error("接口根地址不能为空");
        }
        if (StringUtils.isEmpty(primaryKeyColumnName)) {
            return error("主键字段名不能为空");
        }
        if (StringUtils.isEmpty(packageName)) {
            return error("package不能为空");
        }
        if (columnInfoList == null || columnInfoList.isEmpty()) {
            return error("至少需要一个字段信息");
        }

        // 参数处理
        tableDesc = !tableDesc.isEmpty() ? tableDesc.substring(0, tableDesc.length() - 1) : tableDesc;
        String moduleNameBigHump = moduleName.substring(0, 1).toUpperCase() + moduleName.substring(1);
        String primaryKeyColumnSmallHump = checkJavaKeyword(StrUtil.underlineToHump(primaryKeyColumnName));

        // 准备数据上下文
        Map<String, Object> context = new HashMap<>();

        // 表名
        context.put("tableName", tableName);
        // 表描述
        context.put("tableDesc", tableDesc);
        // 模块名（小驼峰）
        context.put("moduleName", moduleName);
        // 类名/模块名（大驼峰）
        context.put("className", moduleNameBigHump);
        // 接口根地址
        context.put("apiRootAddress", apiRootAddress);
        // 主键字段（下划线）
        context.put("primaryKeyColumn", primaryKeyColumnName);
        // 主键字段（小驼峰）
        context.put("primaryKeyField", primaryKeyColumnSmallHump);
        // 设置包名（可以根据需要修改）
        context.put("packageName", packageName);

        // 处理字段信息
        // 主键字段（下划线）
        String primaryKeyColumn = (String) context.get("primaryKeyColumn");

        int tableWidth = 240; // 基础宽度
        // 是否有select字段
        boolean hasSelect = false;
        // 是否有编辑字段
        boolean hasEdit = false;
        // 是否有排序
        boolean hasSort = false;

        // 字段信息列表
        JSONList columnInfos = new JSONList();
        context.put("columnInfoList", columnInfos);

        for (Map<String, Object> columnInfo : columnInfoList) {
            String columnUnderLine = (String) columnInfo.get("column_name");
            // 标题（字段描述）
            String columnTitle = (String) columnInfo.get("title");
            // 是否是主键
            boolean isPrimaryKey = primaryKeyColumn.equals(columnUnderLine);

            Map<String, Object> columnInfoResult = new HashMap<>();
            columnInfos.add(columnInfoResult);
            // 字段（下划线）
            columnInfoResult.put("columnUnderLine", columnInfo.get("column_name"));
            // 字段（小驼峰）
            columnInfoResult.put("columnSmallHump", checkJavaKeyword(StrUtil.underlineToHump(columnUnderLine)));
            // 标题（字段描述）
            columnInfoResult.put("columnTitle", columnInfo.get("title"));
            // 是否是主键
            columnInfoResult.put("isPrimaryKey", isPrimaryKey);
            // 是否搜索
            columnInfoResult.put("isSearch", "1".equals(columnInfo.get("search")));
            // 是否排序
            columnInfoResult.put("isSort", "1".equals(columnInfo.get("sort")));
            // 是否必填
            columnInfoResult.put("isNotNull", "1".equals(columnInfo.get("not_null")));
            // 是否编辑
            columnInfoResult.put("isEdit", "1".equals(columnInfo.get("edit")));
            // 是否select
            columnInfoResult.put("isSelect", "1".equals(columnInfo.get("select")));
            // 是否显示
            columnInfoResult.put("isVisible", "1".equals(columnInfo.get("visible")));
            // 对齐
            columnInfoResult.put("align", columnInfo.get("align"));
            // 控件类型 1:输入框 2:密码框 3:日期选择器 4:日期时间选择器 5:选择器 6:开关
            columnInfoResult.put("ControlType", columnInfo.get("ControlType"));

            // 排序字段
            if ("1".equals(columnInfo.get("sort")) && !isPrimaryKey) {
                hasSort = true;
            }

            if ("1".equals(columnInfo.get("select"))) {
                hasSelect = true;

                if (isPrimaryKey) {
                    tableWidth += 100;
                } else if ("1".equals(columnInfo.get("visible"))) {
                    String widthStr = String.valueOf(columnInfo.get("width"));
                    Integer width;
                    try {
                        width = Integer.parseInt(widthStr);
                        if (width > 0 && width <= 500) {
                            tableWidth += width;
                            // 宽度
                            columnInfoResult.put("width", widthStr);
                        } else {
                            tableWidth += 100;
                            columnInfoResult.put("width", "");
                        }

                    } catch (Exception e) {
                        tableWidth += 100;
                        columnInfoResult.put("width", "");
                    }
                    tableWidth += 100;
                }
            }

            // 编辑字段
            if ("1".equals(columnInfo.get("edit")) && !isPrimaryKey) {
                hasEdit = true;
            }

            // 如果是必填项但不是编辑项，返回失败
            if ("1".equals(columnInfo.get("not_null")) && !"1".equals(columnInfo.get("edit")) && !isPrimaryKey) {
                return error("只有可以编辑的字段才可设为必填");
            }
        }

        if (!hasSelect) {
            return error("select至少需要选择一个字段");
        }

        if (!hasEdit) {
            return error("编辑至少需要选择一个字段");
        }

        // 保存到上下文
        // 是否有select字段
        context.put("hasSelect", hasSelect);
        // 是否有编辑字段（主键除外）
        context.put("hasEdit", hasEdit);
        // 是否有排序字段（主键除外）
        context.put("hasSort", hasSort);

        // 表格宽度
        context.put("tableWidth", tableWidth);

        // 使用代码生成器生成代码
        // 生成各种代码
        List<Map<String, String>> result = new ArrayList<>();

        // 1. 生成Java API代码
        String javaApiCode = PebbleTemplateEngine.generate("ls/codetemplate/backend/Api.java.peb", context);
        result.add(createCodeFile(moduleNameBigHump + "Api.java", "java", javaApiCode));

        // 2. 生成HTML列表页面代码
        String htmlListCode = PebbleTemplateEngine.generate("ls/codetemplate/frontend/index.html.peb", context);
        result.add(createCodeFile("/" + moduleName + "/index.html", "html", htmlListCode));

        // 3. 生成JS列表页面代码
        String jsListCode = PebbleTemplateEngine.generate("ls/codetemplate/frontend/index.js.peb", context);
        result.add(createCodeFile("/" + moduleName + "/js/index.js", "javascript", jsListCode));

        // 4. 生成HTML编辑页面代码
        String htmlEditCode = PebbleTemplateEngine.generate("ls/codetemplate/frontend/edit.html.peb", context);
        result.add(createCodeFile("/" + moduleName + "/edit.html", "html", htmlEditCode));

        return success(result);
    }

    /**
     * 创建代码文件对象
     */
    private Map<String, String> createCodeFile(String filename, String language, String content) {
        Map<String, String> file = new HashMap<>();
        file.put("filename", filename);
        file.put("code_language", language);
        file.put("code_content", content);
        return file;
    }

    /**
     * 检查是否是Java关键字
     */
    private static String checkJavaKeyword(String fieldName) {
        return Arrays.asList(KeyWordArray.JAVA).contains(fieldName) ? "_" + fieldName : fieldName;
    }
    /**
     * 处理MySQL关键字（如果是MySQL关键字，加反引号）
     */
    private static String checkMysqlKeyword(String fieldName) {
        return Arrays.asList(KeyWordArray.MYSQL).contains(fieldName.toUpperCase()) ? "`" + fieldName + "`" : fieldName;
    }
}