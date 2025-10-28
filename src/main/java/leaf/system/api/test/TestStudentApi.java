package leaf.system.api.test;

import leaf.common.DB;
import leaf.common.object.JSONMap;
import leaf.common.mysql.Where;
import leaf.system.common.Http;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestStudentApi {
    /**
     * 获取测试学生表列表
     */
    @GetMapping("/api/testStudent/getTestStudentList")
    public JSONMap getTestStudentList() {
        String studentId = Http.param("student_id");
        String name = Http.param("name");
        String stuNo = Http.param("stuNo");
        String _class = Http.param("class");
        String sortField = Http.param("SortField");
        String sortOrder = Http.param("SortOrder");
        String sql = "" +
                "select student_id,name,user_id,stuNo,class,sex,is_graduate,birthday,create_time " +
                "from test_student ";
        Where.Operator relationship = Where.Operator.LIKE;

        if("1".equals(Http.param("IsEqual","0"))) {
            relationship = Where.Operator.EQ;
        }

        sql += new Where(true)
                .or().add("student_id", studentId, Where.Operator.EQ)
                .or().add("name", name, relationship)
                .or().add("stuNo", stuNo, relationship)
                .or().add("class", _class, relationship)
                .prependWhere().toString();

        //排序字段
        switch(sortField) {
            case "student_id":
            case "stuNo":
            case "class":
            case "sex":
            case "is_graduate":
            case "birthday":
            case "create_time":
                if(sortOrder.equals("asc")) sql += " order by " + sortField + " asc ";
                else if(sortOrder.equals("desc")) sql += " order by " + sortField+" desc ";
                break;
            default:
                sql += " order by student_id desc ";
        }
        return DB.sqlToJSONMap(sql,Http.param("PageNo"),Http.param("PageCount"),"100");
    }
    /**
     * 修改测试学生表
     */
    @PostMapping("/api/testStudent/updateTestStudent")
    public JSONMap updateTestStudent() {
        String updateType = Http.param("UpdateType");
        String studentId = Http.param("student_id");
        String name = Http.param("name");
        String userId = Http.param("user_id");
        String stuNo = Http.param("stuNo");
        String _class = Http.param("class");
        String sex = Http.param("sex");
        String isGraduate = Http.param("is_graduate");
        String birthday = Http.param("birthday");
        String createTime = Http.param("create_time");

        if (!"1".equals(isGraduate)) {
            isGraduate = "0";
        }

        switch(updateType) {
            case "Edit":
//                if(!ApiGlobalInterceptor.permission("{$按照实际情况更换$}")) {
//                    Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
//                    return null;
//                }

                if(studentId.isEmpty()) {
                    return JSONMap.error("学生代码不能为空");
                }

                if(name.isEmpty()) {
                    return JSONMap.error("姓名不能为空");
                }

                if(userId.isEmpty()) {
                    return JSONMap.error("用户代码不能为空");
                }

                if(stuNo.isEmpty()) {
                    return JSONMap.error("学号不能为空");
                }

                String sql = "" +
                        "update test_student " +
                        "set name = '" + DB.e(name) + "',user_id = '" + DB.e(userId) + "',stuNo = '" + DB.e(stuNo) + "',class = '" + DB.e(_class) + "',sex = '" + DB.e(sex) + "',is_graduate = '" + DB.e(isGraduate) + "',birthday = '" + DB.e(birthday) + "',create_time = '" + DB.e(createTime) + "' " +
                        "where student_id = '"+DB.e(studentId)+"'";

                if(DB.update(sql) > 0) {
                    return JSONMap.success();
                }
                break;
            case "Add":
//                if(!ApiGlobalInterceptor.permission("{$按照实际情况更换$}")) {
//                    Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
//                    return null;
//                }

                if(name.isEmpty()) {
                    return JSONMap.error("姓名不能为空");
                }

                if(userId.isEmpty()) {
                    return JSONMap.error("用户代码不能为空");
                }

                if(stuNo.isEmpty()) {
                    return JSONMap.error("学号不能为空");
                }

                sql = "" +
                        "insert into test_student(name,user_id,stuNo,class,sex,is_graduate,birthday,create_time)" +
                        "value('"+DB.e(name)+"','"+DB.e(userId)+"','"+DB.e(stuNo)+"','"+DB.e(_class)+"','"+DB.e(sex)+"','"+DB.e(isGraduate)+"','"+DB.e(birthday)+"','"+DB.e(createTime)+"')";

                if(DB.update(sql) > 0) return JSONMap.success();
                break;
            case "Delete":
//                if(!ApiGlobalInterceptor.permission("{$按照实际情况更换$}")) {
//                    Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
//                    return null;
//                }

                if(studentId.isEmpty()) {
                    return JSONMap.error("学生代码不能为空");
                }

                if(DB.update("delete from test_student where student_id = '"+DB.e(studentId)+"'") > 0) {
                    return JSONMap.success();
                }
            default:
                return JSONMap.error("修改类型有误");
        }
        return JSONMap.error("操作失败");
    }
}
