package leaf.common.pic;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import leaf.common.Log;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * 二维码操作类
 * 依赖 core-x.x.x.jar javase-x.x.x.jar
 */
public class QRCode {
    /**
     * 生成二维码图片
     * @param content 扫描二维码内容
     * @param width 宽度 推荐300到1000
     * @param height 高度
     * @return 生成的二维码图片，BufferedImage 类型
     */
    public static BufferedImage createQRCode(String content,int width,int height) {
        try {
            Hashtable hints = new Hashtable();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            width = bitMatrix.getWidth();
            height = bitMatrix.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return image;
        } catch(Exception e) {
            Log.write("Error",Log.getException(e));
            return null;
        }
    }

    /**
     * 生成二维码图片
     * @param content 扫描二维码内容
     * @param width 宽度 推荐300到1000
     * @param height 高度
     * @param left 左边距离
     * @param top 上边距离
     * @return 生成的二维码图片，BufferedImage 类型
     */
    public static BufferedImage createQRCode(String content,int width,int height,int left,int top) {
        BufferedImage image = createQRCode(content,width,height);
        // 创建一个新的画布，并设置背景颜色为白色
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        // 将二维码绘制到画布的相应位置
        graphics.drawImage(image, left, top, null);
        return canvas;
    }

    /**
     * 生成二维码图片
     * @param content 扫描二维码内容
     * @param path 生成二维码路径
     * @param size 大小 推荐300到1000
     * @return true生成成功
     */
    public static boolean createQRCode(String content,String path,int size) {
        try {
            File file = new File(path);
            if(!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            BufferedImage image = createQRCode(content,size,size);
            if(image == null) {
                return false;
            }
            ImageIO.write(image, "JPG", file);
            return true;
        } catch(Exception e) {
            Log.write("Error",Log.getException(e));
            return false;
        }
    }

    /**
     * 生成二维码图片
     * @param content 扫描二维码内容
     * @param path 生成二维码路径
     * @param size 大小 推荐300到1000
     * @param logoPath logo路径
     * @param logoSize logo大小
     * @return true生成成功
     */
    public static boolean createQRCode(String content,String path,int size,String logoPath,int logoSize) {
        try {
            BufferedImage image = createQRCode(content,size,size);
            Graphics2D graphics = image.createGraphics();
            graphics.drawImage(createQRCode(content,size,size), 0, 0, size, size, null);//绘制二维码
            //计算Logo的位置
            int logoStart = (size - logoSize) / 2;
            //绘制背景
            int border = size / 100;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//开启抗锯齿处理
            graphics.setColor(Color.WHITE);
            graphics.fillRect(logoStart - border, logoStart - border, logoSize + border * 2, logoSize + border * 2); // 绘制边框
            graphics.setColor(Color.WHITE);
            graphics.fillRect(logoStart, logoStart, logoSize, logoSize);
            //加载Logo图片
            File logoFile = new File(logoPath);
            Image logoImage = ImageIO.read(logoFile);
            //绘制带Logo的二维码
            graphics.drawImage(logoImage, logoStart, logoStart, logoSize, logoSize, null);//绘制logo

            graphics.dispose();//释放绘图对象
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            ImageIO.write(image, "JPG", file);
            return true;
        } catch (Exception e) {
            Log.write("Error",Log.getException(e));
            return false;
        }
    }

    /**
     * 生成二维码图片
     * @param content 扫描二维码内容
     * @param path 生成二维码路径
     * @param width 宽度 推荐500
     * @param logoPath logo路径
     * @param logoSize logo大小 推荐120
     * @param desc 描述文字
     * @return true生成成功
     */
    public static boolean createQRCode(String content,String path,int width,String logoPath,int logoSize,String desc) {
        try {
            int height = width / 10 + width;
            BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.drawImage(createQRCode(content,width,height,0,(width - height) / 2), 0, 0, width, height, null);//绘制二维码
            //绘制logo
            if(logoPath != null) {
                //计算Logo的位置
                int logoStart = (width - logoSize) / 2;
                //绘制背景
                int border = width / 100;
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//开启抗锯齿处理
                graphics.setColor(Color.WHITE);
                graphics.fillRect(logoStart - border, logoStart - border, logoSize + border * 2, logoSize + border * 2); // 绘制边框
                graphics.setColor(Color.WHITE);
                graphics.fillRect(logoStart, logoStart, logoSize, logoSize);
                //加载Logo图片
                File logoFile = new File(logoPath);
                Image logoImage = ImageIO.read(logoFile);
                graphics.drawImage(logoImage, logoStart, logoStart, logoSize, logoSize, null);//绘制logo
            }
            //绘制描述文字
            if(desc != null) {
                //计算文字的水平居中位置
                Font font = new Font("微软雅黑", Font.BOLD, width / 20);
                int textWidth = graphics.getFontMetrics(font).stringWidth(desc);
                int textX = (width - textWidth) / 2;

                graphics.setFont(font);
                graphics.setColor(Color.BLACK);
                graphics.drawString(desc, textX, (height - width) / 3 + width);
            }
            graphics.dispose();//释放绘图对象
            File file = new File(path);
            ImageIO.write(image, "JPG", file);
            return true;
        } catch (Exception e) {
            Log.write("Error",Log.getException(e));
            return false;
        }
    }

    /**
     * 解析二维码
     * @param path 二维码路径
     * @return 二维码内容
     */
    public static String parseQRCode(String path) {
        BufferedImage image;
        try {
            image = ImageIO.read(new File(path));
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            Binarizer binarizer = new HybridBinarizer(source);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            Map<DecodeHintType, Object> hints = new HashMap<>(1);
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");//设置编码格式
            Result result = new MultiFormatReader().decode(binaryBitmap, hints);//解码
            return result.getText();
        } catch(IOException e) {
            Log.write("Error",Log.getException(e));
            return null;
        } catch (NotFoundException e) {
            //这里判断如果识别不了带LOGO的图片，重新添加上一个属性
            try {
                image = ImageIO.read(new File(path));
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                Binarizer binarizer = new HybridBinarizer(source);
                BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
                Map<DecodeHintType, Object> hints = new HashMap<>(3);
                hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
                hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);//设置优化精度
                hints.put(DecodeHintType.PURE_BARCODE, Boolean.TYPE);//设置复杂模式开启（我使用这种方式就可以识别微信的二维码了）
                Result result = new MultiFormatReader().decode(binaryBitmap, hints);//解码
                return result.getText();
            } catch(NotFoundException | IOException e1) {
                Log.write("Error",Log.getException(e1));
                return null;
            }
        }
    }
}
