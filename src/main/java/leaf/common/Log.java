package leaf.common;

import com.alibaba.druid.sql.SQLUtils;
import leaf.common.util.DateTime;

import java.io.*;
import java.sql.SQLException;

/**
 * 日志操作类
 */
public class Log {
    /**
     * 日志路径
     */
    public static String logPath = System.getProperty("user.dir");

    /**
     * 日志内容
     * @param title 标题
     * @param text 自定义文本
     * @return 格式化后的日志字符串
     */
    public static String content(String title,String text) {
        return "[ "+title+" - "+ DateTime.now("yyyy-MM-dd HH:mm:ss")+" ================================================== ]\n" +
                text+"\n";
    }

    /**
     * 获取异常
     * @param e Exception
     * @return 格式化后的日志字符串
     */
    public static String getException(Exception e) {
        return "[ ERROR - "+DateTime.now("yyyy-MM-dd HH:mm:ss")+" ================================================== ]\n" +
                "ErrorMsg:"+e.getMessage()+"\n------\n" +
                getExceptionSource(e) + "\n";
    }

    /**
     * 获取异常
     * @param e Exception
     * @param text 自定义文本
     * @return 格式化后的日志字符串
     */
    public static String getException(Exception e,String text) {
        return "[ ERROR - "+DateTime.now("yyyy-MM-dd HH:mm:ss")+" ================================================== ]\n" +
                "ErrorMsg:"+e.getMessage()+"\n------\n"+text+"\n" +
                getExceptionSource(e) + "\n";
    }

    /**
     * 获取SQL异常
     * @param e Exception
     * @return 格式化后的日志字符串
     */
    public static String getSQLException(SQLException e) {
        return "[ ERROR - "+DateTime.now("yyyy-MM-dd HH:mm:ss")+" ================================================== ]\n" +
                "ErrorMsg:"+e.getMessage()+"\n------\n" +
                "SQL:"+e.getErrorCode()+"\n------\n" +
                getExceptionSource(e) + "\n";
    }

    /**
     * 获取SQL异常
     * @param e Exception
     * @param sql SQL语句
     * @return 格式化后的日志字符串
     */
    public static String getSQLException(SQLException e,String sql) {
        if(e.getErrorCode() != 1064) {
            sql = SQLUtils.formatMySql(sql);//格式化SQL语句
        }

        return "[ ERROR - "+DateTime.now("yyyy-MM-dd HH:mm:ss")+" ================================================== ]\n" +
                "ErrorMsg:"+e.getMessage()+"\n------\n" +
                "SQL:"+e.getErrorCode()+"\n"+sql+"\n------\n" +
                getExceptionSource(e) + "\n";
    }

    /**
     * 写日志
     * @param filename 文件名称
     * @param msg 信息
     */
    public static void write(String filename,String msg) {
        FileWriter fw = null;
        try {
            File logFile = new File(logPath + "/log/" + DateTime.now("yyyy") + "/" +
                    DateTime.now("MM") + "/" + DateTime.now("dd"),filename + ".log");

            if(!logFile.getParentFile().exists()) {
                logFile.getParentFile().mkdirs();
            }

            if(!logFile.exists()) {
                logFile.createNewFile();
            }

            fw = new FileWriter(logFile,true);
            fw.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fw != null) {
                try {
                    fw.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取异常来源
     * @param e Exception
     * @return 异常来源
     */
    private static String getExceptionSource(Exception e) {
        String source = "";
        Throwable cause = e;
        StackTraceElement[] stackTrace = cause.getStackTrace();

        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();

            if(className.startsWith("com.mysql") || className.contains("com.alibaba")  || className.contains("jdk.") || className.startsWith("java.")  || className.contains("javax.")
                    || className.contains("org.springframework") || className.contains("org.apache") || className.contains("sun.")) continue;

            source += "" + className + "." + element.getMethodName() + "(" + element.getFileName() + ":" + element.getLineNumber() + ")\n";
        }

        return source;
    }

    /**
     * 获取异常详细信息（已弃用）
     * @param e Exception
     * @return 格式化后的日志字符串
     */
    @Deprecated
    public static String getExceptionDetailOld(Exception e) {
        String ret = "=================== Error ===================\n" +
                "Time:"+DateTime.now("yyyy-MM-dd HH:mm:ss")+"\n------\n";
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream pout = new PrintStream(out);
            e.printStackTrace(pout);
            ret += new String(out.toByteArray());
            pout.close();
            out.close();
        } catch (Exception _e) {
            _e.printStackTrace();
        }
        return ret + "\n";
    }

    /**
     * 信息日志
     * @param content 内容
     * @return 格式化后的日志字符串
     */
    public static String info(String content) {
        return String.format("\033[%d;1m[INFO - " + DateTime.now("yyyy-MM-dd HH:mm:ss") + "] \033[0m \033[0;1m%s\033[0m",34,content);
    }

    /**
     * 成功日志
     * @param content 内容
     * @return 格式化后的日志字符串
     */
    public static String success(String content) {
        return String.format("\033[%d;1m[SUCCESS - " + DateTime.now("yyyy-MM-dd HH:mm:ss") + "]\033[0m \033[0;1m%s\033[0m",36,content);
    }

    /**
     * 失败日志
     * @param content 内容
     * @return 格式化后的日志字符串
     */
    public static String error(String content) {
        return String.format("\033[%d;1m[ERROR - " + DateTime.now("yyyy-MM-dd HH:mm:ss") + "] \033[0m \033[0;1m%s\033[0m",31,content);
    }
}
