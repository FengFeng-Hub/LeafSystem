package leaf.system.api;

import leaf.common.DB;
import leaf.common.object.JSONList;
import leaf.common.object.JSONMap;
import leaf.common.util.StrUtil;
import leaf.common.util.Valid;
import leaf.system.annotate.LoginToken;
import leaf.system.common.Http;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 代码生成模块
 */
@RestController
public class SysCodeGenerationApi {
    String javaKeyword[] = {"abstract","assert","boolean","break","byte","case","catch","char","class","continue","default","do",
            "double","else","enum","extends","final","finally","float","for","if","implements","import","int",
            "interface","instanceof","long","native","new","package","private","protected","public","return","short","static",
            "strictfp","super","switch","synchronized","this","throw","throws","transient","try","void","volatile","while",
            "goto","const","true","false","null"};
    /**
     * 获取数据库表格信息
     */
    @GetMapping("/system/api/codeGeneration/getDBTableInfo")
    @LoginToken(validBackend = true,permissionKey = "lspk:ls:codeGeneration:DBTableInfo")
    public JSONMap getDBTableInfo() {
        String tableName = Http.param("table_name");
        String tableComment = Http.param("table_comment");
        String sortField = Http.param("SortField");
        String sortOrder = Http.param("SortOrder");
        tableName = DB.e(tableName);
        tableComment = DB.e(tableComment);
        String sql = "" +
                "select table_name 'table_name',table_comment 'table_comment',table_schema 'table_schema'," +
                "   create_time 'create_time',update_time 'update_time',table_rows 'table_rows',auto_increment 'auto_increment' " +
                "from information_schema.tables " +
                "where table_schema = (select database()) ";

        if("1".equals(Http.param("IsEqual","0"))) {
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
        return DB.sqlToJSONMap(sql,Http.param("PageNo"),Http.param("PageCount"),"100");
    }
    /**
     * 获取数据库表格字段信息
     */
    @GetMapping("/system/api/codeGeneration/getDBTableFieldInfo")
    @LoginToken(validBackend = true,permissionKey = "lspk:ls:codeGeneration:DBTableFieldInfo")
    public JSONMap getDBTableFieldInfo() {
        return JSONMap.success(DB.query("" +
                "select column_name 'column_name',column_comment 'column_comment',column_type 'column_type'," +
                "   is_nullable 'is_nullable',column_default 'column_default',column_key 'column_key' " +
                "from information_schema.columns " +
                "where table_schema = (select database()) and table_name = '" + DB.e(Http.param("table_name")) + "' " +
                "order by ordinal_position"));
    }
    /**
     * 生成代码
     */
    @PostMapping("/system/api/codeGeneration/generationCode")
    @LoginToken(validBackend = true,permissionKey = "lspk:ls:codeGeneration:generationCode")
    public JSONMap generationCode(@RequestBody JSONMap requestJSON) {
//        System.out.println(requestJSON);
        String tableName = requestJSON.getString("table_name");
        String tableDesc = requestJSON.getString("table_desc");
        String moduleName = requestJSON.getString("module_name");
        String apiRootAddress = requestJSON.getString("api_root_address");
        String primaryKeyColumnName = requestJSON.getString("primary_key_column_name");//主键字段名
        String primaryKeyColumnSmallHump = StrUtil.underlineToHump(primaryKeyColumnName);//主键字段小驼峰
        String primaryKeyColumnDesc = "主键代码";//主键字段描述
        List<Map> columnInfoList = (List<Map>) requestJSON.get("column_info_list");

        if(Valid.isEmpty(tableName)) {
            return JSONMap.error("表名不能为空");
        }

        if(Valid.isEmpty(tableDesc)) {
            return JSONMap.error("表描述不能为空");
        }

        if(Valid.isEmpty(moduleName)) {
            return JSONMap.error("模块名不能为空");
        }

        if(Valid.isEmpty(apiRootAddress)) {
            return JSONMap.error("接口根地址不能为空");
        }

        if(Valid.isEmpty(primaryKeyColumnName)) {
            return JSONMap.error("主键字段名不能为空");
        }

        if(columnInfoList.size() < 1) {
            return JSONMap.error("没有一个字段信息");
        }

        String tableDesc2 = tableDesc.substring(0, tableDesc.length() - 1);
        String moduleNameBigHump = moduleName.substring(0,1).toUpperCase() + moduleName.substring(1);//模块名大驼峰
        Map<String,String> columnInfo;

        //字段信息
        String columnUnderLine;
        String columnSmallHump;
        String columnTitle;

        boolean primaryKeyIsNotNull = false;
        boolean primaryKeyIsSelect = false;
        boolean isEditable = false;//是否可以编辑

        //获取列表
        String javaApiGetListRequestParamCode = "       String " + primaryKeyColumnSmallHump + " = Http.param(\"" + primaryKeyColumnName + "\");\n";//请求参数
        String javaApiGetListSelectSqlCode = "";//select后面的字段SQL
        String javaApiGetListWhereSqlCode = "                .or().add(\"" + primaryKeyColumnName + "\", " + primaryKeyColumnSmallHump + ", Where.Operator.EQ)\n";//where条件
        String javaApiGetListOrderBySqlSwitchCode = "";//排序字段
        String javaApiSwitchParam = "";
        //修改
        String javaApiUpdateRequestParamCode = "        String " + primaryKeyColumnSmallHump + " = Http.param(\"" + primaryKeyColumnName + "\");\n";//请求参数
        String javaApiUpdateValidPrimaryKeyNotNullCode = "";//验证主键非空
        String javaApiUpdateValidNotNullCode = "";//验证非空
        String javaApiUpdateSetSqlCode = "";//update set后面的SQL
        String javaApiUpdateAddFieldSqlCode = "";//添加的字段名SQL
        String javaApiUpdateAddValueSqlCode = "";//添加的值SQL

        //菜单代码
        String htmlMenuSearchCode = "";//搜索框
        String jsTableFieldCode = "";//JS表格字段
        //菜单编辑代码
        String htmlMenuEditFormInputCode = "";//表单控件
        String htmlMenuEditValid = "";//表单验证
        String htmlMenuEditRequestParamCode = "";//Ajax请求参数


        for (int i = 0;i < columnInfoList.size();i++) {
            //请求参数代码
            columnInfo = columnInfoList.get(i);
            columnUnderLine = columnInfo.get("column_name");
            columnSmallHump = StrUtil.underlineToHump(columnUnderLine);
            columnSmallHump = Arrays.asList(javaKeyword).contains(columnSmallHump)?"_" + columnSmallHump:columnSmallHump;
            columnTitle = columnInfo.get("title");

            //搜索
            if("1".equals(columnInfo.get("search"))) {
                //不为主键时候
                if(!primaryKeyColumnName.equals(columnUnderLine)) {
                    javaApiGetListRequestParamCode += "       String " + columnSmallHump + " = Http.param(\"" + columnUnderLine + "\");\n";
                    javaApiGetListWhereSqlCode += "                .or().add(\"" + columnUnderLine + "\", " + columnSmallHump + ", relationship)\n";
                    htmlMenuSearchCode += "                <a-select-option value=\"" + columnUnderLine + "\">" + columnTitle + "</a-select-option>\n";
                }
            }

            //排序
            if("1".equals(columnInfo.get("sort"))) {
                javaApiGetListOrderBySqlSwitchCode += "            case \"" + columnUnderLine + "\":\n";
            }

            //编辑
            isEditable = "1".equals(columnInfo.get("edit"));

            if(isEditable) {
                if(primaryKeyColumnName.equals(columnUnderLine)) {
                    return JSONMap.error("主键不允许编辑");
                }

                javaApiUpdateRequestParamCode += "        String " + columnSmallHump + " = Http.param(\"" + columnUnderLine + "\");\n";
                javaApiUpdateSetSqlCode += columnUnderLine + " = '\" + DB.e(" + columnSmallHump + ") + \"',";
                javaApiUpdateAddFieldSqlCode += columnUnderLine + ",";
                javaApiUpdateAddValueSqlCode += "'\"+DB.e(" + columnSmallHump + ")+\"',";

                htmlMenuEditFormInputCode += "" +
                        "        <a-form-model-item label=\"" + columnTitle + "\" prop=\"" + columnUnderLine + "\" :label-col=\"{span: 3}\" :wrapper-col=\"{span: 20}\">\n";
                switch (columnInfo.get("ControlType")) {
                    case "2":
                        htmlMenuEditFormInputCode += "" +
                                "            <a-input-password v-model=\"formData." + columnUnderLine + "\" :disabled=\"formDisabled\"/>\n";
                        break;
                    case "3":
                        htmlMenuEditFormInputCode += "" +
                                "            <a-date-picker\n" +
                                "                v-model=\"formData." + columnUnderLine + "\"\n" +
                                "                :disabled=\"formDisabled\"\n" +
                                "                format=\"yyyy年MM月DD日\" style=\"width: 200px;\"\n" +
                                "                value-format=\"yyyy-MM-DD\"\n" +
                                "            />\n";
                        break;
                    case "4":
                        htmlMenuEditFormInputCode += "" +
                                "            <a-date-picker\n" +
                                "                v-model=\"formData." + columnUnderLine + "\"\n" +
                                "                :disabled=\"formDisabled\"\n" +
                                "                format=\"yyyy年MM月DD日 HH:mm:ss\"\n" +
                                "                value-format=\"yyyy-MM-DD HH:mm:ss\"\n" +
                                "                show-time\n" +
                                "                style=\"width: 200px;\"\n" +
                                "            />\n";

                        break;
                    case "5":
                        htmlMenuEditFormInputCode += "" +
                                "           <a-select v-model=\"formData." + columnUnderLine + "\" :disabled=\"formDisabled\" style=\"width: 100px;\">\n" +
                                "               <a-select-option value=\"1\">选项1</a-select-option>\n" +
                                "               <a-select-option value=\"2\">选项2</a-select-option>\n" +
                                "           </a-select>\n";

                        break;
                    case "6":
                        htmlMenuEditFormInputCode += "           <a-switch :checked=\"formData." + columnUnderLine + " == '1'\" :disabled=\"formDisabled\" @change=\"formData." + columnUnderLine + " = formData." + columnUnderLine + " === '1'?'0':'1'\"/>\n";

                        javaApiSwitchParam += "" +
                                "       if (!\"1\".equals(" + columnSmallHump + ")) {\n" +
                                "          " + columnSmallHump + " = \"0\";\n" +
                                "       }" +
                                "\n";
                        break;
                    case "1":
                    default:
                        htmlMenuEditFormInputCode += "            <a-input v-model=\"formData." + columnUnderLine + "\" :disabled=\"formDisabled\"/>\n";
                }
                htmlMenuEditFormInputCode += "        </a-form-model-item>\n";


                htmlMenuEditRequestParamCode += "                            " + columnUnderLine + ": null,\n";
            }

            //必填
            if(primaryKeyColumnName.equals(columnUnderLine)) {
                if("1".equals(columnInfo.get("not_null"))) {
                    return JSONMap.error("只有可以编辑的字段才可设为必填");
                }

                javaApiUpdateValidPrimaryKeyNotNullCode += "" +
                        "                if(" + columnSmallHump + ".isEmpty()) {\n" +
                        "                    return JSONMap.error(\"" + columnTitle + "不能为空\");\n" +
                        "                }\n" +
                        "\n";
                primaryKeyColumnDesc = columnTitle;
                primaryKeyIsNotNull = true;
            } else if("1".equals(columnInfo.get("not_null"))) {
                if(!isEditable) {
                    return JSONMap.error("只有可以编辑的字段才可设为必填");
                }

                javaApiUpdateValidNotNullCode += "" +
                        "                if(" + columnSmallHump + ".isEmpty()) {\n" +
                        "                    return JSONMap.error(\"" + columnTitle + "不能为空\");\n" +
                        "                }\n" +
                        "\n";

                htmlMenuEditValid += "" +
                        "                    " + columnUnderLine + ": [\n" +
                        "                        { required: true, message: '" + columnTitle + "为必填项', trigger: 'change' },\n" +
                        "                    ],\n";
            }

            //select
            if("1".equals(columnInfo.get("select"))) {
                javaApiGetListSelectSqlCode += columnUnderLine + ",";

                if (primaryKeyColumnName.equals(columnUnderLine)) {
                    primaryKeyIsSelect = true;
                    jsTableFieldCode += "" +
                            ", {\n" +
                            "                " + "title: 'ID', dataIndex: '" + primaryKeyColumnName + "', align: '" + columnInfo.get("align") + "', sorter: true, width: 100, fixed: \"left\"\n" +
                            "            }";
                } else if ("1".equals(columnInfo.get("visible"))) {
                    jsTableFieldCode += "" +
                            ", {\n" +
                            "                " + "title: '" + columnTitle + "', dataIndex: '" + columnUnderLine + "', align: '" + columnInfo.get("align") + "', " + ("1".equals(columnInfo.get("sort"))?"sorter: true":"") + "\n" +
                            "            }";
                }
            }
        }

        if(javaApiGetListSelectSqlCode.isEmpty()) {
            return JSONMap.error("select至少需要选择一个字段");
        }

        if(!primaryKeyIsSelect) {
            return JSONMap.error("主键必须选择select");
        }

        if(!primaryKeyIsNotNull) {
            return JSONMap.error("主键必须设为必填");
        }

        if(javaApiUpdateSetSqlCode.isEmpty() || javaApiUpdateAddFieldSqlCode.isEmpty() || javaApiUpdateAddValueSqlCode.isEmpty()) {
            return JSONMap.error("编辑至少需要选择一个字段");
        }

        javaApiGetListWhereSqlCode = "" +
                "       Where.Operator relationship = Where.Operator.LIKE;\n" +
                "\n" +
                "        if(\"1\".equals(Http.param(\"IsEqual\",\"0\"))) {\n" +
                "            relationship = Where.Operator.EQ;\n" +
                "        }\n" +
                "\n" +
                "       sql += new Where(true)\n" +
                javaApiGetListWhereSqlCode +
                "                .prependWhere().toString();\n";

        if(!javaApiGetListOrderBySqlSwitchCode.isEmpty()) {
            javaApiGetListOrderBySqlSwitchCode = "\n" +
                    "        //排序字段\n" +
                    "        switch(sortField) {\n" +
                    javaApiGetListOrderBySqlSwitchCode +
                    "                if(sortOrder.equals(\"asc\")) sql += \" order by \" + sortField + \" asc \";\n" +
                    "                else if(sortOrder.equals(\"desc\")) sql += \" order by \" + sortField+\" desc \";\n" +
                    "                break;\n" +
                    "            default:\n" +
                    "                sql += \" order by " + primaryKeyColumnName + " desc \";\n" +
                    "        }\n";
        }

        //去掉最后一个字符
        javaApiGetListSelectSqlCode = javaApiGetListSelectSqlCode.substring(0,javaApiGetListSelectSqlCode.length() - 1);
        javaApiUpdateSetSqlCode = javaApiUpdateSetSqlCode.substring(0,javaApiUpdateSetSqlCode.length() - 1);
        javaApiUpdateAddFieldSqlCode = javaApiUpdateAddFieldSqlCode.substring(0,javaApiUpdateAddFieldSqlCode.length() - 1);
        javaApiUpdateAddValueSqlCode = javaApiUpdateAddValueSqlCode.substring(0,javaApiUpdateAddValueSqlCode.length() - 1);

        String javaApiCode = "" +
                "\n" +
                "import leaf.common.DB;\n" +
                "import leaf.common.object.JSONMap;\n" +
                "import leaf.common.mysql.Where;\n" +
                "import leaf.system.common.Http;\n" +
                "import org.springframework.web.bind.annotation.GetMapping;\n" +
                "import org.springframework.web.bind.annotation.PostMapping;\n" +
                "import org.springframework.web.bind.annotation.RestController;\n" +
                "\n" +
                "@RestController\n" +
                "public class " + moduleNameBigHump + "Api {\n" +
                "    /**\n" +
                "     * 获取" + tableDesc2 + "列表\n" +
                "     */\n" +
                "    @GetMapping(\"" + apiRootAddress + "get" + moduleNameBigHump + "List\")\n" +
                "    public JSONMap get" + moduleNameBigHump + "List() {\n" +
                javaApiGetListRequestParamCode +
                "       String sortField = Http.param(\"SortField\");\n" +
                "       String sortOrder = Http.param(\"SortOrder\");" +
                "\n" +
                "       String sql = \"\" +\n" +
                "                \"select " + javaApiGetListSelectSqlCode + " \" +\n" +
                "                \"from " + tableName + " \";\n" +
                javaApiGetListWhereSqlCode +
                javaApiGetListOrderBySqlSwitchCode +
                "        return DB.sqlToJSONMap(sql,Http.param(\"PageNo\"),Http.param(\"PageCount\"),\"100\");\n" +
                "    }\n" +
                "    /**\n" +
                "     * 修改" + tableDesc2 + "\n" +
                "     */\n" +
                "    @PostMapping(\"" + apiRootAddress + "update" + moduleNameBigHump + "\")\n" +
                "    public JSONMap update" + moduleNameBigHump + "() {\n" +
                "        String updateType = Http.param(\"UpdateType\");\n" +
                javaApiUpdateRequestParamCode +
                "\n" +
                javaApiSwitchParam +
                "\n" +
                "        switch(updateType) {\n" +
                "            case \"Edit\":\n" +
                "//                if(!ApiGlobalInterceptor.permission(\"{$按照实际情况更换$}\")) {\n" +
                "//                    Http.write(403,JSONMap.error(\"接口执行失败，该用户没有权限\"));\n" +
                "//                    return null;\n" +
                "//                }\n" +
                "\n" +
                javaApiUpdateValidPrimaryKeyNotNullCode +
                javaApiUpdateValidNotNullCode +
                "                String sql = \"\" +\n" +
                "                        \"update " + tableName + " \" +\n" +
                "                        \"set " + javaApiUpdateSetSqlCode + " \" +\n" +
                "                        \"where " + primaryKeyColumnName + " = '\"+DB.e(" + primaryKeyColumnSmallHump + ")+\"'\";\n" +
                "\n" +
                "                if(DB.update(sql) > 0) {\n" +
                "                    return JSONMap.success();\n" +
                "                }\n" +
                "                break;\n" +
                "            case \"Add\":\n" +
                "//                if(!ApiGlobalInterceptor.permission(\"{$按照实际情况更换$}\")) {\n" +
                "//                    Http.write(403,JSONMap.error(\"接口执行失败，该用户没有权限\"));\n" +
                "//                    return null;\n" +
                "//                }\n" +
                "\n" +
                javaApiUpdateValidNotNullCode +
                "                sql = \"\" +\n" +
                "                        \"insert into " + tableName + "(" + javaApiUpdateAddFieldSqlCode + ")\" +\n" +
                "                        \"value(" + javaApiUpdateAddValueSqlCode + ")\";\n" +
                "\n" +
                "                if(DB.update(sql) > 0) return JSONMap.success();\n" +
                "                break;\n" +
                "            case \"Delete\":\n" +
                "//                if(!ApiGlobalInterceptor.permission(\"{$按照实际情况更换$}\")) {\n" +
                "//                    Http.write(403,JSONMap.error(\"接口执行失败，该用户没有权限\"));\n" +
                "//                    return null;\n" +
                "//                }\n" +
                "\n" +
                "                if(" + primaryKeyColumnSmallHump + ".isEmpty()) {\n" +
                "                    return JSONMap.error(\"" + primaryKeyColumnDesc + "不能为空\");\n" +
                "                }\n" +
                "\n" +
                "                if(DB.update(\"delete from " + tableName + " where " + primaryKeyColumnName + " = '\"+DB.e(" + primaryKeyColumnSmallHump + ")+\"'\") > 0) {\n" +
                "                    return JSONMap.success();\n" +
                "                }\n" +
                "                break;\n" +
                "            default:\n" +
                "                return JSONMap.error(\"修改类型有误\");\n" +
                "        }\n" +
                "        return JSONMap.error(\"操作失败\");\n" +
                "    }\n" +
                "}\n";

        String htmlMenuCode = "" +
                "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>列表页面</title>\n" +
                "    <style>\n" +
                "        /* 表头居中 */\n" +
                "        .ls-container .ant-table-thead > tr > th {\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"ls-card ls-container\">\n" +
                "    <a-button v-if=\"pageParams.backMenuParams\" size=\"small\"\n" +
                "        @click=\"QiankunUtil.toPage('/backend/systemManage/menu/index.html', {backParams: pageParams.backMenuParams})\"\n" +
                "        style=\"margin-right: 10px;\"><a-icon type=\"rollback\"></a-icon> 返回</a-button>\n" +
                "    <b>" + tableDesc2 + "管理</b>\n" +
                "    <a-divider></a-divider>\n" +
                "    <div class=\"ls-table-tool\">\n" +
                "        <a-input-group compact style=\"width: 400px;\">\n" +
                "            <a-select v-model=\"searchConfig.searchType\" style=\"width: 130px;\">\n" +
                "                <a-select-option value=\"" + primaryKeyColumnName + "\">ID</a-select-option>\n" +
                htmlMenuSearchCode +
                "            </a-select>\n" +
                "            <a-select v-model=\"searchConfig.IsEqual\" style=\"width: 70px;\">\n" +
                "                <a-select-option value=\"0\">包含</a-select-option>\n" +
                "                <a-select-option value=\"1\">等于</a-select-option>\n" +
                "            </a-select>\n" +
                "            <a-input v-model=\"searchConfig.searchKey\" placeholder=\"请输入搜索关键字\" style=\"width: calc(100% - 200px);\"/>\n" +
                "        </a-input-group>\n" +
                "        <a-button type=\"primary\" @click=\"search(false)\"><a-icon type=\"search\"></a-icon> 搜索</a-button>\n" +
                "        <a-button @click=\"search(true)\"><a-icon type=\"sync\"></a-icon> 重置</a-button>\n" +
                "    </div>\n" +
                "    <div class=\"ls-table-tool\" style=\"width: 100%;display: flex;justify-content: space-between;\">\n" +
                "        <div>\n" +
                "            <a-button type=\"primary\" @click=\"edit(null,'Add')\" v-if=\"$ls.hasPermission('')\"><a-icon type=\"plus\"></a-icon> 新建</a-button>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <a-table\n" +
                "        :columns=\"tableColumns\"\n" +
                "        :data-source=\"tableData\"\n" +
                "        :pagination=\"tablePagination\"\n" +
                "        @change=\"handleTableChange\"\n" +
                "        :loading=\"tableLoading\"\n" +
                "        :row-selection=\"{\n" +
                "            selectedRowKeys: selectedRowKeys,\n" +
                "            onChange: onSelectChange,\n" +
                "        }\"\n" +
                "        :scroll=\"{ x: " + (240 + (columnInfoList.size() - 1) * 100) + " }\"\n" +
                "        size=\"small\" bordered\n" +
                "    >\n" +
                "        <template slot=\"action\" slot-scope=\"text, record\">\n" +
                "            <a-button type=\"link\" size=\"small\" title=\"查看\" @click=\"edit(record." + primaryKeyColumnName + ",'View')\" v-if=\"$ls.hasPermission('')\"><a-icon type=\"eye\"></a-icon></a-button>\n" +
                "            <a-button type=\"link\" size=\"small\" title=\"编辑\" @click=\"edit(record." + primaryKeyColumnName + ",'Edit')\" v-if=\"$ls.hasPermission('')\"><a-icon type=\"edit\"></a-icon></a-button>\n" +
                "            <a-popconfirm title=\"确定删除该项？\" v-if=\"$ls.hasPermission('')\" @confirm=\"edit(record." + primaryKeyColumnName + ",'Delete')\" @cancel=\"\" ok-text=\"确定\" cancel-text=\"取消\">\n" +
                "                <a-button type=\"link\" size=\"small\" title=\"删除\" style=\"color: #f5222d;\"><a-icon type=\"delete\"></a-icon></a-button>\n" +
                "            </a-popconfirm>\n" +
                "        </template>\n" +
                "    </a-table>\n" +
                "</div>\n" +
                "</body>\n" +
                "<script src=\"./js/index.js\"></script>\n" +
                "</html>";

        String jsMenuCode = "" +
                "function init(props, params) {\n" +
                "    let _this = null;\n" +
                "\n" +
                "    new Vue({\n" +
                "        el: \".ls-container\",\n" +
                "        data: {\n" +
                "            pageParams: params,\n" +
                "            // 表格字段\n" +
                "            tableColumns: [{\n" +
                "                title: '操作', align: 'center', width: 120, fixed: 'left', scopedSlots: { customRender: 'action' }\n" +
                "            }, " + jsTableFieldCode + "],\n" +
                "            // 表格数据\n" +
                "            tableData: [],\n" +
                "            tableLoading: false,\n" +
                "            // 表格分页配置\n" +
                "            tablePagination: {\n" +
                "                pageSize: 10,  // 每页显示的条数\n" +
                "                showSizeChanger: true,  // 是否可以改变每页显示的条数\n" +
                "                pageSizeOptions: [1, 2, 5, 10, 30, 50, 100, 200, 300, 500, 600, 700, 800, 900, 1000], // 可选的每页显示条数\n" +
                "                showQuickJumper: true,  // 是否可以快速跳转到指定页\n" +
                "                showTotal: total => `共 ${total} 条`,  // 显示总条数和当前数据范围\n" +
                "                current: 1, // 当前页数\n" +
                "                total: 0 // 总条数\n" +
                "            },\n" +
                "            // 表格排序配置\n" +
                "            tableSort: {\n" +
                "                field: '',\n" +
                "                order: ''\n" +
                "            },\n" +
                "            // 搜索配置\n" +
                "            searchConfig: {\n" +
                "                searchType: '" + primaryKeyColumnName + "',\n" +
                "                searchKey: '',\n" +
                "                IsEqual: '0',\n" +
                "                model: {\n" +
                "                },\n" +
                "                params: params.backParams?.searchParams\n" +
                "            },\n" +
                "            // 多选框\n" +
                "            // 选中的表格行key\n" +
                "            selectedRowKeys: [],\n" +
                "            selectedIds: []\n" +
                "        },\n" +
                "        created() {\n" +
                "            _this = this;\n" +
                "            // 加载数据\n" +
                "            this.loadTableList(params.backParams?params.backParams:{\n" +
                "                PageNo: this.tablePagination.current,\n" +
                "                PageCount: this.tablePagination.pageSize\n" +
                "            });\n" +
                "        },\n" +
                "        mounted() {},\n" +
                "        methods: {\n" +
                "            handleTableChange(pagination, filters, sorter) {\n" +
                "                this.loadTableList({\n" +
                "                    ...{\n" +
                "                        PageNo: pagination.current,\n" +
                "                        PageCount: pagination.pageSize,\n" +
                "                        SortField: sorter.field,\n" +
                "                        SortOrder: sorter.order === 'ascend' ? 'asc' : sorter.order === 'descend' ? 'desc' : ''\n" +
                "                    }, ...filters\n" +
                "                });\n" +
                "            },\n" +
                "            // 加载表格列表\n" +
                "            loadTableList(params) {\n" +
                "                this.tableLoading = true;\n" +
                "                Ajax.get({\n" +
                "                    url: '" + apiRootAddress + "get" + moduleNameBigHump + "List',\n" +
                "                    param: {...params, ...this.searchConfig.params},\n" +
                "                    success(result) {\n" +
                "                        if(result.IsSuccess === '1') {\n" +
                "                            _this.tableData = result.data;\n" +
                "                            _this.selectedRowKeys = [];\n" +
                "                            _this.selectedIds = [];\n" +
                "                            _this.tablePagination.total = result.dataCount;\n" +
                "                            _this.tableLoading = false;\n" +
                "                        } else {\n" +
                "                            _this.$message.error(result.Msg);\n" +
                "                        }\n" +
                "                    }\n" +
                "                });\n" +
                "                this.$ls.table.updatePaginationAndSort.call(this ,params);\n" +
                "            },\n" +
                "            // 搜索\n" +
                "            search(isReset) {\n" +
                "                for (const key in this.searchConfig.params) {\n" +
                "                    delete this.searchConfig.params[key]\n" +
                "                }\n" +
                "\n" +
                "                if (!isReset) {\n" +
                "                    this.searchConfig.params = Object.assign({},this.searchConfig.model);\n" +
                "                    this.searchConfig.params[this.searchConfig.searchType] = this.searchConfig.searchKey;\n" +
                "                    this.tablePagination.current = 1;\n" +
                "                }\n" +
                "\n" +
                "                this.$ls.table.clearSort.call(this);\n" +
                "                this.loadTableList({\n" +
                "                    PageNo: 1,\n" +
                "                    PageCount: params.pageSize?params.pageSize:this.tablePagination.pageSize,\n" +
                "                    SortField: this.tableSort.field,\n" +
                "                    SortOrder: this.tableSort.order\n" +
                "                });\n" +
                "            },\n" +
                "            // 编辑表格\n" +
                "            edit(id,operate) {\n" +
                "                if (operate === 'Delete') {\n" +
                "                    Ajax.post({\n" +
                "                        url: '" + apiRootAddress + "update" + moduleNameBigHump + "?UpdateType=Delete',\n" +
                "                        param: {\n" +
                "                            " + primaryKeyColumnName + ": id\n" +
                "                        },\n" +
                "                        success(result) {\n" +
                "                            if(result.IsSuccess === '1') {\n" +
                "                                _this.$message.success('删除成功');\n" +
                "                                _this.loadTableList({\n" +
                "                                    PageNo: _this.tablePagination.current,\n" +
                "                                    PageCount: _this.tablePagination.pageSize,\n" +
                "                                    SortField: _this.tableSort.field,\n" +
                "                                    SortOrder: _this.tableSort.order\n" +
                "                                });\n" +
                "                            } else {\n" +
                "                                _this.$message.error(result.Msg);\n" +
                "                            }\n" +
                "                        }\n" +
                "                    });\n" +
                "                    return;\n" +
                "                }\n" +
                "\n" +
                "                QiankunUtil.toPage(props.parentUrl + 'edit.html', {\n" +
                "                    id: id,\n" +
                "                    operate: operate,\n" +
                "                    backParams: {\n" +
                "                        PageNo: this.tablePagination.current,\n" +
                "                        PageCount: this.tablePagination.pageSize,\n" +
                "                        SortField: this.tableSort.field,\n" +
                "                        SortOrder: this.tableSort.order,\n" +
                "                        searchParams: this.searchConfig.params\n" +
                "                    }\n" +
                "                });\n" +
                "            },\n" +
                "            // 多选框变换事件\n" +
                "            onSelectChange(selectedKeys,selectedItems) {\n" +
                "                this.selectedRowKeys = selectedKeys;\n" +
                "                this.selectedIds = selectedItems.map(item => item." + primaryKeyColumnName + ");\n" +
                "            }\n" +
                "        }\n" +
                "    });\n" +
                "}\n" +
                "\n" +
                "// 获取qiankun子应用生命周期\n" +
                "QiankunUtil.getLifecycles({\n" +
                "    mount(props) {\n" +
                "        init(props, props.params);\n" +
                "    }\n" +
                "});";

        String htmlMenuEditCode = "" +
                "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>编辑页面</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"ls-card ls-container\">\n" +
                "    <div style=\"height: 50px;position: sticky;top: 24px;z-index: 1;\">\n" +
                "        <a-button size=\"small\" @click=\"onBack\"><a-icon type=\"rollback\"></a-icon> 返回</a-button>\n" +
                "        <a-button v-if=\"operate !== 'View'\" type=\"primary\" size=\"small\" @click=\"savaForm\" style=\"margin-left: 10px;\"><a-icon type=\"save\"></a-icon> 保存</a-button>\n" +
                "    </div>\n" +
                "    <a-form-model :model=\"formData\" :rules=\"formDataRules\" ref=\"form\">\n" +
                "        <a-form-model-item v-if=\"operate !== 'Add'\" label=\"ID\" :label-col=\"{span: 3}\" :wrapper-col=\"{span: 20}\">\n" +
                "            <a-input v-model=\"id\" disabled/>\n" +
                "        </a-form-model-item>\n" +
                htmlMenuEditFormInputCode +
                "    </a-form-model>\n" +
                "</div>\n" +
                "</body>\n" +
                "<script>\n" +
                "    function init(props, params) {\n" +
                "        let _this = null;\n" +
                "\n" +
                "        new Vue({\n" +
                "            el: \".ls-container\",\n" +
                "            data: {\n" +
                "                operate: params.operate,\n" +
                "                id: params.id,\n" +
                "                // 禁用表单\n" +
                "                formDisabled: false,\n" +
                "                // 表单数据\n" +
                "                formData: {\n" +
                htmlMenuEditRequestParamCode +
                "                },\n" +
                "                // 表单验证\n" +
                "                formDataRules: {\n" +
                htmlMenuEditValid +
                "\n" +
                "                }\n" +
                "            },\n" +
                "            created() {\n" +
                "                _this = this;\n" +
                "\n" +
                "                if (this.operate === 'View') {\n" +
                "                    this.formDisabled = true;\n" +
                "                }\n" +
                "\n" +
                "                if (this.operate !== 'Add') {\n" +
                "                    Ajax.get({\n" +
                "                        url: '" + apiRootAddress + "get" + moduleNameBigHump + "List',\n" +
                "                        param: {\n" +
                "                            " + primaryKeyColumnName + ": _this.id\n" +
                "                        },\n" +
                "                        success(result) {\n" +
                "                            if(result.IsSuccess === '1' && result.data.length > 0) {\n" +
                "                                _this.formData = result.data[0];\n" +
                "                            } else {\n" +
                "                                _this.$message.error(result.Msg);\n" +
                "                            }\n" +
                "                        }\n" +
                "                    });\n" +
                "                }\n" +
                "            },\n" +
                "            methods: {\n" +
                "                // 返回\n" +
                "                onBack() {\n" +
                "                    QiankunUtil.toPage(props.parentUrl + 'index.html', { backParams: params.backParams });\n" +
                "                },\n" +
                "                // 保存表单\n" +
                "                savaForm() {\n" +
                "                    this.$refs.form.validate(valid => {\n" +
                "                        if (valid) {\n" +
                "                            let param = this.formData;\n" +
                "\n" +
                "                            if (this.operate === 'Edit') {\n" +
                "                                param." + primaryKeyColumnName + " = this.id;\n" +
                "                            }\n" +
                "\n" +
                "                            Ajax.post({\n" +
                "                                url: '" + apiRootAddress + "update" + moduleNameBigHump + "?UpdateType=' + this.operate,\n" +
                "                                param: param,\n" +
                "                                success(result) {\n" +
                "                                    if(result.IsSuccess === '1') {\n" +
                "                                        _this.$message.success('操作成功');\n" +
                "                                        _this.onBack();\n" +
                "                                    } else {\n" +
                "                                        _this.$message.error(result.Msg);\n" +
                "                                    }\n" +
                "                                }\n" +
                "                            });\n" +
                "                        } else {\n" +
                "                            this.$message.error('表单校验失败，请检查输入');\n" +
                "                        }\n" +
                "                    });\n" +
                "                }\n" +
                "            }\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    // 获取qiankun子应用生命周期\n" +
                "    QiankunUtil.getLifecycles({\n" +
                "        mount(props) {\n" +
                "            init(props, props.params);\n" +
                "        }\n" +
                "    });\n" +
                "</script>\n" +
                "</html>";

        JSONList result = new JSONList();
        JSONMap javaApi = new JSONMap();
        JSONMap htmlMenu = new JSONMap();
        JSONMap jsMenu = new JSONMap();
        JSONMap htmlMenuEdit = new JSONMap();
        javaApi.put("filename",moduleNameBigHump + "Api.java");
        javaApi.put("code_language","java");
        javaApi.put("code_content",javaApiCode);
        htmlMenu.put("filename","/" + moduleName + "/index.html");
        htmlMenu.put("code_language","html");
        htmlMenu.put("code_content",htmlMenuCode);
        jsMenu.put("filename","/" + moduleName + "/js/index.js");
        jsMenu.put("code_language","javascript");
        jsMenu.put("code_content",jsMenuCode);
        htmlMenuEdit.put("filename","/" + moduleName + "/edit.html");
        htmlMenuEdit.put("code_language","html");
        htmlMenuEdit.put("code_content",htmlMenuEditCode);
        result.add(javaApi);
        result.add(htmlMenu);
        result.add(jsMenu);
        result.add(htmlMenuEdit);
        return JSONMap.success(result);
    }
}
