package leaf.system.api;

import leaf.common.util.StrUtil;
import leaf.system.common.Http;
import leaf.common.Log;
import leaf.common.object.JSONList;
import leaf.common.object.JSONMap;
import leaf.system.annotate.LoginToken;
import leaf.system.interceptor.ApiGlobalInterceptor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class SysLogApi {
    /**
     * 获取日志
     */
    @GetMapping("/system/api/log/getLog")
    @LoginToken(validBackend = true,permissionKey = "lspk:ls:log:get")
    public JSONMap getLog() {
        File logDir = new File(Log.logPath + "/log/");

        //判断文件夹是否存在
        if(!logDir.exists()) {
            return JSONMap.error("获取日志失败，文件夹不存在：" + Log.logPath + "/log/");
        }

        JSONList years = new JSONList();
        JSONMap year;
        JSONList months;
        JSONMap month;
        JSONList days;
        JSONMap day;
        JSONList logs;
        JSONMap log;
        File[] yearDirs = logDir.listFiles();
        File[] monthDirs;
        File[] dayDirs;
        File[] logFiles;

        if(yearDirs == null) {
            return JSONMap.success(years);
        }

        //获取所有年文件夹
        for(int i = yearDirs.length - 1;i >= 0;i--) {
            String yearName = yearDirs[i].getName();
            year = new JSONMap();
            year.put("type","dir");
            year.put("name",yearName + "年");
            years.add(year);

            if(yearDirs[i].isDirectory()) {
                monthDirs = yearDirs[i].listFiles();

                if(monthDirs == null) continue;

                months = new JSONList();
                year.put("children",months);

                //获取所有月文件夹
                for(int j = monthDirs.length - 1;j >= 0;j--) {
                    String monthName = monthDirs[j].getName();
                    month = new JSONMap();
                    month.put("type","dir");
                    month.put("name",monthName + "月");
                    months.add(month);

                    if(monthDirs[j].isDirectory()) {
                        dayDirs = monthDirs[j].listFiles();

                        if(dayDirs == null) continue;

                        days = new JSONList();
                        month.put("children",days);

                        //获取所有日文件夹
                        for(int k = dayDirs.length - 1;k >= 0;k--) {
                            String dayName = dayDirs[k].getName();
                            day = new JSONMap();
                            day.put("type","dir");
                            day.put("name",dayName + "日");
                            days.add(day);

                            if(dayDirs[k].isDirectory()) {
                                logFiles = dayDirs[k].listFiles();

                                if(logFiles == null) continue;

                                logs = new JSONList();
                                day.put("children",logs);

                                //获取所有日志文件
                                for(File logFile:logFiles) {
                                    String logName = logFile.getName();
                                    log = new JSONMap();
                                    log.put("type","file");
                                    log.put("name", StrUtil.removeSuffix(logName,".log"));
                                    log.put("log_url","/log/" + yearName + "/" + monthName + "/" + dayName + "/" + logName);
                                    logs.add(log);
                                }
                            }
                        }
                    }
                }
            }
        }

        return JSONMap.success(years);
    }
    /**
     * 修改日志
     */
    @PostMapping("/system/api/log/updateLog")
    @LoginToken(validBackend = true)
    public JSONMap updateLog() {
        String updateType = Http.param("UpdateType");
        String logUrl = Http.param("log_url");

        if(logUrl.isEmpty()) {
            return JSONMap.error("请选择文件");
        }

        File file = new File(Log.logPath + logUrl);

        switch(updateType) {
            case "Rename":
                if(!ApiGlobalInterceptor.permission("lspk:ls:log:rename")) {
                    Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                    return null;
                }

                String newName = Http.param("new_name");

                if(newName.isEmpty()) {
                    return JSONMap.error("请填写新文件名");
                }

                String logNewUrl = getFileDirByFilePath(logUrl) + newName + ".log";

                if(file.renameTo(new File(Log.logPath + logNewUrl))) {
                    JSONMap result = JSONMap.success();
                    result.put("log_new_url",logNewUrl);
                    return result;
                }
                break;
            case "Delete":
                if(!ApiGlobalInterceptor.permission("lspk:ls:log:delete")) {
                    Http.write(403,JSONMap.error("接口执行失败，该用户没有权限"));
                    return null;
                }

                if(file.delete()) {
                    return JSONMap.success();
                }
                break;
            default:
                return JSONMap.error("修改类型有误");
        }
        return JSONMap.error("操作失败");
    }
    /**
     * 通过文件路径获取文件目录
     * @param filePath 文件路径
     * @return 文件目录
     */
    public static String getFileDirByFilePath(String filePath) {
        String[] split = filePath.split("/");
        String fileDir = "";

        if(split.length > 1) {
            for(int i = 0;i < split.length - 1;i++) {
                fileDir += split[i] + "/";
            }
        }
        return fileDir;
    }
}
