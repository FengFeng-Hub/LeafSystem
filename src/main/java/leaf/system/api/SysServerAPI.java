package leaf.system.api;

import leaf.common.object.Cache;
import leaf.common.object.JSONList;
import leaf.common.object.JSONMap;
import leaf.system.annotate.LoginToken;
import leaf.system.common.Http;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.management.OperatingSystemMXBean;

/**
 * 服务器模块
 */
@RestController
public class SysServerAPI {

    // 获取 OperatingSystemMXBean 实例
    OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    Runtime runtime = Runtime.getRuntime();

    /**
     * 获取服务器信息
     */
    @GetMapping("/system/api/server/getServerInfo")
    @LoginToken(validBackend = true,permissionKey = "lspk:ls:server:serverInfo")
    public JSONMap getServerInfo() {
        String isRepeatRequest = Http.param("IsRepeatRequest");
        JSONMap result = new JSONMap();
        JSONMap serverInfo = new JSONMap();
        JSONMap javaVMInfo = new JSONMap();
        JSONMap cpuInfo = new JSONMap();
        JSONMap memoryInfo = new JSONMap();
        JSONMap jvmMemoryInfo = new JSONMap();
        JSONList diskInfoList = new JSONList();
        JSONMap diskInfo;

        //cpu信息
        double cpuLoad = osBean.getSystemCpuLoad(); // 返回值范围 [0.0, 1.0]
//        double userCpuTime = osBean.getProcessCpuTime(); // 以纳秒为单位
        cpuInfo.put("cpu_core_num",runtime.availableProcessors());//cpu核心数
        cpuInfo.put("cpu_load_rate",String.format("%.2f",cpuLoad * 100) + " %");//cpu负载率

        //内存信息
        double totalMemory = (double)osBean.getTotalPhysicalMemorySize() / 1024 / 1024 / 1024;//GB
        double freeMemory = (double)osBean.getFreePhysicalMemorySize() / 1024 / 1024 / 1024; // 系统空闲内存
        double usedMemory = totalMemory - freeMemory; // 已用内存
        memoryInfo.put("total_memory",String.format("%.2f",totalMemory) + " GB");//总内存
        memoryInfo.put("used_memory",String.format("%.2f",usedMemory) + " GB");//已用内存
        memoryInfo.put("free_memory",String.format("%.2f",freeMemory) + " GB");//空闲内存
        memoryInfo.put("use_rate",String.format("%.2f",usedMemory / totalMemory * 100) + " %");

        //jvm内存信息
        double jvmMaxMemory = (double)runtime.maxMemory() / 1024 / 1024;//jvm最大内存
        double jvmTotalMemory = (double)runtime.totalMemory() / 1024 / 1024;//jvm总内存
        double jvmFreeMemory = (double)runtime.freeMemory() / 1024 / 1024;//jvm空闲内存
        double jvmUsedMemory = jvmTotalMemory - jvmFreeMemory;//jvm已用内存
        jvmMemoryInfo.put("max_memory",String.format("%.2f",jvmMaxMemory) + " MB");
        jvmMemoryInfo.put("total_memory",String.format("%.2f",jvmTotalMemory) + " MB");
        jvmMemoryInfo.put("used_memory",String.format("%.2f",jvmUsedMemory) + " MB");
        jvmMemoryInfo.put("free_memory",String.format("%.2f",jvmFreeMemory) + " MB");
        jvmMemoryInfo.put("use_rate",String.format("%.2f",jvmUsedMemory / jvmTotalMemory * 100) + " %");
        memoryInfo.put("jvm_memory_info",jvmMemoryInfo);

        if(!"1".equals(isRepeatRequest)) {
            //服务器信息
            InetAddress localHost = null;

            try {
                localHost = InetAddress.getLocalHost();
            } catch(UnknownHostException e) {
                return JSONMap.error("获取服务器信息失败");
            }

            serverInfo.put("server_name",localHost.getHostName());//服务名称
            serverInfo.put("server_ip",localHost.getHostAddress());//服务IP
            serverInfo.put("operate_system",System.getProperty("os.name") + " 版本：" + System.getProperty("os.version"));//操作系统
            serverInfo.put("system_architecture",System.getProperty("os.arch"));//系统架构

            //Java虚拟机信息
            javaVMInfo.put("java_name",System.getProperty("java.runtime.name"));//Java名称
            javaVMInfo.put("java_version",System.getProperty("java.version"));//Java版本
            javaVMInfo.put("java_install_path",System.getProperty("java.home"));//Java安装路径
            javaVMInfo.put("project_path",System.getProperty("user.dir"));//项目路径

            //磁盘信息
            File[] roots = File.listRoots();

            for(File file : roots) {
                diskInfo = new JSONMap();
                String path = file.getPath();
                String type = null;
                double totalSize = (double)file.getTotalSpace()/1024/1024/1024;
                double remainSize = (double)file.getFreeSpace()/1024/1024/1024;
                double usedSize = totalSize - remainSize;
                double usedPercentage = usedSize / totalSize * 100;

                try {
                    type = Files.getFileStore(Paths.get(path)).type();
                } catch(IOException e) {
                    return JSONMap.error("获取磁盘文件系统失败");
                }

                diskInfo.put("disk_path",path);//盘符路径
                diskInfo.put("file_system",type);//文件系统
                diskInfo.put("total_size",String.format("%.2f",totalSize) + "GB");//总大小
                diskInfo.put("used_size",String.format("%.2f",usedSize) + "GB");//已用大小
                diskInfo.put("remain_size",String.format("%.2f",remainSize) + "GB");//剩余大小
                diskInfo.put("used_percentage",String.format("%.2f",usedPercentage) + "%");//已用百分比
                diskInfoList.add(diskInfo);
            }
        }

        result.put("server_info",serverInfo);
        result.put("java_vm_info",javaVMInfo);
        result.put("cpu_info",cpuInfo);
        result.put("memory_info",memoryInfo);
        result.put("disk_info_list",diskInfoList);
        return JSONMap.success(result);
    }
    /**
     * 获取缓存信息
     */
    @GetMapping("/system/api/server/getCacheInfo")
    @LoginToken(validBackend = true,permissionKey = "lspk:ls:server:cacheInfo")
    public JSONMap getCacheInfo() {
        return JSONMap.success(Cache.entrySet());
    }
    /**
     * 删除缓存
     */
    @PostMapping("/system/api/server/deleteCache")
    @LoginToken(validBackend = true,permissionKey = "lspk:ls:server:deleteCache")
    public JSONMap deleteCache() {

        if("1".equals(Http.param("IsDeleteAll"))) {
            Cache.clear();
        } else {
            Cache.remove(Http.param("cache_key"));
        }
        return JSONMap.success("删除成功");
    }
}
