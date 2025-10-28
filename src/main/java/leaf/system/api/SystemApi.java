package leaf.system.api;

import jakarta.servlet.http.Part;
import leaf.system.common.Http;
import leaf.system.common.SysCommon;
import leaf.common.IO;
import leaf.common.Log;
import leaf.common.object.Cache;
import leaf.common.object.JSONMap;
import leaf.common.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Random;

/**
 * 系统模块
 */
@RestController
public class SystemApi {
    @Autowired
    private Environment environment;

    /**
     * 上传
     */
    @PostMapping("/system/api/upload")
    public JSONMap upload() {
        Part file = Http.part("File");
        String isOldName = Http.param("IsOldName","0");

        if(file == null) {
            return JSONMap.error("请选择需要上传的文件");
        }

        String filename = file.getSubmittedFileName();

        System.out.println(filename);

        if(filename == null) {
            return JSONMap.error("请选择需要上传的文件");
        }

        //获取允许上传文件大小和允许上传文件的后缀
        JSONMap systemConfig = SysCommon.getSystemConfigs("AllowUploadFileSize", "AllowUploadFileSuffix");
        BigDecimal size = StrUtil.eval(systemConfig.getString("AllowUploadFileSize"));
        String suffix = systemConfig.getString("AllowUploadFileSuffix");

        if(size != null && size.compareTo(BigDecimal.valueOf(-1)) != 0) {
            if(size.compareTo(new BigDecimal(file.getSize())) == -1) {
                return JSONMap.error("上传文件大小超出指定范围");
            }
        }

        if(suffix != null && !".".equals(suffix)) {
            String[] suffixs = suffix.split(",");
            boolean flag = false;

            for(String suf:suffixs) {
                if(suf.equals(IO.getSuffix(filename))) flag = true;
            }

            if(!flag) {
                return JSONMap.error("服务器不允许上传该类型的文件");
            }
        }

        String filepath = "upload/system/"+DateTime.now("yyyyMMdd")+"/";

        if("1".equals(isOldName)) {
            filepath += IO.getSuffix(filename)+DateTime.now("-HH_mm_ss_SSS-")+Rdm.num(8)+"/"+filename;
        } else {
            filepath += IO.getSuffix(filename)+DateTime.now("-HH_mm_ss_SSS-")+Rdm.num(8)+"."+IO.getSuffix(filename);
        }

        String path = System.getProperty("user.dir")+environment.getProperty("leaf.resource","/resources/")+filepath;

        try {
            IO.createFile(new File(path));
            file.write(path);
        } catch (IOException e) {
            Log.write("Error_system",Log.getException(e));
            return JSONMap.error("上传失败");
        }

        return JSONMap.success("/"+filepath);
    }
    /**
     * 获取验证码
     */
    @GetMapping("/system/api/getValidCode")
    public JSONMap getValidCode() {
        String length = Http.param("Length", "4");
        int _length;

        try {
            _length = Integer.parseInt(length);
        } catch(Exception e) {
            return JSONMap.error("长度必须为整数");
        }

        JSONMap validCode = ValidRobot.getValidCode(_length);
        String validParam = "lyfsys_validCode_"+validCode.get("text") + "_"+DateTime.now("yyyyMMddHHmmssSSS") + "_"+Rdm.str(10, StrUtil.Char);
        long duration = 180;

        try {
            duration = Long.parseLong(""+environment.getProperty("leaf.validCode.duration", "180"));
        } catch(Exception ignored) {}

        if(duration == -1) {
            Cache.put(validParam,validCode.get("text"));
        } else {
            Cache.put(validParam,validCode.get("text"),duration);
        }

        validCode.remove("text");
        validCode.put("valid_param",Lock.aesEncrypt(validParam));
        return JSONMap.success(validCode);
    }
    /**
     * 检查验证码
     */
    @GetMapping("/system/api/checkValidCode")
    public JSONMap validPuzzleCode() {
        String validParam = Http.param("ValidParam");
        String text = Http.param("Text");
        int result = SysCommon.checkValidCode(validParam,text);

        switch(result) {
            case 1:
                return JSONMap.success();
            case 2:
                return JSONMap.error("验证失败");
            case 3:
                return JSONMap.error("验证超时，请刷新验证码后重新验证");
        }

        return JSONMap.error("验证失败");
    }
    /**
     * 获取拼图验证
     */
    @GetMapping("/system/api/getValidPuzzle")
    public JSONMap getPuzzleValid() {
        String puzzleValidImgPath = System.getProperty("user.dir")+environment.getProperty("leaf.validPuzzle.path","/system/puzzleImg/");
        File file = new File(puzzleValidImgPath);
        String[] list = file.list();

        if(!file.exists() || list == null || list.length < 1) {
            return JSONMap.error("获取验证拼图图片失败");
        }

        Random rdm = new Random();
        JSONMap puzzleValid = ValidRobot.getValidPuzzle(puzzleValidImgPath + (rdm.nextInt(list.length) + 1) + ".png");

        if(puzzleValid == null) {
            return JSONMap.error("获取验证拼图图片失败");
        }

        String validParam = "lyfsys_validPuzzle_"+puzzleValid.get("x")+"_"+DateTime.now("yyyyMMddHHmmssSSS")+"_"+ Rdm.str(10, StrUtil.Char);
        long duration = 120;

        try {
            duration = Long.parseLong(""+environment.getProperty("leaf.validPuzzle.duration", "120"));
        } catch(Exception ignored) {}

        if(duration == -1) {
            Cache.put(validParam,puzzleValid.get("x"));
        } else {
            Cache.put(validParam,puzzleValid.get("x"),duration);
        }

        puzzleValid.remove("x");
        puzzleValid.put("valid_param",Lock.aesEncrypt(validParam));
        return JSONMap.success(puzzleValid);
    }
    /**
     * 检查验证拼图
     */
    @GetMapping("/system/api/checkValidPuzzle")
    public JSONMap validPuzzleValid() {
        String validParam = Http.param("ValidParam");
        String x = Http.param("X");
        int _x = 0;

        try {
            _x = Integer.parseInt(x);
        } catch(Exception e) {
            return JSONMap.error("验证失败");
        }

        Log.write("12", environment.getProperty("leaf.validPuzzle.range"));
        int result = SysCommon.checkValidPuzzle(validParam, _x, environment.getProperty("leaf.validPuzzle.range"));

        switch(result) {
            case 1:
                return JSONMap.success();
            case 2:
                return JSONMap.error("验证失败");
            case 3:
                return JSONMap.error("验证超时，请刷新验证码后重新验证");
        }

        return JSONMap.error("验证失败");
    }
}
