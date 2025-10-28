package leaf.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具类
 */
public class DateTime {
//    public static final int YEAR = 1;
//    public static final int Month = 2;
//    public static final int Day = 5;
//    public static final int Hour = 10;
//    public static final int Minute = 12;
//    public static final int Second = 13;
//    public static final int MilliSecond = 14;
//    public static final int Week = 7;

    /**
     * 获取当前时间 例如：2023-08-06 星期日 17:21:46:036
     * @return 当前时间
     */
    public static String now() {
        return new SimpleDateFormat("yyyy-MM-dd EEE HH:mm:ss:SSS").format(new Date());
    }

    /**
     * 获取当前时间
     * @param formatStr 字符串格式
     * @return 当前时间
     */
    public static String now(String formatStr) {
        return new SimpleDateFormat(formatStr).format(new Date());
    }

    /**
     * Date 转时间字符串
     * @param date Date
     * @param formatStr 字符串格式
     * @return 转换后的时间字符串
     */
    public static String dateToStr(Date date, String formatStr) {
        return new SimpleDateFormat(formatStr).format(date);
    }

    /**
     * 时间字符串转 Date
     * @param str 时间字符串
     * @param formatStr 字符串格式
     * @return date
     */
    public static Date strToDate(String str, String formatStr) {
        try {
            return new SimpleDateFormat(formatStr).parse(str);
        } catch(ParseException e) {
            return null;
        }
    }

    /**
     * 时间戳转 Date
     * @param timeStamp 时间戳
     * @return 转换后的 Data
     */
    public static Date timeStampToData(long timeStamp) {
        return new Date(timeStamp);
    }

    /**
     * Data 转时间戳
     * @param date Date
     * @return 转换后的时间戳
     */
    public static long dataToTimeStamp(Date date) {
        if (date == null) {
            return 0;
        }
        return date.getTime();
    }

    /**
     * 验证日期
     * @param dateTimeStr 日期字符串
     * @param format 字符串格式
     * @return true是日期
     */
    public static boolean valid(String dateTimeStr, String format) {
        return strToDate(dateTimeStr, format) != null;
    }
//    /**
//     * 获取当前年号 例如2023
//     * @return int
//     */
//    public static int year() {
//        Calendar calendar = Calendar.getInstance();
//        return calendar.get(Calendar.YEAR);
//    }
//    /**
//     * 获取指定日期年号 例如2023
//     * @param date 日期
//     * @return int
//     */
//    public static int year(Date date) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        return calendar.get(Calendar.YEAR);
//    }
//    /**
//     * 获取当前月号 例如9
//     * @return int
//     */
//    public static int month() {
//        Calendar calendar = Calendar.getInstance();
//        return calendar.get(Calendar.MONTH) + 1;
//    }
//    /**
//     * 获取指定日期月号 例如9
//     * @param date 日期
//     * @return int
//     */
//    public static int month(Date date) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        return calendar.get(Calendar.MONTH) + 1;
//    }
//    /**
//     * 获取当前天号 例如12
//     * @return int
//     */
//    public static int day() {
//        Calendar calendar = Calendar.getInstance();
//        return calendar.get(Calendar.DATE);
//    }
//    /**
//     * 获取指定日期天号 例如12
//     * @param date 日期
//     * @return int
//     */
//    public static int day(Date date) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        return calendar.get(Calendar.DATE);
//    }
//    /**
//     * 获取当前小时数 例如14
//     * @return int
//     */
//    public static int hour() {
//        Calendar calendar = Calendar.getInstance();
//        return calendar.get(Calendar.HOUR);
//    }
//    /**
//     * 获取指定日期小时数 例如14
//     * @param date 日期
//     * @return int
//     */
//    public static int hour(Date date) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        return calendar.get(Calendar.HOUR);
//    }
//    /**
//     * 获取当前分钟数 例如20
//     * @return int
//     */
//    public static int minute() {
//        Calendar calendar = Calendar.getInstance();
//        return calendar.get(Calendar.MINUTE);
//    }
//    /**
//     * 获取指定日期分钟数 例如20
//     * @param date 日期
//     * @return int
//     */
//    public static int minute(Date date) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        return calendar.get(Calendar.MINUTE);
//    }
//    /**
//     * 获取当前秒数 例如45
//     * @return int
//     */
//    public static int second() {
//        Calendar calendar = Calendar.getInstance();
//        return calendar.get(Calendar.SECOND);
//    }
//    /**
//     * 获取指定日期秒数 例如45
//     * @param date 日期
//     * @return int
//     */
//    public static int second(Date date) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        return calendar.get(Calendar.SECOND);
//    }
//    /**
//     * 获取当前毫秒数 例如391
//     * @return int
//     */
//    public static int millisecond() {
//        Calendar calendar = Calendar.getInstance();
//        return calendar.get(Calendar.MILLISECOND);
//    }
//    /**
//     * 获取指定日期毫秒数 例如391
//     * @param date 日期
//     * @return int
//     */
//    public static int millisecond(Date date) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        return calendar.get(Calendar.MILLISECOND);
//    }
//    /**
//     * 获取当前星期数 例如2
//     * @return int
//     */
//    public static int week() {
//        Calendar calendar = Calendar.getInstance();
//        int n = calendar.get(Calendar.DAY_OF_WEEK);
//        if(n == 1) n = 7; else n --;
//        return n;
//    }
//    /**
//     * 获取指定日期星期数 例如2
//     * @param date 日期
//     * @return int
//     */
//    public static int week(Date date) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        int n = calendar.get(Calendar.DAY_OF_WEEK);
//        if(n == 1) n = 7; else n --;
//        return n;
//    }
//    /**
//     * 获取1年前的年号
//     * @return int
//     */
//    public static int yearOneYearAgo() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date());
//        calendar.add(Calendar.YEAR,-1);
//        return calendar.get(Calendar.YEAR);
//    }
//    /**
//     * 获取1年后的年号
//     * @return int
//     */
//    public static int yearOneYearLater() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date());
//        calendar.add(Calendar.YEAR,1);
//        return calendar.get(Calendar.YEAR);
//    }
//    /**
//     * 获取1年前的年月号
//     * @return int
//     */
//    public static int monthOneYearAgo() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date());
//        calendar.add(Calendar.YEAR,-1);
//        return calendar.get(Calendar.MONTH) + 1;
//    }
//    /**
//     * 获取1年前的年天号
//     * @return int
//     */
//    public static int dayOneYearAgo() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date());
//        calendar.add(Calendar.YEAR,-1);
//        return calendar.get(Calendar.DAY_OF_MONTH);
//    }
//    /**
//     * 获取本月天数
//     * @return int
//     */
//    public static int getCurrentMonthDayNumber() {
//        Calendar calendar = Calendar.getInstance();
//        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
//    }
}
