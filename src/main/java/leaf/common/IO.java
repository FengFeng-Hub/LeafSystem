package leaf.common;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.*;

/**
 * IO流操作类
 */
public class IO {
    /**
     * 获取文件后缀（使用split函数）
     * @param fileName 文件名
     * @return 获取到的文件后缀
     */
    public static String getSuffix(String fileName) {
        String[] split = fileName.split("\\.");
        return split[split.length-1];
    }

    /**
     * 获取文件名（使用File类）
     * @param path 文件路径
     * @return 获取到的文件名
     */
    public static String getFilename(String path) {
        return new File(path.trim()).getName();
    }

    /**
     * 创建目录
     * @param file File对象
     * @return true创建成功
     */
    public static boolean createDir(File file) {
        if(!file.exists()) return file.getParentFile().mkdirs();
        return false;
    }

    /**
     * 创建文件
     * @param file File对象
     * @return true创建成功
     */
    public static boolean createFile(File file) {
        try {
            if (!file.getParentFile().exists()) {
                if(!file.getParentFile().mkdirs()) return false;
            }

            if(!file.exists()) {
                return file.createNewFile();
            }
        } catch(IOException e) {
            Log.write("Error",Log.getException(e));
        }
        return false;
    }

    /**
     * 查看指定路径下的文件夹和文件
     * @param path 文件路径
     * @return Map类型
     * {
     *     "folder": [文件夹集合],
     *     "file": [文件集合]
     * }
     */
    public static Map<String,List<String>> viewFolderFile(String path) {
        File file = new File(path);

        if(file.isDirectory()) {
            Map<String,List<String>> map = new HashMap<>();
            map.put("folder", new ArrayList<>());
            map.put("file", new ArrayList<>());
            String[] list = file.list();

            for(String s : list) {
                File fileSub = new File(path + "\\" + s);
                if (fileSub.isDirectory()) {
                    map.get("folder").add(s);
                } else {
                    map.get("file").add(s);
                }
            }

            return map;
        }

        return null;
    }

    /**
     * 文件转byte数组
     * @param path 文件路径
     * @return 转换后的byte数组
     */
    public static byte[] fileToBytes(String path) {
        byte[] buffer = null;
        ByteArrayOutputStream bos = null;
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(path);
            bos = new ByteArrayOutputStream();
            int n;
            byte[] b = new byte[1024];

            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }

            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            Log.write("Error",Log.getException(e));
        }

        return buffer;
    }

    /**
     * 字节写入到指定文件
     * @param bytes 字节数组
     * @param path 文件路径
     * @return true写入成功
     */
    public static boolean bytesToFile(byte[] bytes,String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(bytes);
            return true;
        } catch (IOException e) {
            Log.write("Error",Log.getException(e));
            return false;
        }
    }

    /**
     * byte数组转base64字符串
     * @param bytes byte数组
     * @return 转换后的base64字符串
     */
    public static String bytesToBase64(byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes));
    }

    /**
     * byte数组转BufferedImage
     * @param bytes byte数组
     * @return 转换后的BufferedImage
     */
    public static BufferedImage bytesToBufferedImage(byte[] bytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 字符串转base64字符串
     * @param str 字符串
     * @return 转换后的base64字符串
     */
    public static String strToBase64(String str) {
        return new String(Base64.getEncoder().encode(str.getBytes()));
    }

    /**
     * base64转byte数组
     * @param str base64字符串
     * @return 转换后的byte数组
     */
    public static byte[] base64ToBytes(String str) {
        return Base64.getDecoder().decode(str.getBytes());
    }

    /**
     * base64转字符串
     * @param str base64字符串
     * @return 转换后的字符串
     */
    public static String base64ToStr(String str) {
        return new String(Base64.getDecoder().decode(str.getBytes()));
    }

    /**
     * BufferedImage转byte数组
     * @param bufferedImage BufferedImage
     * @return 转换后的byte数组
     */
    public static byte[] bufferedImageToBytes(BufferedImage bufferedImage) {
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage,"png",bao);
            return bao.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 修改图片像素大小
     * @param path 路径
     * @param toPath 目标路径
     * @param width 宽度（像素）
     * @param height 高度（像素）
     * @return true成功
     */
    public static boolean resizeImg(String path, String toPath, int width, int height) {
        try {
            //通过url获取BufferedImage图像缓冲区
            BufferedImage image = ImageIO.read(new FileInputStream(path));
            Image resultingImage = image.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
            BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
            ImageIO.write(outputImage, getSuffix(toPath), new File(toPath));//文件不存在会自动创建文件保存，文件存在会覆盖原文件保存
            return true;
        } catch (IOException e) {
            Log.write("Error",Log.getException(e));
            return false;
        }
    }

    /**
     * 对图片裁剪
     * @param path 读取源图片路径
     * @param toPath 写入图片路径
     * @param x 剪切起始点x坐标
     * @param y 剪切起始点y坐标
     * @param width 剪切宽度
     * @param height 剪切高度
     * @return true成功
     */
    public static boolean cropImg(String path,String toPath,int x,int y,int width,int height) {
        FileInputStream fis = null;
        ImageInputStream iis =null;

        try{
            //读取图片文件
            fis = new FileInputStream(path);
            Iterator it = ImageIO.getImageReadersByFormatName(getSuffix(path));
            ImageReader reader = (ImageReader) it.next();
            //获取图片流
            iis = ImageIO.createImageInputStream(fis);
            reader.setInput(iis,true) ;
            ImageReadParam param = reader.getDefaultReadParam();
            //定义一个矩形
            Rectangle rect = new Rectangle(x, y, width, height);
            //提供一个 BufferedImage，将其用作解码像素数据的目标。
            param.setSourceRegion(rect);
            BufferedImage bi = reader.read(0,param);
            //保存新图片
            ImageIO.write(bi, getSuffix(toPath), new File(toPath));
            return true;
        } catch(IOException e) {
            Log.write("Error",Log.getException(e));
            return false;
        } finally {
            if(fis != null) {
                try { fis.close(); } catch (IOException e) { Log.write("Error",Log.getException(e)); }
            }

            if(iis != null) {
                try { iis.close(); } catch (IOException e) { Log.write("Error",Log.getException(e)); }
            }
        }
    }

    /**
     * 在源图片上设置水印文字
     * @param path 源图片路径
     * @param toPath 写入图片路径
     * @param alpha	透明度（0 小于 alpha 小于 1）
     * @param x	水印显示起始的x坐标
     * @param y	水印显示起始的y坐标
     * @param words 输入显示在图片上的文字
     * @param font 字体（例如：宋体）
     * @param fontStyle	字体格式(例如：普通样式--Font.PLAIN、粗体--Font.BOLD )
     * @param fontSize 字体大小
     * @param color	字体颜色(例如：黑色--Color.BLACK)
     * @return true成功
     */
    public static boolean watermarkWords(String path,String toPath,float alpha,int x,int y,String words,String font,int fontStyle,int fontSize,Color color) {
        FileOutputStream fos = null;

        try {
            BufferedImage image = ImageIO.read(new File(path));
            //创建java2D对象
            Graphics2D g2d = image.createGraphics();
            //用源图像填充背景
            g2d.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null, null);
            //设置透明度
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(ac);
            //设置文字字体名称、样式、大小
            g2d.setFont(new Font(font, fontStyle, fontSize));
            g2d.setColor(color);//设置字体颜色
            g2d.drawString(words, x, y); //输入水印文字及其起始x、y坐标
            g2d.dispose();
            fos=new FileOutputStream(toPath);
            ImageIO.write(image, getSuffix(toPath), fos);
            return true;
        } catch (Exception e) {
            Log.write("Error",Log.getException(e));
            return false;
        } finally {
            if(fos!=null){
                try { fos.close(); } catch (IOException e) { Log.write("Error",Log.getException(e)); }
            }
        }
    }

    /**
     * 在源图像上设置图片水印 当alpha==1时文字不透明（和在图片上直接输入文字效果一样）
     * @param path 源图片路径
     * @param toPath 图像写入路径
     * @param alpha	透明度
     * @param x	水印图片的起始x坐标
     * @param y	水印图片的起始y坐标
     * @param ImgPath 水印图片路径
     * @param width	水印图片的宽度
     * @param height 水印图片的高度
     * @return true成功
     */
    public static boolean watermarkImg(String path,String toPath,float alpha,int x,int y,String ImgPath,int width,int height) {
        FileOutputStream fos = null;

        try {
            BufferedImage image = ImageIO.read(new File(path));
            //创建java2D对象
            Graphics2D g2d = image.createGraphics();
            //用源图像填充背景
            g2d.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null, null);
            //设置透明度
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(ac);
            //设置水印图片的起始x/y坐标、宽度、高度
            BufferedImage appendImage = ImageIO.read(new File(ImgPath));
            g2d.drawImage(appendImage, x, y, width, height, null, null);
            g2d.dispose();
            fos=new FileOutputStream(toPath);
            ImageIO.write(image, getSuffix(ImgPath), fos);
            return true;
        } catch (Exception e) {
            Log.write("Error",Log.getException(e));
            return false;
        } finally {
            if(fos!=null){
                try { fos.close(); } catch (IOException e) { Log.write("Error",Log.getException(e)); }
            }
        }
    }
}
