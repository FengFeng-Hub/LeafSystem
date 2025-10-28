package leaf.common.util;

import leaf.common.IO;
import leaf.common.object.JSONMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * 验证机器人类
 */
public class ValidRobot {
    /**
     * 获取验证码
     * @param length 验证码长度
     * @return JSONMap 类型
     *  {
     *      "img":"图片（base64字符串）",
     *      "text":"验证码",
     *  }
     */
    public static JSONMap getValidCode(int length) {
        int width = length * 50;
        int height = 70;
        BufferedImage valid = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) valid.getGraphics();
        graphics.setColor(Color.WHITE);// 设置画笔颜色-验证码背景色
        graphics.fillRect(0, 0, width, height);// 填充背景
        graphics.setFont(new Font("微软雅黑", Font.BOLD, 40));
        // 数字和字母的组合
        String baseNumLetter = "123456789abcdefghijklmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
        StringBuffer sBuffer = new StringBuffer();
        int x = 20; // 旋转原点的 x 坐标
        String ch = "";
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            graphics.setColor(new Color(random.nextInt(256),random.nextInt(256), random.nextInt(256)));
            // 设置字体旋转角度
            int degree = random.nextInt() % 75;//角度小于75度
            int dot = random.nextInt(baseNumLetter.length());
            ch = baseNumLetter.charAt(dot) + "";
            sBuffer.append(ch);
            graphics.setFont(new Font("宋体",Font.BOLD,random.nextInt(20)+40));//设置字体
            // 正向旋转
            graphics.rotate(degree * Math.PI / 180, x, 45);
            graphics.drawString(ch, x, 50);
            // 反向旋转
            graphics.rotate(-degree * Math.PI / 180, x, 50);
            x += 48;

        }

        int count = random.nextInt(length) + length * 3;//干扰性数量

        // 画干扰线
        for (int i = 0; i < count; i++) {
            // 设置随机颜色
            graphics.setColor(new Color(random.nextInt(256),random.nextInt(256), random.nextInt(256)));
            // 随机画线
            graphics.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));

        }
        count = random.nextInt(length*2) + length * 8;//噪点数量

        // 添加噪点
        for (int i = 0; i < count; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            graphics.setColor(new Color(random.nextInt(256),random.nextInt(256), random.nextInt(256)));
            graphics.fillRect(x1, y1, 2, 2);
        }

        JSONMap result = new JSONMap();
        result.put("img", IO.bytesToBase64(IO.bufferedImageToBytes(valid)));
        result.put("text", sBuffer.toString());
        return result;

    }

    /**
     * 大图宽度
     */
    private static final Integer bigWidth = 800;
    /**
     * 大图高度
     */
    private static final Integer bigHeight = 500;
    /**
     * 边距（上、右、下小图距离大图最近距离，左边需要留大点边距方便拖动），同时也是内凹点和外凸点到正方形的边距
     */
    private static final Integer margin = 20;
    /**
     * 小正方形边长，制作拼图的轮廓
     */
    private static final Integer square = 100;
    /**
     * 小圆半径，拼图的凹凸轮廓
     */
    private static final Integer circle = 20;
    /**
     * 阴影宽度
     */
    private static final Integer shadow = 2;

    /**
     * 获取验证拼图
     * @param path 图片路径
     * @return JSONMap 类型
     * {
     *      "big_image":"大图base64字符串",
     *      "small_height":"小图base64字符串",
     *      "x":"横坐标",
     *      "y":"纵坐标",
     *      "big_width":"大图宽度",
     *      "big_height":"大图高度",
     *      "small_width":"小图宽度",
     *      "small_height":"小图高度"
     * }
     */
    public static JSONMap getValidPuzzle(String path) {
        Image bi = null;
        try {
            bi = ImageIO.read(new File(path));
        } catch (IOException e) {
            return null;
        }
        //规范原图的大小
        BufferedImage oriImage=resizeImage(bi,bigWidth,bigHeight,true);
        //获取正方形的位置和边缘凹凸信息
        Integer[] pos = getPosAndOutline();
        //计算拼图的真实大小和拼图的位置信息
        int width = square + 1;
        int height = square + 1;

        if (pos[2] == 2){
            //上方突出，真实高度加一个margin，真实坐标，正方形y坐标减去一个margin
            height += margin;
            pos[9] = pos[1]-margin;
        }

        if (pos[3] == 2){
            width += margin;
            pos[8] = pos[0]-margin;
        }

        if (pos[4] == 2){
            height += margin;
        }

        if (pos[5] == 2){
            width += margin;
        }

        pos[6] = width;
        pos[7] = height;
        //创建拼图
        BufferedImage targetImage= new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        //获取拼图的裁剪信息（0不要1裁剪）
        Integer[][] templateImage = getSmallImg(pos);
        //裁剪拼图
        cutByTemplate(oriImage,targetImage,templateImage,pos);
        JSONMap puzzleInfo=new JSONMap();
        puzzleInfo.put("big_image",IO.bytesToBase64(IO.bufferedImageToBytes(resizeImage(oriImage,bigWidth,bigHeight,true))));
        puzzleInfo.put("small_image",IO.bytesToBase64(IO.bufferedImageToBytes(resizeImage(targetImage,width,height,false))));
        puzzleInfo.put("x",pos[8] == null?pos[0]:pos[8]);
        puzzleInfo.put("y",pos[9] == null?pos[1]:pos[9]);
        puzzleInfo.put("big_width",bigWidth);
        puzzleInfo.put("big_height",bigHeight);
        puzzleInfo.put("small_width",width);
        puzzleInfo.put("small_height",height);
        return puzzleInfo;
    }

    /**
     * 调整图片尺寸
     * @param image  原始图片对象
     * @param width  目标宽度
     * @param height 目标高度
     * @param type   图片类型标识：true表示RGB格式，false表示ARGB格式（带透明度）
     * @return 调整尺寸后的BufferedImage对象
     */
    private static BufferedImage resizeImage(final Image image, int width, int height,boolean type) {
        BufferedImage bufferedImage;

        if (type){
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
        else {
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        final Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setComposite(AlphaComposite.Src);
        //below three lines are for RenderingHints for better image quality at cost of higher processing time
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }

    /**
     * 生成拼图块的随机位置和边缘轮廓信息
     * @return 包含以下信息的整型数组：
     *         [0] - 正方形区域x坐标
     *         [1] - 正方形区域y坐标
     *         [2] - 顶部轮廓类型（0=无 1=内凹 2=外凸）
     *         [3] - 左侧轮廓类型
     *         [4] - 底部轮廓类型
     *         [5] - 右侧轮廓类型
     *         [6] - 拼图实际宽度
     *         [7] - 拼图实际高度
     *         [8] - 拼图x坐标（有外凸时调整后的值）
     *         [9] - 拼图y坐标（有外凸时调整后的值）
     */
    private static Integer[] getPosAndOutline(){
        //前端将图约束在300*150的框内，所以计算时除以10防止坐标约束时（除以10）出现小数
        //最小高度，只需排除边距加外凸
        int minY = (2*margin)/10;
        //最大高度，排除边距，正方形边长，上下两个外凸（极限）情况下，增加的高度。
        int maxY = (bigHeight - (square + 3 * margin))/10;
        //最小宽度，左边留一个半身位方便移动，身位宽度仍是极限（正方形边长+左右两个外凸）
        int minX = ((square + margin * 2) * 3 / 2)/10;
        //最大宽度，排除边距，正方形边长，左右两个外凸。
        int maxX = (bigWidth - (square + 3 * margin))/10;
        //获取正方形的位置，乘以10计算时保持正常大小，传给前端时需约束处理（除以10）
        int y = (minY + (int) (Math.random() * (maxY + 1 - minY)))*10;
        int x = (minX + (int) (Math.random() * (maxX + 1 - minX)))*10;
        Integer[] p=new Integer[10];
        //正方形x坐标
        p[0]=x;
        //正方形y坐标
        p[1]=y;
        //获取边缘凹凸信息
        //0没有 1内凹圆弧 2外凸圆弧
        int top = new Random().nextInt(3);
        int left = new Random().nextInt(3);
        int down = new Random().nextInt(3);
        int right = new Random().nextInt(3);
        //上方凹凸信息
        p[2]=top;
        //左侧凹凸信息
        p[3]=left;
        //下方凹凸信息
        p[4]=down;
        //右侧凹凸信息
        p[5]=right;
        return p;
    }

    /**
     * 生成小图（拼图块）的轮廓模板矩阵
     * @param pos 包含拼图位置和轮廓信息的数组，结构为：
     *            [0] 正方形x坐标, [1] 正方形y坐标,
     *            [2] 顶部轮廓类型, [3] 左侧轮廓类型,
     *            [4] 底部轮廓类型, [5] 右侧轮廓类型,
     *            [6] 拼图宽度, [7] 拼图高度,
     *            [8] 拼图x坐标（调整后）, [9] 拼图y坐标（调整后）
     * @return 二维整型数组模板，标记哪些像素需要裁剪：
     *         1 = 需要裁剪的区域（拼图块主体或外凸部分）
     *         0 = 不需要裁剪的区域（背景或内凹部分）
     *         模板范围包含拼图块及其外围的阴影区域（shadow宽度）
     */
    private static Integer[][] getSmallImg(Integer[] pos) {
        //正方形x坐标
        Integer x=pos[0];
        //正方形y坐标
        Integer y=pos[1];
        //小图x坐标（小图没有左侧外凸时x坐标与正方形一致）
        Integer posX=null==pos[8]?pos[0]:pos[8];
        //小图y坐标（小图没有上方外凸时y坐标与正方形一致）
        Integer posY=null==pos[9]?pos[1]:pos[9];
        //0没有 1内凹圆弧 2外凸圆弧
        Integer top =  pos[2];
        Integer left =pos[3];
        Integer down = pos[4];
        Integer right = pos[5];
        //取色模板，值为1的地方是小图需要从原图取色，大图需要在原图基础上模糊的位置
        Integer[][] templateImage = new Integer[bigHeight][bigWidth];
        double rPow = Math.pow(circle, 2);

        //模板只需要计算小图范围，其他地方不用管,阴影范围需要从小图边界向外延申shadow长度作为参照
        for(int i = posY-shadow; i <= posY+pos[7]+shadow; i++) {
            for(int j = posX-shadow; j <= posX+pos[6]+shadow; j++) {
                //先判断正方形
                if((i >= y && i <= (y + square) && j >= x && j <= (x + square))) {
                    templateImage[i][j] = 1;
                } else {
                    templateImage[i][j] = 0;
                }
                //不在上方圆内
                if(top == 1) {
                    Integer tx = x + square / 2;
                    Integer ty = y + (margin - circle);
                    //当前点在上方圆形的位置内外
                    double res = Math.pow(j - tx, 2) + Math.pow(i - ty, 2);
                    //在圆内挖掉
                    if (res <= rPow) {
                        templateImage[i][j] = 0;
                    }
                }
                //在上方圆内
                if(top == 2) {
                    Integer tx = x + square / 2;
                    Integer ty = y - (margin - circle);
                    //当前点在上方圆形的位置内外
                    double res = Math.pow(j - tx, 2) + Math.pow(i - ty, 2);
                    //在圆内补上
                    if (res <= rPow) {
                        templateImage[i][j] = 1;
                    }
                }
                //不在左侧圆内
                if(left == 1) {
                    Integer tx = x + (margin - circle);
                    Integer ty = y + square / 2;
                    //当前点在左侧圆形的位置内外
                    double res = Math.pow(j - tx, 2) + Math.pow(i - ty, 2);
                    //在圆内挖掉
                    if (res <= rPow) {
                        templateImage[i][j] = 0;
                    }
                }
                //在左侧圆内
                if(left == 2) {
                    Integer tx = x - (margin - circle);
                    Integer ty = y + square / 2;
                    //当前点在左侧圆形的位置内外
                    double res = Math.pow(j - tx, 2) + Math.pow(i - ty, 2);
                    //在圆内挖掉
                    if (res <= rPow) {
                        templateImage[i][j] = 1;
                    }
                }
                //不在下方圆内
                if(down == 1) {
                    Integer tx = x + square / 2;
                    Integer ty = y + square - (margin - circle);
                    //当前点在左侧圆形的位置内外
                    double res = Math.pow(j - tx, 2) + Math.pow(i - ty, 2);
                    //在圆内挖掉
                    if (res <= rPow) {
                        templateImage[i][j] = 0;
                    }
                }
                //在下方圆内
                if(down == 2) {
                    Integer tx = x + square / 2;
                    Integer ty = y + square + (margin - circle);
                    //当前点在左侧圆形的位置内外
                    double res = Math.pow(j - tx, 2) + Math.pow(i - ty, 2);
                    //在圆内挖掉
                    if (res <= rPow) {
                        templateImage[i][j] = 1;
                    }
                }
                //不在右侧圆内
                if(right == 1) {
                    Integer tx = x + square - (margin - circle);
                    Integer ty = y + square / 2;
                    //当前点在左侧圆形的位置内外
                    double res = Math.pow(j - tx, 2) + Math.pow(i - ty, 2);
                    //在圆内挖掉
                    if (res <= rPow) {
                        templateImage[i][j] = 0;
                    }
                }
                //在右侧圆内
                if(right == 2) {
                    Integer tx = x + square + (margin - circle);
                    Integer ty = y + square / 2;
                    //当前点在左侧圆形的位置内外
                    double res = Math.pow(j - tx, 2) + Math.pow(i - ty, 2);
                    //在圆内挖掉
                    if (res <= rPow) {
                        templateImage[i][j] = 1;
                    }
                }
            }
        }
        return templateImage;
    }

    /**
     * 根据模板从原图中裁剪出拼图块，并对原图对应区域进行模糊处理
     * @param oriImage      原始大图（将被修改，抠图区域会模糊化）
     * @param targetImage   目标小图（拼图块输出）
     * @param templateImage 轮廓模板矩阵（来自getSmallImg）
     * @param pos           拼图位置和轮廓信息数组
     *
     * 处理逻辑：
     * 1. 遍历模板矩阵，标记为1的像素从原图拷贝到拼图块
     * 2. 对原图的抠图区域进行高斯模糊
     * 3. 为拼图块添加边界光影效果：
     *    - 顶部/左侧边界加深阴影（模拟凹陷）
     *    - 底部/右侧边界加亮（模拟凸起）
     */
    private static void cutByTemplate(BufferedImage oriImage, BufferedImage targetImage, Integer[][] templateImage,Integer[] pos) {
        int[][] martrix = new int[3][3];
        int[] values = new int[9];
        //小图x坐标（小图没有左侧外凸时x坐标与正方形一致）
        Integer posX=null==pos[8]?pos[0]:pos[8];
        Integer posY=null==pos[9]?pos[1]:pos[9];

        //创建shape区域
        for(int i = posY; i <=posY+pos[7]; i++) {
            for(int j = posX; j <= posX+pos[6]; j++) {
                int rgb = templateImage[i][j];
                // 原图中对应位置变色处理
                int rgb_ori = oriImage.getRGB(j, i);

                int top=1;
                int left=1;
                int down=1;
                int right=1;
                if (i>shadow-1)
                {
                    top= templateImage[i-shadow][j];
                }
                if (j>shadow-1){
                    left = templateImage[i][j-shadow];
                }
                if (i<bigHeight-shadow){
                    down = templateImage[i+shadow][j];
                }
                if (j<bigWidth-shadow){
                    right = templateImage[i][j+shadow];
                }
                if (rgb == 1) {
                    targetImage.setRGB(j-posX, i-posY, rgb_ori);
                    //抠图区域高斯模糊
                    readPixel(oriImage,  j, i, values);
                    fillMatrix(martrix, values);
                    oriImage.setRGB(j, i, avgMatrix(martrix));

                    Color white = new Color(230,230,230);
                    Color gray=new Color(40,40,40);
                    Color black=new Color(20,20,20);
                    //上方是图区外，当前为顶部边界，加重阴暗
                    if(top == 0) {
                        oriImage.setRGB(j, i,black.getRGB());
                        targetImage.setRGB(j-posX, i-posY,white.getRGB());
                    }
                    //左侧是图区外，当前为左侧边界，加重阴暗
                    if(left == 0) {
                        oriImage.setRGB(j, i,black.getRGB());
                        targetImage.setRGB(j-posX, i-posY,white.getRGB());
                    }
                    //下方是图区外，当前为下方边界，加重光亮
                    if(down == 0) {
                        oriImage.setRGB(j, i,white.getRGB());
                        targetImage.setRGB(j-posX, i-posY,white.getRGB());
                    }
                    //右侧是图区外，当前为右侧边界，加重光亮
                    if(right == 0) {
                        oriImage.setRGB(j, i,white.getRGB());
                        targetImage.setRGB(j-posX, i-posY,white.getRGB());
                    }

                }
//                else {
//                    //这里把背景设为透明
//                    targetImage.setRGB(j, i, rgb_ori & 0x00ffffff);
//                }
            }
        }
    }

    /**
     * 读取图片中指定像素点周围3x3邻域的像素值
     * @param img    源图片
     * @param x      中心点x坐标
     * @param y      中心点y坐标
     * @param pixels 输出数组（长度必须为9），按行优先顺序存储像素值：
     *               [0]左上, [1]中上, [2]右上,
     *               [3]左中, [4]中心, [5]右中,
     *               [6]左下, [7]中下, [8]右下
     *
     * 注意：如果邻域超出图片边界，使用镜像填充（通过tx/ty修正坐标）
     */
    private static void readPixel(BufferedImage img, int x, int y, int[] pixels) {
        int xStart = x - 1;
        int yStart = y - 1;
        int current = 0;

        for (int i = xStart; i < 3 + xStart; i++)
            for (int j = yStart; j < 3 + yStart; j++) {
                int tx = i;

                if (tx < 0) {
                    tx = -tx;
                } else if (tx >= img.getWidth()) {
                    tx = x;
                }

                int ty = j;

                if (ty < 0) {
                    ty = -ty;
                } else if (ty >= img.getHeight()) {
                    ty = y;
                }

                pixels[current++] = img.getRGB(tx, ty);

            }
    }

    /**
     * 将一维像素值数组填充到3x3矩阵中
     * @param matrix 目标3x3矩阵
     * @param values 源像素值数组（长度必须为9）
     *
     * 填充顺序：行优先（即values[0] -> matrix[0][0], values[1] -> matrix[0][1]...）
     */
    private static void fillMatrix(int[][] matrix, int[] values) {
        int filled = 0;

        for (int i = 0; i < matrix.length; i++) {
            int[] x = matrix[i];
            for (int j = 0; j < x.length; j++) {
                x[j] = values[filled++];
            }
        }
    }
    /**
     * 计算3x3像素矩阵的平均RGB值（忽略中心点）
     * @param matrix 3x3像素矩阵，每个元素为RGB整型值
     * @return 平均后的RGB值（8个周边像素的R/G/B通道分别取平均）
     *
     * 用途：用于生成高斯模糊效果，跳过中心点以避免过度平滑。
     */

    private static int avgMatrix(int[][] matrix) {
        int r = 0;
        int g = 0;
        int b = 0;

        for (int i = 0; i < matrix.length; i++) {
            int[] x = matrix[i];
            for (int j = 0; j < x.length; j++) {
                if (j == 1) {
                    continue;
                }
                Color c = new Color(x[j]);
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
            }
        }
        return new Color(r / 8, g / 8, b / 8).getRGB();
    }
}
