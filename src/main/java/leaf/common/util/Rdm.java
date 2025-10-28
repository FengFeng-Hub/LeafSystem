package leaf.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * 随机数操作类
 */
public class Rdm {
    public enum Nin {
        /**
         * 移动网络识别号
         */
        YD,
        /**
         * 电信网络识别号
         */
        DX,
        /**
         * 联通网络识别号
         */
        LT,
        /**
         * 广电网络识别号
         */
        GD,
        /**
         * 全部网络识别号
         */
        ALL
    }

    /**
     * 移动网络识别号
     */
    private static final String[] YD = {"134","135","136","137","138","139","147","150","151","152","157","158","159","174","182","183","184","187","188","195","197","198"};
    /**
     * 电信网络识别号
     */
    private static final String[] DX = {"133","153","173","177","180","181","189","190","191","193","199"};
    /**
     * 联通网络识别号
     */
    private static final String[] LT = {"130","131","132","145","155","156","166","175","176","185","186","196"};
    /**
     * 广电网络识别号
     */
    private static final String[] GD = {"192"};
    /**
     * 全部网络识别号
     */
    private static final String[] ALL = {"130","131","132","133","134","135","136","137","138","139","145","147","150","151","152","153","155","156", "157","158","159",
            "166","173","174","175","176","177","180","181","182","183","184","185","186","187","188","189","190","191","192","193","195","196","197","198","199"};
    /**
     * 身份证省、自治区、直辖市代码
     */
    private static final String PROVINCES[] = { "11", "12", "13", "14", "15", "21", "22", "23", "31", "32", "33", "34", "35", "36", "37", "41", "42", "43",
            "44", "45", "46", "50", "51", "52", "53", "54", "61", "62", "63", "64", "65", "71", "81", "82" };
    /**
     * 身份证地级市、盟、自治州代码
     */
    private static final String CITYS[] = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "21", "22", "23", "24", "25", "26", "27", "28" };
    /**
     * 身份证县、县级市、区代码
     */
    private static final String COUNTS[] = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38" };

    /**
     * 生成指定位数的数字
     * @param length 长度
     * @return 生成后的数字
     */
    public static String num(int length) {
        return String.format("%0"+length+"d",new Random().nextInt((int) StrictMath.pow(10,length)));
    }

    /**
     * 生成指定位数的指定字符串
     * @param length 长度
     * @param str 指定字符串
     * @return 生成后的字符串
     */
    public static String str(int length,String str) {
        char[] nonceChars = new char[length];
        for (int index = 0; index < nonceChars.length; ++index) {
            nonceChars[index] = str.charAt(new Random().nextInt(str.length()));
        }
        return new String(nonceChars);
    }

    /**
     * 生成随机手机号
     * @param nin 网络识别号
     * @return 生成后的手机号
     */
    public static String phone(Nin nin) {
        Random rdm = new Random();
        String[] nin_ = Rdm.ALL;
        if(nin == null || nin == Nin.ALL) {
            return nin_[rdm.nextInt(nin_.length)]+num(8);
        }
        switch (nin) {
            case YD:
                nin_ = Rdm.YD;
                break;
            case DX:
                nin_ = Rdm.DX;
                break;
            case LT:
                nin_ = Rdm.LT;
                break;
            case GD:
                nin_ = Rdm.GD;
                break;
        }
        return nin_[rdm.nextInt(nin_.length)]+num(8);
    }

    /**
     * 生成随机身份证号码
     * @return 生成后的身份证号码
     */
    public static String IDCard() {
        StringBuffer identityNo = new StringBuffer();
        identityNo.append(PROVINCES[new Random().nextInt(PROVINCES.length - 1)]);// 随机生成省、自治区、直辖市代码 1-2
        identityNo.append(CITYS[new Random().nextInt(CITYS.length - 1)]);// 随机生成地级市、盟、自治州代码 3-4
        identityNo.append(COUNTS[new Random().nextInt(COUNTS.length - 1)]);// 随机生成县、县级市、区代码 5-6
        SimpleDateFormat dft = new SimpleDateFormat("yyyyMMdd");// 随机生成出生年月 7-14
        Date beginDate = new Date();
        Calendar date = Calendar.getInstance();
        date.setTime(beginDate);
        date.set(Calendar.DATE, date.get(Calendar.DATE) - new Random().nextInt(365 * 100));
        identityNo.append(dft.format(date.getTime()));
        identityNo.append(num(3));// 随机生成顺序号 15-17
        identityNo.append(getVerifyCode(identityNo));// 生成校验码 18
        return identityNo.toString();
    }

    /**
     * 计算校验码
     * @param cardId 身份证号
     * @return 生成后的校验码
     */
    private static char getVerifyCode(StringBuffer cardId) {
        char[] ValCodeArr = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int[] Wi = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        int tmp = 0;
        for (int i = 0; i < Wi.length; i++) {
            tmp += Integer.parseInt(String.valueOf(cardId.charAt(i))) * Wi[i];
        }
        return ValCodeArr[tmp % 11];
    }
}
