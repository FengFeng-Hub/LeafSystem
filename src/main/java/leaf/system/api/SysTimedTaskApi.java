package leaf.system.api;

import leaf.common.DB;
import leaf.common.object.JSONList;
import leaf.common.object.JSONMap;
import leaf.common.mysql.Where;
import leaf.common.util.Valid;
import leaf.system.annotate.LoginToken;
import leaf.system.common.Http;
import leaf.system.common.SysUser;
import leaf.system.interceptor.ApiGlobalInterceptor;
import leaf.system.service.SysTimedTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.locks.ReentrantLock;

@RestController
public class SysTimedTaskApi {
    @Autowired
    private SysTimedTaskService sysTimedTaskService;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 获取定时任务列表
     */
    @GetMapping("/system/api/timedTask/getSysTimedTaskList")
    @LoginToken(validBackend = true, permissionKey = "lspk:ls:timedTask:list")
    public JSONMap getSysTimedTaskList() {
        String timedTaskId = Http.param("timed_task_id");
        String taskDesc = Http.param("task_desc");
        String taskGroup = Http.param("task_group");
        String funcPath = Http.param("func_path");
        String cronExpression = Http.param("cron_expression");
        String status = Http.param("status");
        String sortField = Http.param("SortField");
        String sortOrder = Http.param("SortOrder");
        String sql = "" +
                "select timed_task_id,task_desc,task_group,func_path,cron_expression,status,misfire_policy,concurrent_execute,is_log,memo," +
                "   ls_create_time,b.name 'ls_create_by',ls_update_time,c.name 'ls_update_by' " +
                "from sys_timed_task a " +
                "   left join sys_user b on a.ls_create_by = b.user_id " +
                "   left join sys_user c on a.ls_update_by = c.user_id ";
        Where.Operator relationship = Where.Operator.LIKE;

        if("1".equals(Http.param("IsEqual","0"))) {
            relationship = Where.Operator.EQ;
        }

        sql += new Where(true)
                .or().add("timed_task_id", timedTaskId, Where.Operator.EQ)
                .or().add("task_desc", taskDesc, relationship)
                .or().add("task_group", taskGroup, relationship)
                .or().add("func_path", funcPath, relationship)
                .or().add("cron_expression", cronExpression, relationship)
                .or().add("status", status, Where.Operator.EQ)
                .prependWhere().toString();

        //排序字段
        switch(sortField) {
            case "timed_task_id":
            case "task_desc":
            case "task_group":
            case "func_path":
            case "cron_expression":
            case "status":
            case "misfire_policy":
            case "concurrent_execute":
            case "is_log":
                if(sortOrder.equals("asc")) sql += " order by " + sortField + " asc ";
                else if(sortOrder.equals("desc")) sql += " order by " + sortField+" desc ";
                break;
            default:
                sql += " order by timed_task_id desc ";
        }
        return DB.sqlToJSONMap(sql,Http.param("PageNo"),Http.param("PageCount"),"100");
    }

    /**
     * 修改定时任务
     */
    @PostMapping("/system/api/timedTask/updateSysTimedTask")
    @LoginToken(validBackend = true)
    public JSONMap updateSysTimedTask() {
        String updateType = Http.param("UpdateType");
        String timedTaskId = Http.param("timed_task_id");
        String taskDesc = Http.param("task_desc");
        String taskGroup = Http.param("task_group");
        String funcPath = Http.param("func_path");
        String cronExpression = Http.param("cron_expression");
        String status = Http.param("status");
        String misfirePolicy = Http.param("misfire_policy");
        String concurrentExecute = Http.param("concurrent_execute");
        String isLog = Http.param("is_log");
        String memo = Http.param("memo");
        String backendLoginId = SysUser.getBackendLoginId();

        if (!"1".equals(isLog)) {
            isLog = "0";
        }

        switch (status) {
            case "1":
            case "2":
            case "3":
                break;
            default:
                status = "3";
                break;
        }

        switch (misfirePolicy) {
            case "1":
            case "2":
            case "3":
            case "4":
                break;
            default:
                misfirePolicy = "4";
                break;
        }

        String finalIsLog = isLog;
        String finalStatus = status;
        String finalMisfirePolicy = misfirePolicy;

        JSONMap timedTask = null;
        String _taskGroup = null;

        switch (updateType) {
            case "Execute":
            case "Start":
            case "Pause":
            case "Stop":
                if(timedTaskId.isEmpty()) {
                    return JSONMap.error("定时任务代码不能为空");
                }

                timedTask = DB.queryFirst("" +
                        "select task_desc,task_group,func_path,cron_expression,status,misfire_policy,concurrent_execute,is_log " +
                        "from sys_timed_task " +
                        "where timed_task_id = '" + DB.e(timedTaskId) + "'");

                if (timedTask == null) {
                    return JSONMap.error("获取定时任务失败");
                }

                _taskGroup = timedTask.getString("task_group");

                if (Valid.isEmpty(_taskGroup)) {
                    return JSONMap.error("获取定时任务分组失败");
                }
                break;
        }

        String finalTaskGroup = _taskGroup;
        JSONMap finalTimedTask = timedTask;

        lock.lock();
        try {
            switch(updateType) {
                case "Edit":
                    if(!ApiGlobalInterceptor.permission("lspk:ls:timedTask:edit")) {
                        Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                        return null;
                    }

                    if(timedTaskId.isEmpty()) {
                        return JSONMap.error("定时任务代码不能为空");
                    }

                    if(taskDesc.isEmpty()) {
                        return JSONMap.error("任务描述不能为空");
                    }

                    if(taskGroup.isEmpty()) {
                        return JSONMap.error("任务分组不能为空");
                    }

                    if(funcPath.isEmpty()) {
                        return JSONMap.error("函数路径不能为空");
                    }

                    if(cronExpression.isEmpty()) {
                        return JSONMap.error("Cron表达式不能为空");
                    }

                    String sql = "" +
                            "update sys_timed_task " +
                            "set task_desc = '" + DB.e(taskDesc) + "',task_group = '" + DB.e(taskGroup) + "',func_path = '" + DB.e(funcPath) + "'," +
                            "   cron_expression = '" + DB.e(cronExpression) + "',status = '" + DB.e(status) + "',misfire_policy = '" + DB.e(misfirePolicy) + "'," +
                            "   concurrent_execute = '" + DB.e(concurrentExecute) + "',is_log = '" + DB.e(isLog) + "',memo = '" + DB.e(memo) + "'," +
                            "   ls_update_time = now(),ls_update_by = '" + backendLoginId + "' " +
                            "where timed_task_id = '"+DB.e(timedTaskId)+"'";

//                    if(DB.update(sql) > 0) {
//                        if (sysTimedTaskService.deleteTimedTask(timedTaskId, taskGroup)) {
//                            if (sysTimedTaskService.addTimedTask(
//                                    timedTaskId, taskGroup, cronExpression, funcPath,
//                                    status, misfirePolicy, concurrentExecute, isLog
//                            )) {
//                                return JSONMap.success();
//                            } else {
//                                return JSONMap.error("添加定时任务失败");
//                            }
//                        } else {
//                            return JSONMap.error("删除定时任务失败");
//                        }
//                    }
                    if (
                            DB.updateTransaction(sql, () -> {
                                sysTimedTaskService.deleteTimedTask(timedTaskId, taskGroup);
                                if (sysTimedTaskService.addTimedTask(
                                        timedTaskId, taskGroup, cronExpression, funcPath,
                                        finalStatus, finalMisfirePolicy, concurrentExecute, finalIsLog
                                )) {
                                    return false;
                                } else {
                                    return true;
                                }
                            }) > 0
                    ) {
                        return JSONMap.success();
                    }
                    break;
                case "Add":
                    if(!ApiGlobalInterceptor.permission("lspk:ls:timedTask:add")) {
                        Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                        return null;
                    }

                    if(taskDesc.isEmpty()) {
                        return JSONMap.error("任务描述不能为空");
                    }

                    if(taskGroup.isEmpty()) {
                        return JSONMap.error("任务分组不能为空");
                    }

                    if(funcPath.isEmpty()) {
                        return JSONMap.error("函数路径不能为空");
                    }

                    if(cronExpression.isEmpty()) {
                        return JSONMap.error("Cron表达式不能为空");
                    }

                    sql = "" +
                            "insert into sys_timed_task(task_desc,task_group,func_path,cron_expression,status,misfire_policy,concurrent_execute,is_log,memo,ls_create_time,ls_create_by)" +
                            "value('"+DB.e(taskDesc)+"','"+DB.e(taskGroup)+"','"+DB.e(funcPath)+"','"+DB.e(cronExpression)+"','"+DB.e(status)+"','"+DB.e(misfirePolicy)+"','"+DB.e(concurrentExecute)+"','"+DB.e(isLog)+"','"+DB.e(memo)+"',now(),'"+backendLoginId+"');" +
                            "select last_insert_id() 'new_task_id';";

    //                if(DB.update(sql) > 0) return JSONMap.success();

                    if (
                            !"0".equals(DB.execute(sql, (JSONList result) -> {
                                if(result == null || result.size() < 2) {
                                    return true;
                                }

                                if (result.getString(0) == null || !"1".equals(result.getString(0))) {
                                    return true;
                                }

                                JSONList result2 = result.getList(1);

                                if (result2 == null || result2.isEmpty()) {
                                    return true;
                                }

                                String newTaskId = result2.getMap(0).getString("new_task_id");

                                if (newTaskId == null || newTaskId.isEmpty()) {
                                    return true;
                                }

                                if (sysTimedTaskService.addTimedTask(
                                        newTaskId, taskGroup, cronExpression, funcPath,
                                        finalStatus, finalMisfirePolicy, concurrentExecute, finalIsLog
                                )) {
                                    return false;
                                } else {
                                    return true;
                                }
                            }).getString(0))
                    ) {
                        return JSONMap.success();
                    }
                    break;
                case "Delete":
                    if(!ApiGlobalInterceptor.permission("lspk:ls:timedTask:delete")) {
                        Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                        return null;
                    }

                    if(timedTaskId.isEmpty()) {
                        return JSONMap.error("定时任务代码不能为空");
                    }

                    _taskGroup = DB.queryFirstField("select task_group from sys_timed_task where timed_task_id = '" + DB.e(timedTaskId) + "'");

                    if (Valid.isEmpty(_taskGroup)) {
                        return JSONMap.error("获取定时任务分组失败");
                    }

                    if(DB.update("delete from sys_timed_task where timed_task_id = '"+DB.e(timedTaskId)+"'") > 0) {
                        sysTimedTaskService.deleteTimedTask(timedTaskId, _taskGroup);
                        return JSONMap.success();
                    }
                    break;
                case "Execute":
                    if(!ApiGlobalInterceptor.permission("lspk:ls:timedTask:execute")) {
                        Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                        return null;
                    }

                    if (sysTimedTaskService.executeTimedTask(timedTaskId, finalTimedTask.getString("func_path"), finalTimedTask.getString("is_log"), false)) {
                        return JSONMap.success("执行成功");
                    }
                    break;
                case "Start":
                    if(!ApiGlobalInterceptor.permission("lspk:ls:timedTask:start")) {
                        Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                        return null;
                    }

                    switch (timedTask.getString("status")){
                        // 原本状态是启动，响应失败
                        case "1":
                            return JSONMap.error("启动失败，该任务是启动状态");
                        // 原本状态是暂停，恢复任务
                        case "2":
                            if(
                                    DB.updateTransaction(
                                            "update sys_timed_task set status = '1' where timed_task_id = '" + DB.e(timedTaskId) + "'",
                                            () -> !sysTimedTaskService.resumeJob(timedTaskId, finalTaskGroup)
                                    ) == -1
                            ) {
                                return JSONMap.error("恢复任务失败");
                            } else {
                                return JSONMap.success("恢复任务成功");
                            }
                        // 原本任务是终止，创建任务
                        default:
                            if(
                                    DB.updateTransaction(
                                            "update sys_timed_task set status = '1' where timed_task_id = '" + DB.e(timedTaskId) + "'",
                                            () -> {
                                                sysTimedTaskService.deleteTimedTask(timedTaskId, finalTaskGroup);
                                                if (sysTimedTaskService.addTimedTask(
                                                        timedTaskId, finalTaskGroup, finalTimedTask.getString("cron_expression"), finalTimedTask.getString("func_path"),
                                                        "1", finalTimedTask.getString("misfire_policy"), finalTimedTask.getString("concurrent_execute"),
                                                        finalTimedTask.getString("is_log")
                                                )) {
                                                    return false;
                                                } else {
                                                    return true;
                                                }
                                            }
                                    ) == -1
                            ) {
                                return JSONMap.error("创建任务失败失败");
                            } else {
                                return JSONMap.success("创建任务成功");
                            }
                    }
                case "Pause":
                    if(!ApiGlobalInterceptor.permission("lspk:ls:timedTask:pause")) {
                        Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                        return null;
                    }

                    if(
                            DB.updateTransaction(
                                    "update sys_timed_task set status = '2' where timed_task_id = '" + DB.e(timedTaskId) + "'",
                                    () -> !sysTimedTaskService.pauseJob(timedTaskId, finalTaskGroup)
                            ) == -1
                    ) {
                        return JSONMap.error("暂停任务失败");
                    } else {
                        return JSONMap.success("暂停任务成功");
                    }
                case "Stop":
                    if(!ApiGlobalInterceptor.permission("lspk:ls:timedTask:stop")) {
                        Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                        return null;
                    }

                    if(
                            DB.updateTransaction(
                                    "update sys_timed_task set status = '3' where timed_task_id = '" + DB.e(timedTaskId) + "'",
                                    () -> !sysTimedTaskService.deleteTimedTask(timedTaskId, finalTaskGroup)
                            ) == -1
                    ) {
                        return JSONMap.error("终止任务失败");
                    } else {
                        return JSONMap.success("终止任务成功");
                    }
                default:
                    return JSONMap.error("修改类型有误");
            }
        } finally {
            lock.unlock();
        }
        return JSONMap.error("操作失败");
    }

    /**
     * 获取定时任务日志列表
     */
    @GetMapping("/api/sysTimedTaskLog/getSysTimedTaskLogList")
    @LoginToken(validBackend = true, permissionKey = "lspk:ls:timedTaskLog:list")
    public JSONMap getSysTimedTaskLogList() {
        String timedTaskLogId = Http.param("timed_task_log_id");
        String funcPath = Http.param("func_path");
        String status = Http.param("status");
        String type = Http.param("type");
        String sortField = Http.param("SortField");
        String sortOrder = Http.param("SortOrder");
        String sql = "" +
                "select a.timed_task_log_id,a.timed_task_id,b.task_desc,b.task_group,a.func_path,a.start_time,a.end_time,a.time,a.status,a.type,a.result " +
                "from sys_timed_task_log a " +
                "   left join sys_timed_task b on a.timed_task_id = b.timed_task_id ";
        Where.Operator relationship = Where.Operator.LIKE;

        if("1".equals(Http.param("IsEqual","0"))) {
            relationship = Where.Operator.EQ;
        }

        sql += new Where(true)
                .or().add("a.timed_task_log_id", timedTaskLogId, Where.Operator.EQ)
                .or().add("a.func_path", funcPath, relationship)
                .or().add("a.status", status, relationship)
                .or().add("a.type", type, relationship)
                .prependWhere().toString();

        //排序字段
        switch(sortField) {
            case "timed_task_log_id":
            case "start_time":
            case "end_time":
            case "time":
            case "status":
            case "type":
                if(sortOrder.equals("asc")) sql += " order by a." + sortField + " asc ";
                else if(sortOrder.equals("desc")) sql += " order by a." + sortField+" desc ";
                break;
            default:
                sql += " order by a.timed_task_log_id desc ";
        }
        return DB.sqlToJSONMap(sql,Http.param("PageNo"),Http.param("PageCount"),"100");
    }

    /**
     * 修改定时任务日志
     */
    @PostMapping("/api/sysTimedTaskLog/updateSysTimedTaskLog")
    @LoginToken(validBackend = true, permissionKey = "lspk:ls:timedTaskLog:delete")
    public JSONMap updateSysTimedTaskLog() {
        String updateType = Http.param("UpdateType");
        String timedTaskLogId = Http.param("timed_task_log_id");

        switch(updateType) {
            case "Delete":
                if(timedTaskLogId.isEmpty()) {
                    return JSONMap.error("定时任务代码不能为空");
                }

                if(DB.update("delete from sys_timed_task_log where timed_task_log_id = '"+DB.e(timedTaskLogId)+"'") > 0) {
                    return JSONMap.success();
                }
                break;
            case "BatchDelete":
                String timedTaskLogIds = Http.param("timed_task_log_id_arr");

                String[] timedTaskLogIdArr = timedTaskLogIds.split(",");

                if (timedTaskLogIdArr.length < 1) {
                    return JSONMap.error("请选择至少一个任务日志");
                }

                String timedTaskLogIdArrStr = "";

                for(int i = 0;i < timedTaskLogIdArr.length;i++) {
                    try {
                        if(i == timedTaskLogIdArr.length - 1) {
                            timedTaskLogIdArrStr += "'"+DB.e(String.valueOf(Integer.parseInt(timedTaskLogIdArr[i])))+"'";
                        } else {
                            timedTaskLogIdArrStr += "'"+DB.e(String.valueOf(Integer.parseInt(timedTaskLogIdArr[i])))+"',";
                        }
                    } catch(Exception e) {
                        return JSONMap.error("任务代码格式有误");
                    }
                }

                if(DB.update("delete from sys_timed_task_log where timed_task_log_id in ("+timedTaskLogIdArrStr+")") != -1) {
                    return JSONMap.success();
                }
                break;
            case "Clear":
                if (DB.update("truncate table sys_timed_task_log") != -1) {
                    return JSONMap.success();
                }
                break;
            default:
                return JSONMap.error("修改类型有误");
        }
        return JSONMap.error("操作失败");
    }

}
