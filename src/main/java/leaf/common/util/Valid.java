package leaf.common.util;

import leaf.common.IO;
import leaf.common.Log;
import leaf.common.object.JSONMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据验证类
 */
public class Valid {
    /**
     * 判断参数是否为 null
     * @param objs 参数
     * @return true为 null
     */
    public static boolean isNull(Object ... objs) {
        for(Object obj : objs)
            if(obj == null) return true;

        return false;
    }

    /**
     * 判断字符串参数是否为 null 或者空字符串
     * @param str 需要验证的字符串
     * @return true为 null 或者空字符串
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串参数是否为 null 或者空字符串
     * @param strs 字符串参数
     * @return true为 null 或者空字符串
     */
    public static boolean isEmptyAll(String ... strs) {
        if(strs == null)
            return true;

        for(String str : strs)
            if(str == null || str.isEmpty()) return true;
        return false;
    }

    /**
     * 判断字符串参数是否为空白（空格、\t、\n、\f、会被认定为空\r）
     * @param str 需要验证的字符串
     * @return true为空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.replaceAll("\\s*","").isEmpty();
    }

    /**
     * 判断字符串参数是否为空白（空格、\t、\n、\f、会被认定为空\r）
     * @param strs 字符串参数
     * @return true为空白
     */
    public static boolean isBlankAll(String ... strs) {
        if(strs == null)
            return true;

        for(String str : strs)
            if(str == null || str.replaceAll("\\s*","").isEmpty()) return true;
        return false;
    }

    /**
     * 验证指定字符串是否存在于指定字符串数组里
     * @param str 需要验证的字符串
     * @param StrArr 字符串数组
     * @return true存在
     */
    public static boolean isExistInStrArr(String str,String ... StrArr) {
        for(String s : StrArr)
            if(s.equals(str)) return true;

        return false;
    }

    /**
     * 是否为 int 类型
     * @param str 需要验证的字符串
     * @return true为 int 类型
     */
    public static boolean isIntType(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    /**
     * 是否为整数
     * @param str 需要验证的字符串
     * @return true为整数
     */
    public static boolean isInteger(String str) {
        return str != null && str.matches("^[0-9]*[0-9][0-9]*$");
    }

    /**
     * 是否为小数
     * @param str 需要验证的字符串
     * @return true为小数
     */
    public static boolean isDecimal(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * 是否为日期时间字符串
     * @param str 需要验证的字符串
     * @param formatStr 字符串格式
     * @return true为日期时间字符串
     */
    public static boolean isDateTimeStr(String str,String formatStr) {
        if(str == null || str.isEmpty()) return false;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatStr);
        try {
            Date date = simpleDateFormat.parse(str);
            return str.equals(simpleDateFormat.format(date));
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * 是否为手机号
     * @param str 需要验证的字符串
     * @return true为手机号
     */
    public static boolean isPhone(String str){
        String regex = "^(1[3-9]\\d{9}$)";
        if (str.length() == 11) {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(str);
            if (m.matches()) return true;
        }
        return false;
    }

    /**
     * 是否为邮箱
     * @param str 需要验证的字符串
     * @return true为邮箱
     */
    public static boolean isEmail(String str){
        if ((str != null) && (!str.isEmpty())) {
            return Pattern.matches("^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$", str);
        }
        return false;
    }

    /**
     * 是否为身份证号
     * @param str 需要验证的字符串
     * @return true为身份证号
     */
    public static boolean isIDCard(String str) {
        Map<Integer, String> zoneNum = new HashMap<>();
        zoneNum.put(11, "北京");
        zoneNum.put(12, "天津");
        zoneNum.put(13, "河北");
        zoneNum.put(14, "山西");
        zoneNum.put(15, "内蒙古");
        zoneNum.put(21, "辽宁");
        zoneNum.put(22, "吉林");
        zoneNum.put(23, "黑龙江");
        zoneNum.put(31, "上海");
        zoneNum.put(32, "江苏");
        zoneNum.put(33, "浙江");
        zoneNum.put(34, "安徽");
        zoneNum.put(35, "福建");
        zoneNum.put(36, "江西");
        zoneNum.put(37, "山东");
        zoneNum.put(41, "河南");
        zoneNum.put(42, "湖北");
        zoneNum.put(43, "湖南");
        zoneNum.put(44, "广东");
        zoneNum.put(45, "广西");
        zoneNum.put(46, "海南");
        zoneNum.put(50, "重庆");
        zoneNum.put(51, "四川");
        zoneNum.put(52, "贵州");
        zoneNum.put(53, "云南");
        zoneNum.put(54, "西藏");
        zoneNum.put(61, "陕西");
        zoneNum.put(62, "甘肃");
        zoneNum.put(63, "青海");
        zoneNum.put(64, "宁夏");
        zoneNum.put(65, "新疆");
        zoneNum.put(71, "台湾");
        zoneNum.put(81, "香港");
        zoneNum.put(82, "澳门");
        zoneNum.put(91, "外国");
        int[] PARITYBIT = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int[] POWER_LIST = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

        //身份证号码长度
        if (str == null ||  str.length() != 18)
            return false;

        char[] cs = str.toUpperCase().toCharArray();
        //校验位数
        int power = 0;

        for (int i = 0; i < cs.length; i++) {
            if (i == cs.length - 1 && cs[i] == 'X')
                break;//最后一位可以 是X或x
            if (cs[i] < '0' || cs[i] > '9')
                return false;
            if (i < cs.length - 1) {
                power += (cs[i] - '0') * POWER_LIST[i];
            }
        }

        //校验区位码
        if (!zoneNum.containsKey(Integer.valueOf(str.substring(0, 2)))) {
            return false;
        }

        //校验年份
        String year = str.substring(6, 10);
        final int iyear = Integer.parseInt(year);

        if (iyear < 1900 || iyear > Calendar.getInstance().get(Calendar.YEAR))
            return false;//1900年的PASS，超过今年的PASS

        //校验月份
        String month = str.substring(10, 12);
        final int imonth = Integer.parseInt(month);

        if (imonth < 1 || imonth > 12) {
            return false;
        }

        //校验天数
        String day = str.substring(12, 14);
        final int iday = Integer.parseInt(day);

        if (iday < 1 || iday > 31)
            return false;

        //校验"校验码"
        return cs[cs.length - 1] == PARITYBIT[power % 11];
    }

    /**
     * 滑动验证小图宽度（已弃用）
     */
    @Deprecated
    private static final int targetWidth = 100;
    /**
     * 滑动验证小图高度（已弃用）
     */
    @Deprecated
    private static final int targetHeight = 80;
    /**
     * 半径（已弃用）
     */
    @Deprecated
    private static final int circleR = 30;
    /**
     * 距离点（已弃用）
     */
    @Deprecated
    private static final int r1 = 0;

    /**
     * 生成拼图验证
     * @param imgPath 图片路径(分辨率最好为 800px x 500px)
     * @return JSONMap 类型
     * {
     *      "bigImage":"小图base64字符串",
     *      "smallImage":"大图base64字符串",
     *      "x":"横坐标",
     *      "y":"纵坐标"
     * }
     */
    @Deprecated
    public static JSONMap createPuzzleValid(String imgPath){
        try {
            JSONMap result = new JSONMap();
            BufferedImage bufferedImage = ImageIO.read(new File(imgPath));
            Random rand = new Random();
            int widthRandom = rand.nextInt(bufferedImage.getWidth() -  targetWidth - 100 + 1 ) + 100;
            int heightRandom = rand.nextInt(bufferedImage.getHeight() - targetHeight + 1 );
//            System.out.println("原图大小"+bufferedImage.getWidth()+" x "+bufferedImage.getHeight()+
//                    ",随机生成的坐标 X,Y 为（"+widthRandom+"，"+heightRandom+"）");
            BufferedImage target= new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_4BYTE_ABGR);
            cutByTemplate(bufferedImage,target,getBlockData(),widthRandom,heightRandom);
            result.put("bigImage", IO.bytesToBase64(IO.bufferedImageToBytes(bufferedImage)));//大图
            result.put("smallImage", IO.bytesToBase64(IO.bufferedImageToBytes(target)));//小图
            result.put("x",widthRandom);
            result.put("y",heightRandom);
            return result;
        } catch (Exception e) {
            Log.write("Error",Log.getException(e));
            return null;
        }
    }

    /**
     * 生成小图轮廓
     * @return int[][]
     */
    @Deprecated
    private static int[][] getBlockData() {
        int[][] data = new int[targetWidth][targetHeight];
        double x2 = targetWidth -circleR; //47
        //随机生成圆的位置
        double h1 = circleR + Math.random() * (targetWidth-3*circleR-r1);
        double po = Math.pow(circleR,2); //64
        double xbegin = targetWidth - circleR - r1;
        double ybegin = targetHeight- circleR - r1;
        //圆的标准方程 (x-a)²+(y-b)²=r²,标识圆心（a,b）,半径为r的圆
        //计算需要的小图轮廓，用二维数组来表示，二维数组有两张值，0和1，其中0表示没有颜色，1有颜色

        for (int i = 0; i < targetWidth; i++) {
            for (int j = 0; j < targetHeight; j++) {
                double d2 = Math.pow(j - 2,2) + Math.pow(i - h1,2);
                double d3 = Math.pow(i - x2,2) + Math.pow(j - h1,2);
                if ((j <= ybegin && d2 < po)||(i >= xbegin && d3 > po)) {
                    data[i][j] = 0;
                }  else {
                    data[i][j] = 1;
                }
            }
        }
        return data;
    }
    /**
     * 有这个轮廓后就可以依据这个二维数组的值来判定抠图并在原图上抠图位置处加阴影,
     * @param oriImage 原图
     * @param targetImage 抠图拼图
     * @param templateImage 颜色
     * @param x x
     * @param y y
     */
    @Deprecated
    private static void cutByTemplate(BufferedImage oriImage, BufferedImage targetImage, int[][] templateImage, int x, int y){
        int[][] martrix = new int[3][3];
        int[] values = new int[9];
        //创建shape区域
        for (int i = 0; i < targetWidth; i++) {
            for (int j = 0; j < targetHeight; j++) {
                int rgb = templateImage[i][j];
                // 原图中对应位置变色处理
                int rgb_ori = oriImage.getRGB(x + i, y + j);
                if (rgb == 1) {
                    targetImage.setRGB(i, j, rgb_ori);
                    //抠图区域高斯模糊
                    int _x = x + i,_y = y + i,xStart = _x - 1,yStart = _y - 1,current = 0;
                    for (int a = xStart; a < 3 + xStart; a++) {
                        for (int b = yStart; b < 3 + yStart; b++) {
                            int tx = a;
                            if (tx < 0) {
                                tx = -tx;
                            } else if (tx >= oriImage.getWidth()) {
                                tx = _x;
                            }
                            int ty = b;
                            if (ty < 0) {
                                ty = -ty;
                            } else if (ty >= oriImage.getHeight()) {
                                ty = _y;
                            }
                            try {
                                values[current++] = oriImage.getRGB(tx, ty);
                            } catch(Exception e) {
                                break;
                            }
                        }
                    }
                    int filled = 0;

                    for (int a = 0; a < martrix.length; a++) {
                        int[] xs = martrix[a];
                        for (int b = 0; b < xs.length; b++) {
                            xs[b] = values[filled++];
                        }
                    }
                    int r = 0,g = 0,b = 0;

                    for (int a = 0; a < martrix.length; a++) {
                        int[] xs = martrix[a];
                        for (int _b = 0; _b < xs.length; _b++) {
                            if (_b == 1) {
                                continue;
                            }
                            Color c = new Color(xs[_b]);
                            r += c.getRed();
                            g += c.getGreen();
                            b += c.getBlue();
                        }
                    }
                    oriImage.setRGB(x + i, y + j, new Color(r / 8, g / 8, b / 8).getRGB());
                }else{
                    targetImage.setRGB(i, j, rgb_ori & 0x00ffffff);//这里把背景设为透明
                }
            }
        }
    }
}
