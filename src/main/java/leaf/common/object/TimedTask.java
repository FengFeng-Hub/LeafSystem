package leaf.common.object;

import leaf.common.Clazz;
import leaf.common.Log;
import leaf.common.util.StrUtil;
import leaf.common.util.Valid;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 定时任务
 * 依赖 quartz-x.x.x.jar slf4j-1.1.1.jar
 */
public class TimedTask {

//    /**
//     * 删除定时任务
//     * @param taskGroup 任务分组
//     * @param taskName 任务名称
//     */
//    public static void deleteTimedTask(String taskName,String taskGroup) {
//        try {
//            TimedTask.createSchedulerFactory().getScheduler().deleteJob(new JobKey(taskName,taskGroup));
//        } catch (SchedulerException e) {
//            Log.write("Error",Log.getException(e));
//        }
//    }




}
