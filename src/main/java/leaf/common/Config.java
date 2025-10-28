package leaf.common;

import leaf.common.util.DateTime;

/**
 * 配置类
 */
public class Config {
//    public static final java.util.Properties Properties = new Properties();
    public static String ConfigPath = System.getProperty("user.dir");

//    /**
//     * 设置配置路径
//     * @param configPath 配置路径 例如：/LeafConfig.properties
//     */
//    public static void setConfigPath(String configPath) {
//        try {
//            InputStream in = new FileInputStream(System.getProperty("user.dir") + configPath);
//            Properties.load(in);//读取配置文件的信息
//            ConfigPath = configPath;
//            String log = Properties.getProperty("common.log");
//            if(!Valid.isEmpty(log))
//                Log.logPath = System.getProperty("user.dir") + log;
//            System.out.println(Log.success("leaf.common 配置成功"));
//        } catch (IOException e) {
//            Log.write("Error_config","=================== Error ===================\n" +
//                    "Time:"+ DateTime.now("yyyy-MM-dd HH:mm:ss")+"\nMessage:读取配置文件失败！ - "+configPath);
//        }
//    }

    /**
     * 字符串解析成Integer
     * @param value 需要解析的字符串
     * @param propertyName 属性名
     * @return 解析后的int类型
     */
    public static int parseInteger(String value, String propertyName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.write("Error_config", "=================== Error ===================\n" +
                    "Time:" + DateTime.now("yyyy-MM-dd HH:mm:ss") + "\nMessage:string转int失败！ - " + propertyName);
            return 0;
        }
    }

    /**
     * 作者名
     * @return 作者名
     */
    public static String authorName() {
        return String.format("\033[%d;%dm%s\033[0m",34,1,""+
                ".__           _____ \n"+
                "|  | ___.__._/ ____\\ \n"+
                "|  |<   |  |\\   __\\ \n"+
                "|  |_\\___  | |  | \n"+
                "|____/ ____| |__| \n"+
                "     \\/");
    }

    /**
     * 作者
     * @return 作者
     */
    public static String author() {
        return "" +
                ".__                 _____ \n" +
                "|  |   ____ _____ _/ ____\\\n" +
                "|  | _/ __ \\\\__  \\\\   __\\ \n" +
                "|  |_\\  ___/ / __ \\|  |   \n" +
                "|____/\\___  >____  /__|   \n" +
                "          \\/     \\/";
    }
}
