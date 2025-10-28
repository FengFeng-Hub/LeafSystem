package leaf.system.service;

import jakarta.annotation.PostConstruct;
import leaf.common.Clazz;
import leaf.common.DB;
import leaf.common.Log;
import leaf.common.object.JSONList;
import leaf.common.object.JSONMap;
import leaf.common.util.DateTime;
import leaf.common.util.StrUtil;
import leaf.common.util.Valid;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Service
@DependsOn("systemConfig")
public class SysTimedTaskService {
    @Autowired
    private Scheduler scheduler;

    /**
     * 项目启动时，初始化定时器
     * 主要是防止手动修改数据库导致未同步到定时任务处理（注：不能手动修改数据库ID和任务组名，否则会导致脏数据）
     */
    @PostConstruct
    public void init() throws SchedulerException {
        scheduler.clear();
        scheduler.start();
        // 获取定时任务列表
        JSONList timedTaskList = DB.query("" +
                "select timed_task_id,task_desc,task_group,func_path,cron_expression,status,misfire_policy," +
                "   concurrent_execute,is_log " +
                "from sys_timed_task");

        for (Object timedTask : timedTaskList) {
            JSONMap task = (JSONMap) timedTask;
            addTimedTask(
                    task.getString("timed_task_id"), task.getString("task_group"), task.getString("cron_expression"), task.getString("func_path"),
                    task.getString("status"), task.getString("misfire_policy"), task.getString("concurrent_execute"), task.getString("is_log")
            );
        }

        System.out.println(Log.info("初始化定时任务"));
    }

    /**
     * 添加定时任务
     * @param timedTaskId 定时任务代码
     * @param taskGroup 任务分组
     * @param cron cron表达式
     * @param methodPath 方法路径
     * @param status 状态
     * @param misfirePolicy 执行策略
     * @param concurrentExecute 并发执行
     * @param isLog 是否记录日志
     * @return true添加成功
     */
    public boolean addTimedTask(String timedTaskId, String taskGroup,String cron,String methodPath,
                                      String status, String misfirePolicy, String concurrentExecute, String isLog) {
        if ("3".equals(status)) {
            return true;
        }
        try {
//            scheduler.start();
            // 任务名
            JobKey jobKey = JobKey.jobKey(timedTaskId, taskGroup);

            // 是否允许并发执行
            Class<? extends Job> jobClass = "1".equals(concurrentExecute)? TimedTaskJob.class: TimedTaskJobDisallowConcurrent.class;

            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobKey)
                    // 放入参数，运行时的方法可以获取
                    .usingJobData("timedTaskId", timedTaskId)
                    .usingJobData("methodPath", methodPath)
                    .usingJobData("isLog", isLog)
                    .build();

            // 表达式调度构建器
            CronScheduleBuilder cb = CronScheduleBuilder.cronSchedule(cron);

            // 设置执行策略
            switch (misfirePolicy) {
                // 默认
                case "1":
                    break;
                // 行为：调度器会立即执行所有错过的触发，然后恢复正常调度
                // 适用场景：需要确保每个触发都执行，不计较时间延迟
                case "2":
                    cb = cb.withMisfireHandlingInstructionIgnoreMisfires();
                    break;
                // 行为：立即执行一次（针对最近一次错过），然后恢复正常调度
                // 适用场景：平衡实时性和系统负载，只补偿最近一次的错过
                case "3":
                    cb = cb.withMisfireHandlingInstructionFireAndProceed();
                    break;
                // 行为：错过触发就被忽略，只等待下一次正常的触发时间
                // 适用场景：对实时性要求不高，可以接受错过执行的任务
                case "4":
                    cb = cb.withMisfireHandlingInstructionDoNothing();
                    break;
            }

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(timedTaskId,taskGroup)
                    .withSchedule(cb)
                    .build();

            // 判断是否存在
            if (scheduler.checkExists(jobKey)) {
                // 防止创建时存在数据问题 先移除，然后在执行创建操作
                scheduler.deleteJob(jobKey);
            }

            // 调度任务
            scheduler.scheduleJob(jobDetail, trigger);

            // 是否是暂停状态
            if ("2".equals(status)) {
                scheduler.pauseJob(jobKey);
            }
            return true;
        } catch (SchedulerException e) {
            Log.write("Error_TimedTask",Log.getException(e));
            return false;
        }
    }

    /**
     * 删除定时任务
     * @param timedTaskId 定时任务代码
     * @return true删除成功
     */
    public boolean deleteTimedTask(String timedTaskId, String taskGroup) {
        try {
            return scheduler.deleteJob(JobKey.jobKey(timedTaskId, taskGroup));
        } catch (SchedulerException e) {
            Log.write("Error_TimedTask",Log.getException(e));
           return false;
        }
    }

    /**
     * 恢复任务
     * @param timedTaskId 定时任务代码
     * @param taskGroup 任务分组
     * @return true恢复成功
     */
    public boolean resumeJob(String timedTaskId, String taskGroup) {
        try {
            scheduler.resumeJob(JobKey.jobKey(timedTaskId, taskGroup));
            return true;
        } catch (SchedulerException e) {
            Log.write("Error_TimedTask",Log.getException(e));
            return false;
        }
    }

    /**
     * 暂停任务
     * @param timedTaskId 定时任务代码
     * @param taskGroup 任务分组
     * @return true暂停成功
     */
    public boolean pauseJob(String timedTaskId, String taskGroup) {
        try {
            scheduler.pauseJob(JobKey.jobKey(timedTaskId, taskGroup));
            return true;
        } catch (SchedulerException e) {
            Log.write("Error_TimedTask",Log.getException(e));
            return false;
        }
    }

    /**
     * 获取定时任务分组
     * @return 定时任务分组，JSONList 类型
     */
    public JSONList getTimedTaskGroup() {
        try {
            JSONList result = new JSONList();
            for (String groupName : scheduler.getJobGroupNames()) result.add(groupName);
            return result;
        } catch (SchedulerException e) {
            Log.write("Error",Log.getException(e));
            return null;
        }
    }

    /**
     * 获取定时任务
     * @return JSONList 类型
     * [{
     *      "taskName":"e3be9750d0774c158cf187c7850cc523",//任务名称
     *      "taskGroup":"TaskTestTemp",//任务分组
     *      "cron":"0/5 * * * * ?",//cron表达式
     *      "methodPath":"lyf.common.test.PdmTest.t",//方法路径
     * }]
     */
    public JSONList getTimedTask() {
        return getTimedTask(null);
    }

    /**
     * 获取指定任务分组下的定时任务（任务分组为 null 时获取所有任务分组的定时任务）
     * @param taskGroup 任务分组
     * @return JSONList
     * [{
     *      "taskName":"e3be9750d0774c158cf187c7850cc523",//任务名称
     *      "taskGroup":"TaskTestTemp",//任务分组
     *      "cron":"0/5 * * * * ?",//cron表达式
     *      "methodPath":"lyf.common.test.PdmTest.t('123')"//方法路径
     * }]
     */
    public JSONList getTimedTask(String taskGroup) {
        try {
            JSONList result = new JSONList();
            JSONMap object;
            String taskName = "";
            JobDataMap dataMap;

            for (String groupName : scheduler.getJobGroupNames()) {
                if(taskGroup != null && !taskGroup.equals(groupName)) continue;

                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    object = new JSONMap();
                    result.add(object);
                    taskName = jobKey.getName();
                    object.put("taskName",taskName);
                    object.put("taskGroup",groupName);

                    Trigger trigger = scheduler.getTrigger(new TriggerKey(taskName,groupName));

                    if (trigger instanceof CronTrigger) {
                        object.put("cron",((CronTrigger) trigger).getCronExpression());
                    } else {
                        object.put("cron","该触发器不是CronTrigger类型");
                    }

                    // 查看参数
                    dataMap = scheduler.getJobDetail(JobKey.jobKey(taskName,groupName)).getJobDataMap();
                    object.put("methodPath",dataMap.get("methodPath"));
                }
            }
            return result;
        } catch (SchedulerException e) {
            Log.write("Error",Log.getException(e));
            return null;
        }
    }

    /**
     * 获取最近几次运行时间
     * @param cron cron表达式
     * @param num 次数
     * @return 最近几次运行时间，JSONList 类型
     */
    public static JSONList getNextExecTime(String cron,int num) {
        JSONList list = new JSONList();
        CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
        try {
            cronTriggerImpl.setCronExpression(cron);//这个是重点，一行代码搞定
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<Date> dates = TriggerUtils.computeFireTimes(cronTriggerImpl, null, num);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Date date : dates) {
            list.add(dateFormat.format(date));
        }
        return list;
    }

    /**
     * 定时任务处理（允许并发执行）
     */
    public static class TimedTaskJob implements Job {
        /**
         * 执行任务
         * @param context JobExecutionContext 对象
         */
        public void execute(JobExecutionContext context) {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            executeTimedTask(dataMap.getString("timedTaskId"), dataMap.getString("methodPath"), dataMap.getString("isLog"), true);
        }
    }

    /**
     * 定时任务处理（禁止并发执行）
     */
    @DisallowConcurrentExecution
    public static class TimedTaskJobDisallowConcurrent implements Job {
        /**
         * 执行任务
         * @param context JobExecutionContext 对象
         */
        public void execute(JobExecutionContext context) {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            executeTimedTask(dataMap.getString("timedTaskId"), dataMap.getString("methodPath"), dataMap.getString("isLog"), true);
        }
    }

    /**
     * 执行定时任务
     * @param timedTaskId 定时任务代码
     * @param methodPath 函数路径
     * @param isLog 是否记录日志
     * @param isAuto 是否自动触发
     * @return true执行成功
     */
    public static boolean executeTimedTask(String timedTaskId, String methodPath, String isLog, boolean isAuto) {
        // 开始时间
        Date start = new Date();
        String startTime = DateTime.dateToStr(start, "yyyy-MM-dd HH:mm:ss");

        final String a = "(";
        String[] split = methodPath.split("\\(");

        // 结果
        String result = "";
        String status = "2";

        try {
            Object successResult = Clazz.invokeStaticByMethodPath(split.length > 0 ? split[0] : methodPath, getMethodParams(methodPath));
            status = "1";

            if (successResult != null) {
                result = String.valueOf(successResult);
            }
        } catch (Exception e) {
            result = e.getMessage();
        }

        // 结束时间
        Date end = new Date();
        String endTime = DateTime.dateToStr(end, "yyyy-MM-dd HH:mm:ss");
        // 相差毫秒数
        Long time = end.getTime() - start.getTime();

        if ("1".equals(isLog)) {
            DB.update("" +
                    "insert into sys_timed_task_log(timed_task_id,func_path,start_time,end_time,time,status,type,result) value" +
                    "('" + timedTaskId + "', '" + DB.e(methodPath) + "', '" + startTime + "', '" + endTime + "', '" + time + "'," +
                    "   '" + status + "', '" + (isAuto?"1":"2") + "', '" + DB.e(result) + "')");
        }

        return status.equals("1");
    }

    /**
     * 获取method方法参数相关列表
     * @param invokeTarget 目标字符串
     * @return method方法相关参数列表
     */
    private static Object[] getMethodParams(String invokeTarget) {
        String methodStr = StrUtil.substring(invokeTarget, "(", ")");
        if (Valid.isEmpty(methodStr)) {
            return null;
        }
        String[] methodParams = methodStr.split(",(?=([^\"']*[\"'][^\"']*[\"'])*[^\"']*$)");
        List<Object[]> classs = new LinkedList<>();
        for (int i = 0; i < methodParams.length; i++) {
            String str = methodParams[i] == null ? "" : methodParams[i].trim();
            // String字符串类型，以'或"开头
            if (str.startsWith("'") || str.startsWith("\"")) {
                classs.add(new Object[] { str.substring(1, str.length() - 1), String.class });
            } else if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {  // boolean布尔类型，等于true或者false
                classs.add(new Object[] { Boolean.valueOf(str), Boolean.class });
            } else if (str.endsWith("L")) {  // long长整形，以L结尾
                classs.add(new Object[] { Long.valueOf(str.substring(0, str.length() - 1)), Long.class });
            } else if (str.endsWith("D")) {  // double浮点类型，以D结尾
                classs.add(new Object[] { Double.valueOf(str.substring(0, str.length() - 1)), Double.class });
            } else {  // 其他类型归类为整形
                classs.add(new Object[] { Integer.valueOf(str), Integer.class });
            }
        }
        return getMethodParamsValue(classs);
    }

    /**
     * 获取参数值
     * @param methodParams 参数相关列表
     * @return 参数值列表
     */
    private static Object[] getMethodParamsValue(List<Object[]> methodParams)
    {
        Object[] classs = new Object[methodParams.size()];
        int index = 0;
        for (Object[] os : methodParams)
        {
            classs[index] = (Object) os[0];
            index++;
        }
        return classs;
    }
}
