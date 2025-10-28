package leaf.common.pic;

import net.ifok.image.image4j.codec.ico.ICOEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片转换类
 * 依赖 image4j-0.7.2.jar
 */
public class ImgConvert {
    /**
     * 图片转ico
     * @param path 图片路径
     * @param outPath 输出ico路径
     * @return true转换成功
     */
    public static boolean imgToIco(String path,String outPath) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(path));
            System.out.println(bufferedImage);
            if(bufferedImage == null) return false;
            List<BufferedImage> icons = new ArrayList<>();
            icons.add(getScaledInstance(bufferedImage, 16, 16));
            icons.add(getScaledInstance(bufferedImage, 32, 32));
            icons.add(getScaledInstance(bufferedImage, 64, 64));
            icons.add(getScaledInstance(bufferedImage, 128, 128));
            ICOEncoder.write(icons,new File(outPath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取缩放后的BufferedImage的对象
     * @param bufferedImage BufferedImage对象
     * @param toWidth 需要缩放的宽度
     * @param toHeight 需要缩放的高度
     * @return 缩放后的BufferedImage的对象
     */
    private static BufferedImage getScaledInstance(BufferedImage bufferedImage,int toWidth,int toHeight){
        Image scaledInstance = bufferedImage.getScaledInstance(toWidth, toHeight, Image.SCALE_SMOOTH);
        BufferedImage newBufferedImage = new BufferedImage(toWidth, toHeight, BufferedImage.TYPE_INT_RGB);
        newBufferedImage.createGraphics().drawImage(scaledInstance, 0, 0, Color.WHITE, null);
        return newBufferedImage;
    }

}
