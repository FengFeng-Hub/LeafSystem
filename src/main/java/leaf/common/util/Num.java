package leaf.common.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * 数字处理类
 */
public class Num {
    /**
     * 向上取整 示例：1.5 = 2| -1.5 = -1
     */
    public static final RoundingMode Ceil = RoundingMode.CEILING,
    /**
     * 向下取整 示例：1.5 = 1 | -1.5 = -2
     */
    Floor = RoundingMode.FLOOR,
    /**
     * 向0远的方向（向外）取整 示例：1.5 = 2 | -1.5 = -2
     */
    Up = RoundingMode.UP,
    /**
     * 向0近的方向（向内）取整 示例：1.5 = 1 | -1.5 = -1
     */
    Down = RoundingMode.DOWN,
    /**
     * 四舍五入 示例：1.4 = 1 | 1.5 = 2
     */
    HalfUp = RoundingMode.HALF_UP,
    /**
     * 五舍大于五入 示例：1.5 = 1 | 1.500000000001 = 2
     */
    HalfDown = RoundingMode.HALF_DOWN,
    /**
     * 银行家舍入模式  舍入位大于5 或者 舍入位等于5且前一位为奇数，则对舍入部分的前一位数字加1 示例：1.4 = 1 | 1.5 = 2
     *              舍入位小于5 或者 舍入位等于5且前一位为偶数，则直接舍弃。即为银行家舍入模式 示例：2.4 = 2 | 2.5 = 2
     */
    HalfEven = RoundingMode.HALF_EVEN,
    /**
     * 舍入模式来确定所请求的操作具有精确的结果，因此不需要舍入。 示例：1.0 = 1 | 1.5 抛出 ArithmeticException
     */
    Unnecessary = RoundingMode.UNNECESSARY;
    /*
        BigDecimal:
        加       add()
        减       subtract()
        乘       multiply()
        除       divide()
        取余     divideAndRemainder()
        处理小数  setScale()
     */

    /**
     * 将包含 BigInteger 的二进制补码二进制表达式的字节数组转换为 BigInteger
     * @param v bytes
     * @return 转换后的 BigInteger 对象
     */
    public static BigInteger integer(byte[] v) {
        return new BigInteger(v);
    }

    /**
     * 将 BigDecimal 转换为 BigInteger
     * @param v BigDecimal
     * @return 转换后的 BigInteger 对象
     */
    public static BigInteger integer(BigDecimal v) {
        return new BigInteger(v.toString());
    }

    /**
     * 将 BigInteger 的十进制字符串表示形式转换为 BigInteger
     * @param v string
     * @return 转换后的 BigInteger 对象
     */
    public static BigInteger integer(String v) {
        return new BigInteger(v);
    }

    /**
     * 将int转换为 BigInteger
     * @param v string
     * @return 转换后的 BigInteger 对象
     */
    public static BigInteger integer(int v) {
        return new BigInteger(String.valueOf(v));
    }

    /**
     * 将 BigInteger 转换成 BigDecimal
     * @param v BigInteger
     * @return 转换后的 BigDecimal 对象
     */
    public static BigDecimal decimal(BigInteger v) {
        return new BigDecimal(v);
    }

    /**
     * 将BigDecimal的字符串表示 BigDecimal转换为BigDecimal
     * @param v string
     * @return 转换后的 BigDecimal 对象
     */
    public static BigDecimal decimal(String v) {
        return new BigDecimal(v);
    }

    /**
     * 一个转换的字符数组表示 BigDecimal 成 BigDecimal，接受字符作为的相同序列 BigDecimal(String) 构造
     * @param v chars
     * @return 转换后的 BigDecimal 对象
     */
    public static BigDecimal decimal(char[] v) {
        return new BigDecimal(v);
    }

    /**
     * 将int转换为 BigDecimal
     * @param v int
     * @return 转换后的 BigDecimal 对象
     */
    public static BigDecimal decimal(int v) {
        return new BigDecimal(v);
    }

    /**
     * 将double 转换为 BigDecimal
     * @param v double
     * @return 转换后的 BigDecimal 对象
     */
    public static BigDecimal decimal(double v) {
        return new BigDecimal(v);
    }

    /**
     * 将long 转换为 BigDecimal
     * @param v long
     * @return 转换后的 BigDecimal 对象
     */
    public static BigDecimal decimal(long v) {
        return new BigDecimal(v);
    }

    /**
     * 格式化十进制数字
     * @param number 需要格式化的数组
     * @param pattern 格式化方式
     *                  符号          位置          含义
     *                  0           数字          阿拉伯数字，如果不存在则补0
     *                  #  	        数字          阿拉伯数字，如果不存在则显示为空
     *                  . 	        数字          小数分隔符或货币小数分隔符
     *                  - 	        数字          减号
     *                  , 	        数字          分组分隔符
     *                  E 	        数字          分隔科学计数法中的尾数和指数。在前缀或后缀中无需加引号。
     *                  ; 	        子模式边界     分隔正数和负数子模式
     *                  % 	        前缀或后缀     乘以 100 并显示为百分数
     *                  /u2030	    前缀或后缀     乘以 1000 并显示为千分数
     *                  ¤(/u00A4)	前缀或后缀     货币记号，由货币符号替换。如果两个同时出现，则用国际货币符号替换。如果出现在某个模式中，则使用货币小数分隔符，而不使用小数分隔符。
     *                  '	        前缀或后缀     用于在前缀或或后缀中为特殊字符加引号，例如 "'#'#" 将 123 格式化为 "#123"。要创建单引号本身，请连续使用两个单引号："# o''clock"
     * @return 格式化后的字符串
     */
    public static String format(Object number,String pattern) {
        return new DecimalFormat(pattern).format(number);
    }

    /**
     * 格式化十进制数字
     * @param number 需要格式化的数组
     * @param pattern 格式化方式
     *                  符号          位置          含义
     *                  0           数字          阿拉伯数字，如果不存在则补0
     *                  #  	        数字          阿拉伯数字，如果不存在则显示为空
     *                  . 	        数字          小数分隔符或货币小数分隔符
     *                  - 	        数字          减号
     *                  , 	        数字          分组分隔符
     *                  E 	        数字          分隔科学计数法中的尾数和指数。在前缀或后缀中无需加引号。
     *                  ; 	        子模式边界     分隔正数和负数子模式
     *                  % 	        前缀或后缀     乘以 100 并显示为百分数
     *                  /u2030	    前缀或后缀     乘以 1000 并显示为千分数
     *                  ¤(/u00A4)	前缀或后缀     货币记号，由货币符号替换。如果两个同时出现，则用国际货币符号替换。如果出现在某个模式中，则使用货币小数分隔符，而不使用小数分隔符。
     *                  '	        前缀或后缀     用于在前缀或或后缀中为特殊字符加引号，例如 "'#'#" 将 123 格式化为 "#123"。要创建单引号本身，请连续使用两个单引号："# o''clock"
     * @param roundingMode RoundingMode 取整方式
     * @return 格式化后的字符串
     */
    public static Object format(Object number,String pattern,RoundingMode roundingMode) {
        DecimalFormat decimalFormat = new DecimalFormat(pattern);
        decimalFormat.setRoundingMode(roundingMode);
        return decimalFormat.format(number);
    }
}
