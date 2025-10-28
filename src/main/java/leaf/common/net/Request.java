package leaf.common.net;

import leaf.common.Log;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 请求操作类
 */
public class Request {
    /**
     * Boundary分界线
     */
    private static final String BOUNDARY = "0668LyfCommonRequestBoundary8660";

    /**
     * 常用content-type类型枚举
     */
    public enum ContentType {
        /**
         * application/x-www-form-urlencoded form表单数据被编码为key/value格式发送到服务器（表单默认的提交数据的格式）
         */
        Application_x_www_form_urlencoded,
        /**
         * application/json JSON数据格式
         */
        Application_json,
        /**
         * multipart/form-data 表单数据有多部分构成，既有文本数据，又有文件等二进制数据，一般用于文件上传
         */
        Multipart_form_data
    }

    /**
     * url编码
     * @param str 需要编码的字符串
     * @return 编码后的字符串
     */
    public static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str,"utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.write("Error",Log.getException(e));
            return null;
        }
    }

    /**
     * url解码
     * @param str 需要解码的字符串
     * @return 解码后的字符串
     */
    public static String urlDecode(String str) {
        try {
            return URLDecoder.decode(str,"utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.write("Error",Log.getException(e));
            return null;
        }
    }

    /**
     * 判断IP地址是不是内网
     * @param ipAddress IPv4地址
     * @return true为内网IP
     */
    public static boolean isInternalIp(String ipAddress) {
        String[] strArray = ipAddress.split("\\.");

        //不是IPv4
        if(strArray.length != 4) {
            return false;
        }

        //String转int
        int[] address = new int[4];

        for(int i = 0;i < strArray.length;i++){
            address[i] = Integer.parseInt(strArray[i]);
            if(address[i] < 0 || address[i] > 255) return false;
        }

        switch (address[0]) {
            case 10://10.x.x.x
                return true;
            case 172://172.16.0.0 ~ 172.31.255.255
                return address[1] >= 16 && address[1] <= 31;
            case 192://192.168.x.x
                return address[1] == 168;
            default:
                return false;
        }
    }

    /**
     * 发送get请求
     * @param url 请求URL
     * @return 响应结果，String类型
     */
    public static String get(String url) {
        return send(url,"GET",null,null,null,null,null);
    }

    /**
     * get请求
     * @param url 请求URL
     * @param param 请求参数
     * @return 响应结果，String类型
     */
    public static String get(String url,String param) {
        return send(url,"GET",null,null,param,null,null);
    }

    /**
     * get请求
     * @param url 请求URL
     * @param header 请求头
     * @param param 请求参数
     * @return 响应结果，String类型
     */
    public static String get(String url,Map<String,String> header,String param) {
        return send(url,"GET",null,header,param,null,null);
    }

    /**
     * post请求
     * @param url 请求URL
     * @return 响应结果，String类型
     */
    public static String post(String url) {
        return send(url,"POST",ContentType.Application_x_www_form_urlencoded,null,null,null,null);
    }

    /**
     * post请求
     * @param url 请求URL
     * @param param 请求参数
     * @return 响应结果，String类型
     */
    public static String post(String url,String param) {
        return send(url,"POST",ContentType.Application_x_www_form_urlencoded,null,param,null,null);
    }

    /**
     * post请求
     * @param url 请求URL
     * @param param 请求参数
     * @return 响应结果，String类型
     */
    public static String post(String url,Map<String,String> param) {
        return send(url,"POST",ContentType.Application_x_www_form_urlencoded,null,paramMapToStr(param),null,null);
    }

    /**
     * post请求
     * @param url 请求URL
     * @param header 请求头
     * @param param 请求参数
     * @return 响应结果，String类型
     */
    public static String post(String url,Map<String,String> header,String param) {
        return send(url,"POST",ContentType.Application_x_www_form_urlencoded,header,param,null,null);
    }

    /**
     * post请求
     * @param url 请求URL
     * @param header 请求头
     * @param param 请求参数
     * @return 响应结果，String类型
     */
    public static String post(String url,Map<String,String> header,Map<String,String> param) {
        return send(url,"POST",ContentType.Application_x_www_form_urlencoded,null,paramMapToStr(param),null,null);
    }

    /**
     * post请求（content-type=content-type）
     * @param url 请求URL
     * @param json JSON格式字符串
     * @return 响应结果，String类型
     */
    public static String postByJSON(String url,String json) {
        return send(url,"POST",ContentType.Application_json,null,json,null,null);
    }

    /**
     * post请求（content-type=multipart/form-data）
     * @param url 请求URL
     * @param param 请求参数
     * @param file 文件
     * @return 响应结果，String类型
     */
    public static String postByFormData(String url, Map<String,String> param, Map<String,byte[]> file) {
        return send(url,"POST",ContentType.Multipart_form_data, null,null,param,file);
    }

    /**
     * post请求（content-type=multipart/form-data）
     * @param url 请求URL
     * @param header 请求头
     * @param param 请求参数
     * @param file 文件
     * @return 响应结果，String类型
     */
    public static String postByFormData(String url,Map<String,String> header, Map<String,String> param, Map<String,byte[]> file) {
        return send(url,"POST",ContentType.Multipart_form_data, header,null,param,file);
    }

    /**
     * 发送请求
     * @param url 请求URL
     * @param method 请求方法
     * @param contentType content-type
     * @param header 请求头
     * @param param 请求参数（用于 application/x-www-form-urlencoded 或者 application/json）
     * @param formDataParam 请求参数（用于 multipart/form-data）
     * @param formDataFile 请求文件（用于 multipart/form-data）
     * @return 响应结果，String类型
     */
    public static String send(String url, String method, ContentType contentType, Map<String,String> header, String param, Map<String,String> formDataParam, Map<String,byte[]> formDataFile) {
        HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        DataOutputStream dos = null;
        BufferedReader br = null;

        try {
            connection = (HttpURLConnection)new URL(url).openConnection();//通过远程url连接对象打开连接
            connection.setRequestMethod(method);//设置连接请求方式
            connection.setConnectTimeout(15000);//设置连接主机服务器超时时间：15000毫秒
            connection.setReadTimeout(60000);//设置读取主机服务器返回数据超时时间：60000毫秒

            //设置请求头
            connection.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");//设置鉴权信息：Authorization: Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible;MSIE 6.0;Windows NT 5.1;SV1)");
            connection.setRequestProperty("request-source", "lyf.common.Request");

            if(header != null) {
                for(Map.Entry<String,String> entry : header.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            if("GET".equals(method.toUpperCase())) {
                connection.connect();// 发送请求
            } else if("POST".equals(method.toUpperCase())) {
                connection.setDoOutput(true);//默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
                connection.setDoInput(true);//默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无

                if(contentType == ContentType.Application_x_www_form_urlencoded || contentType == ContentType.Application_json) {
                    if(contentType == ContentType.Application_x_www_form_urlencoded) {
                        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    } else {
                        connection.setRequestProperty("Content-Type", "application/json");
                    }

                    os = connection.getOutputStream();// 通过连接对象获取一个输出流

                    if(param != null) {
                        os.write(param.getBytes());// 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
                    }

                    os.flush();
                } else if(contentType == ContentType.Multipart_form_data) {
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                    os = connection.getOutputStream();// 通过连接对象获取一个输出流
                    dos = new DataOutputStream(os);

                    if(formDataParam != null && formDataParam.size() != 0) {
                        for (Map.Entry<String,String> entry : formDataParam.entrySet()) {
                            dos.writeBytes("--" + BOUNDARY + "\r\n");
                            dos.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n");
                            dos.writeBytes("\r\n");
                            dos.writeBytes(entry.getValue() + "\r\n");
                        }
                    }

                    if(formDataFile != null && formDataFile.size() != 0) {
                        for (Map.Entry<String,byte[]> entry : formDataFile.entrySet()) {
                            dos.writeBytes("--" + BOUNDARY + "\r\n");
                            dos.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"lyf.common.request.file\"\r\n");
                            dos.writeBytes("\r\n");
                            dos.write(entry.getValue());
                            dos.writeBytes("\r\n");
                        }
                    }

                    dos.writeBytes("--" + BOUNDARY + "--" + "\r\n");
                    dos.writeBytes("\r\n");
                    dos.flush();
                    int code = connection.getResponseCode();

                    try {
                        if(code == 200) is = connection.getInputStream();
                        else is = connection.getErrorStream();
                    } catch(SSLException e){
                        Log.write("Error",Log.getException(e));
                        return null;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buff = new byte[4096];
                    int len;

                    while((len = is.read(buff)) != -1)
                        baos.write(buff, 0, len);

                    byte[] bytes = baos.toByteArray();
                    return new String(bytes);
                }
            }

            //通过连接对象获取一个输入流，向远程读取
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));//对输入流对象进行包装:charset根据工作项目组的要求来设置
                StringBuffer sbf = new StringBuffer();
                String temp;

                // 循环遍历一行一行读取数据
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }

                return sbf.toString();
            }
        } catch (IOException e) {
            Log.write("Error",Log.getException(e));
        } finally {
            try {
                if(br != null) br.close();
                if(os != null) os.close();
                if(dos != null) dos.close();
                if(is != null) is.close();
            } catch(IOException e) {
                Log.write("Error",Log.getException(e));
            }
            if(connection != null) connection.disconnect();
        }

        return null;
    }

    /**
     * 从网络Url中下载文件
     * @param url url
     * @param savePath 保存路径
     * @return true保存成功
     */
    public static boolean downLoadByUrl(String url,String savePath) {
        ByteArrayOutputStream bos = null;
        FileOutputStream fos = null;
        InputStream inputStream = null;

        try {
            URL _url = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)_url.openConnection();
            conn.setConnectTimeout(5*1000);//设置超时间为5秒
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");//防止屏蔽程序抓取而返回403错误
            inputStream = conn.getInputStream();//得到输入流
            byte[] buffer = new byte[4*1024];
            int len;
            bos = new ByteArrayOutputStream();

            while((len = inputStream.read(buffer)) != -1)
                bos.write(buffer, 0, len);

            byte[] getData = bos.toByteArray();
            File saveDir = new File(savePath).getParentFile();//文件保存位置

            if(!saveDir.exists()) saveDir.mkdir();//没有就创建该文件

            File file = new File(savePath);
            fos = new FileOutputStream(file);
            fos.write(getData);
            return true;
        } catch(Exception e) {
            Log.write("Error",Log.getException(e));
            return false;
        } finally {
            try {
                if(bos != null) bos.close();
                if(fos != null) fos.close();
                if(inputStream != null) inputStream.close();
            } catch (IOException e) {
                Log.write("Error",Log.getException(e));
            }
        }
    }

    /**
     * 参数类型map转字符串
     * @param param map类型的参数
     * @return 转换后的字符串
     */
    public static String paramMapToStr(Map<String,String> param) {
        StringBuilder paramStr = new StringBuilder();

        if(param != null && param.size() > 0) {
            for(Map.Entry<String,String> entry : param.entrySet()) {
                paramStr.append(urlEncode(entry.getKey())).append("=").append(urlEncode(entry.getValue())).append("&");
            }
            paramStr.deleteCharAt(paramStr.length() - 1);//删除最后一个字符
        }

        return paramStr.toString();
    }
}
