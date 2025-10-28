package leaf.system.api.test;

import leaf.common.DB;
import leaf.common.object.JSONMap;
import leaf.common.mysql.SQLWhere;
import leaf.system.common.Http;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
public class TestStudentApiOld {
    /**
     * 获取测试学生表列表
     */
    @GetMapping("/api/testStudent/getTestStudentList")
    public JSONMap getTestStudentList() {
        String studentId = Http.param("student_id");
        String sortField = Http.param("SortField");
        String sortOrder = Http.param("SortOrder");
        String sql = "" +
                "select student_id,name,user_id,stuNo,class,is_graduate " +
                "from test_student ";
        String relationship = SQLWhere.Like;

        if("1".equals(Http.param("IsEqual","0"))) {
            relationship = SQLWhere.Equal;
        }

        sql += new SQLWhere().addOr("student_id",studentId,relationship)
                .addOr("student_id",studentId,relationship)
                .toString();
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
        String isGraduate = Http.param("is_graduate");

        switch(updateType) {
            case "Edit":
//                if(!ApiGlobalInterceptor.permission("{$按照实际情况更换$}")) {
//                    Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
//                    return null;
//                }

                if(studentId.isEmpty()) {
                    return JSONMap.error("student_id不能为空");
                }

                if(userId.isEmpty()) {
                    return JSONMap.error("user_id不能为空");
                }

                if(_class.isEmpty()) {
                    return JSONMap.error("class不能为空");
                }

                if(isGraduate.isEmpty()) {
                    return JSONMap.error("is_graduate不能为空");
                }

                String sql = "" +
                        "update test_student " +
                        "set name = '" + DB.e(name) + "',user_id = '" + DB.e(userId) + "',stuNo = '" + DB.e(stuNo) + "',class = '" + DB.e(_class) + "',is_graduate = '" + DB.e(isGraduate) + "' " +
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

                if(userId.isEmpty()) {
                    return JSONMap.error("user_id不能为空");
                }

                if(_class.isEmpty()) {
                    return JSONMap.error("class不能为空");
                }

                if(isGraduate.isEmpty()) {
                    return JSONMap.error("is_graduate不能为空");
                }

                sql = "" +
                        "insert into test_student(name,user_id,stuNo,class,is_graduate)" +
                        "value('"+DB.e(name)+"','"+DB.e(userId)+"','"+DB.e(stuNo)+"','"+DB.e(_class)+"','"+DB.e(isGraduate)+"')";

                if(DB.update(sql) > 0) return JSONMap.success();
                break;
            case "Delete":
//                if(!ApiGlobalInterceptor.permission("{$按照实际情况更换$}")) {
//                    Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
//                    return null;
//                }

                if(studentId.isEmpty()) {
                    return JSONMap.error("student_id不能为空");
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

