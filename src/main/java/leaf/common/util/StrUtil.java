package leaf.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * string处理字符串类
 */
public class StrUtil {
    /**
     * 整数部分的人民币大写
     */
    private static final String[] NUMBERS_AMOUNT = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    /**
     * 数位部分的人民币大写
     */
    private static final String[] IUNIT_AMOUNT = {"元", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "兆", "拾", "佰", "仟"};
    /**
     * 小数部分的人民币大写
     */
    private static final String[] DUNIT_AMOUNT = {"角", "分", "厘"};
    /**
     * 殊字符：整
     */
    private static final String CN_FULL = "整";
    /**
     * 特殊字符：负
     */
    private static final String CN_NEGATIVE = "负";
    /**
     * 殊字符：零元整
     */
    private static final String CN_ZEOR_FULL = "零元" + CN_FULL;
    /**
     * a~z A~Z 0~9
     */
    public static String Char = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYS1234567890";

//    private static final String[] NUMBER_CHINESE = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
//    private static final String[] UNIT_CHINESE = {"", "十", "百", "千", "万", "十", "百", "千", "亿", "十", "百", "千", "兆", "十", "百", "千"};
    /**
     * 去除指定前缀
     * @param str 需要去除前缀的字符串
     * @param prefixs 前缀
     * @return 去除前缀后的字符串
     */
    public static String removePrefix(String str,String ... prefixs) {
        if(str == null || str.isEmpty()) {
            return "";
        }

        if(prefixs.length == 0) {
            return str;
        }

        for(String prefix:prefixs) {
            if (str.startsWith(prefix)) {
                return str.substring(prefix.length());
            }
        }

        return str;
    }

    /**
     * 去除指定后缀
     * @param str 需要去除后缀的字符串
     * @param suffixs 后缀
     * @return 去除后缀后的字符串
     */
    public static String removeSuffix(String str,String ... suffixs) {
        if(str == null || str.isEmpty()) {
            return "";
        }

        if(suffixs.length == 0) {
            return str;
        }

        for(String suffix:suffixs) {
            if (str.endsWith(suffix)) {
                return str.substring(0, str.length() - suffix.length());
            }
        }

        return str;
    }

    /**
     * 拼接成字符串
     * @param arr 需要拼接的数组
     * @return 拼接后的字符串
     */
    public static String join(Object[] arr) {
        String str = "";
        for(int i = 0;i < arr.length;i++) {
            str += arr[i];
        }
        return str;
    }

    /**
     * 拼接成字符串
     * @param arr 需要拼接的数组
     * @param separator 分隔符
     * @return 拼接后的字符串
     */
    public static String join(Object[] arr,String separator) {
        String str = "";
        for(int i = 0;i < arr.length;i++) {
            if(i == arr.length - 1) {
                str += arr[i];
            } else {
                str += arr[i]+separator;
            }
        }
        return str;
    }

    /**
     * 拼接成字符串
     * @param list 需要拼接的集合
     * @return 拼接后的字符串
     */
    public static String join(List list) {
        String str = "";
        for(int i = 0;i < list.size();i++) {
            str += list.get(i);
        }
        return str;
    }

    /**
     * 拼接成字符串
     * @param list 需要拼接的集合
     * @param separator 分隔符
     * @return 拼接后的字符串
     */
    public static String join(List list,String separator) {
        String str = "";
        for(int i = 0;i < list.size();i++) {
            if(i == list.size() - 1) {
                str += list.get(i);
            } else {
                str += list.get(i)+separator;
            }
        }
        return str;
    }

    /**
     * 截取字符串
     * @param str 字符串
     * @param start 开始
     * @return 结果
     */
    public static String substring(final String str, int start) {
        if (str == null) {
            return "";
        }
        if (start < 0) {
            start = str.length() + start;
        }
        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return "";
        }
        return str.substring(start);
    }

    /**
     * 截取字符串
     * @param str 字符串
     * @param open 开始
     * @param close 结束
     * @return 结果
     */
    public static String substring(String str, String open, String close) {
        if (Valid.isNull(str, open, close)) {
            return null;
        } else {
            int start = str.indexOf(open);
            if (start != -1) {
                int end = str.indexOf(close, start + open.length());
                if (end != -1) {
                    return str.substring(start + open.length(), end);
                }
            }

            return null;
        }
    }

    /**
     * 计算一个指定字符串在字符串中出现的次数
     * @param str 字符串
     * @param findStr 需要计算出现次数的字符串
     * @return 出现次数
     */
    public static int findStrCount(String str,String findStr) {
        int count = 0;
        while (str.contains(findStr)) {
            str = str.substring(str.indexOf(findStr) + findStr.length());
            count ++;
        }
        return count;
    }

    /**
     * 驼峰转下划线
     * @param str 目标字符串
     * @return 转换后的字符串
     */
    public static String humpToUnderline(String str) {
        String regex = "([A-Z])";
        Matcher matcher = Pattern.compile(regex).matcher(str);
        while(matcher.find()) {
            String target = matcher.group();
            str = str.replaceAll(target, "_"+target.toLowerCase());
        }
        return str;
    }

    /**
     * 下划线转驼峰
     * @param str 目标字符串
     * @return 转换后的字符串
     */
    public static String underlineToHump(String str) {
        String regex = "_(.)";
        Matcher matcher = Pattern.compile(regex).matcher(str);
        while(matcher.find()) {
            String target = matcher.group(1);
            str = str.replaceAll("_"+target, target.toUpperCase());
        }
        return str;
    }

    /**
     * 解析算式，并计算算式结果；
     * @param str 算式的字符串
     * @return 算式结果
     */
    public static BigDecimal eval(String str) {
        // 递归头
        if(str.isEmpty()) {
            return BigDecimal.ZERO;
        }
        if(Valid.isDecimal(str)) {
            return new BigDecimal(str);
        }
        str = str.replaceAll(" ", ""); // 去除空格
        //递归体
        if(str.contains(")")) {
            // 最后一个左括号
            int lIndex = str.lastIndexOf("(");
            // 对于的右括号
            int rIndex = str.indexOf(")", lIndex);
            return eval(str.substring(0, lIndex) + eval(str.substring(lIndex + 1, rIndex)) + str.substring(rIndex + 1));
        }
        if(str.contains("+")) {
            int index = str.lastIndexOf("+");
            return eval(str.substring(0, index)).add(eval(str.substring(index + 1)));
        }
        if(str.contains("-")) {
            int index = str.lastIndexOf("-");
            return eval(str.substring(0, index)).subtract(eval(str.substring(index + 1)));
        }
        if(str.contains("*")) {
            int index = str.lastIndexOf("*");
            return eval(str.substring(0, index)).multiply(eval(str.substring(index + 1)));
        }
        if(str.contains("/")) {
            int index = str.lastIndexOf("/");
            return eval(str.substring(0, index)).divide(eval(str.substring(index + 1)), RoundingMode.HALF_UP);
        }
        // 出错
        return null;
    }

    /**
     * 将数字字符串转换成逗号分隔的数字串，即从右边开始每三个数字用逗号分隔
     * @param str 数字字符串
     * @return 分割后的数字字符串，例如 3,120,111
     */
    public static String numberFenGe(String str) {
        StringBuffer sb = new StringBuffer(str);
        int index = sb.indexOf(".")>=0?sb.indexOf("."):sb.length();
        for(int i=index-3;i>0;i-=3){
            sb.insert(i,",");
        }
        return sb.toString();
    }

    /**
     * 数字转成中文
     * @param str 数字字符串
     * @return 转换后的字符串
     */
    public static String numToChinese(String str) {
        //判断输入的金额字符串
        if ("0".equals(str) || "0.00".equals(str) || "0.0".equals(str)) {
            return CN_ZEOR_FULL;
        }

        //判断是否存在负号"-"
        boolean flag = false;

        if (str.startsWith("-")) {
            flag = true;
            str = str.replaceAll("-", "");
        }

        //如果输入字符串中包含逗号，替换为 "."
        str = str.replaceAll(",", ".");
        String integerStr;//整数部分数字
        String decimalStr;//小数部分数字

        //分离整数部分和小数部分
        if (str.indexOf(".") > 0) {//整数部分和小数部分
            integerStr = str.substring(0, str.indexOf("."));
            decimalStr = str.substring(str.indexOf(".") + 1);
        } else if (str.indexOf(".") == 0) {//只存在小数部分 .34
            integerStr = "";
            decimalStr = str.substring(1);
        } else { //只存在整数部分 34
            integerStr = str;
            decimalStr = "";
        }

        //整数部分超出计算能力，直接返回
        if (integerStr.length() > IUNIT_AMOUNT.length) {
            System.out.println(str + "：超出计算能力");
            return str;
        }

        //整数部分存入数组 目的是为了可以动态的在字符串数组中取对应的值
        int[] integers = toIntArray(integerStr);

        //判断整数部分是否存在输入012的情况
        if (integers.length > 1 && integers[0] == 0) {
            System.out.println("抱歉，请输入数字！");
            if (flag) {
                str = "-" + str;
            }
            return str;
        }

        boolean isWan = isWanUnits(integerStr);//设置万单位
        //小数部分数字存入数组
        int[] decimals = toIntArray(decimalStr);
        String result = getChineseInteger(integers, isWan) + getChineseDecimal(decimals);//返回最终的大写金额

        if (flag) {
            return CN_NEGATIVE + result;//如果是负数，加上"负"
        } else {
            return result;
        }
    }

    /**
     * 将字符串转为 int 数组
     * @param str 字符串
     * @return 转换后的 int 数组
     */
    private static int[] toIntArray(String str) {
        //初始化一维数组长度
        int[] array = new int[str.length()];

        //循环遍历赋值
        for (int i = 0; i < str.length(); i++) {
            array[i] = Integer.parseInt(str.substring(i, i + 1));
        }
        return array;
    }

    /**
     * 将整数部分转为大写的金额
     * @param integers int
     * @param isWan 判断
     * @return 转换后的字符串
     */
    private static String getChineseInteger(int[] integers, boolean isWan) {
        StringBuffer chineseInteger = new StringBuffer("");
        int length = integers.length;
        // 对于输入的字符串为 "0." 存入数组后为 0
        if (length == 1 && integers[0] == 0) {
            return "";
        }
        for (int i = 0; i < length; i++) {
            String key = "";//0325464646464
            if (integers[i] == 0) {
                if ((length - i) == 13)//万（亿）
                    key = IUNIT_AMOUNT[4];
                else if ((length - i) == 9) {//亿
                    key = IUNIT_AMOUNT[8];
                } else if ((length - i) == 5 && isWan) {//万
                    key = IUNIT_AMOUNT[4];
                } else if ((length - i) == 1) {//元
                    key = IUNIT_AMOUNT[0];
                }
                if ((length - i) > 1 && integers[i + 1] != 0) {
                    key += NUMBERS_AMOUNT[0];
                }
            }
            chineseInteger.append(integers[i] == 0 ? key : (NUMBERS_AMOUNT[integers[i]] + IUNIT_AMOUNT[length - i - 1]));
        }
        return chineseInteger.toString();
    }

    /**
     * 将小数部分转为大写的金额
     * @param decimals ints
     * @return 转换后的字符串
     */
    private static String getChineseDecimal(int[] decimals) { //角 分 厘 038 壹分捌厘
        StringBuffer chineseDecimal = new StringBuffer("");
        String flag0 = "";
        for (int i = 0; i < decimals.length; i++) {
            if (i == 3) {
                break;
            }
            chineseDecimal.append(decimals[i] == 0 ? "" : (NUMBERS_AMOUNT[decimals[i]] + DUNIT_AMOUNT[i]));
        }
        if("".equals(chineseDecimal.toString())){
            //如果小数部分为00或者不存在时部位 整
            chineseDecimal.append(CN_FULL);
        }else if (chineseDecimal.indexOf("角") != -1 && chineseDecimal.indexOf("分") == -1){
            // 有角没分
            chineseDecimal.append(CN_FULL);
        }
        return flag0 + chineseDecimal.toString();
    }

    /**
     * 判断当前整数部分是否已经是达到【万】
     * @param integerStr int
     * @return true达到
     */
    private static boolean isWanUnits(String integerStr) {
        int length = integerStr.length();
        if (length > 4) {
            String subInteger = "";
            if (length > 8) {
                subInteger = integerStr.substring(length - 8, length - 4);
            } else {
                subInteger = integerStr.substring(0, length - 4);
            }
            return Integer.parseInt(subInteger) > 0;
        } else {
            return false;
        }
    }
}
