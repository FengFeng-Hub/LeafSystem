package leaf.system.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import leaf.common.Log;
import leaf.common.net.Servlet;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.Map;

/**
 * http协议
 */
public class Http {
    /**
     * 响应写入
     * @param status 响应状态码
     * @param content 响应内容
     */
    public static void write(int status, Object content) {
        HttpServletResponse response = response();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        try {
            response.getWriter().println(content);
        } catch (IOException e) {
            Log.write("Error_system",Log.getException(e));
        }
    }

    /**
     * 获取 HttpServletRequest 对象
     * @return HttpServletRequest对象
     */
    public static HttpServletRequest request() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    /**
     * 获取 HttpServletRequest 对象
     * @return HttpServletRequest对象
     */
    public static HttpServletResponse response() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    /**
     * 获取请求参数
     * @param key 参数名
     * @return 参数值
     */
    public static String param(String key) {
        return param(key, "");
    }

    /**
     * 获取请求参数
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static String param(String key,String defaultValue) {
        key = request().getParameter(key);

        if(key == null) {
            return defaultValue;
        }
        return key;
    }

    /**
     * 获取请求参数
     * @param key 参数名
     * @return 参数值
     */
    public static String[] params(String key) {
        String[] arr = request().getParameterValues(key);

        if(arr == null) {
            return new String[0];
        }
        return arr;
    }

    /**
     * 获取请求文件（multipart）
     * @param key 参数名
     * @return 参数值
     */
    public static MultipartFile multipart(String key) {
        try {
            return ((MultipartHttpServletRequest) request()).getFile(key);
        } catch (Exception e) {
            return  null;
        }
    }

    /**
     * 获取请求文件（part）
     * @param key 参数名
     * @return 参数值
     */
    public static Part part(String key) {
        try {
            return request().getPart(key);
        } catch (Exception e) {
            return  null;
        }
    }

    /**
     * 获取所有cookie信息
     * @return map
     */
    public static Map<String,String> getCookies() {
        return Servlet.getCookies(request());
    }

    /**
     * 获取cookie
     * @param name name
     * @return str
     */
    public static String getCookie(String name) {
        return Servlet.getCookie(request(),name);
    }

    /**
     * 设置cookie
     * @param name name
     * @param val value
     */
    public static void setCookie(String name,String val) {
        Servlet.setCookie(response(),name,val);
    }

    /**
     * 设置cookie并设置cookie时间
     * @param name name
     * @param val value
     * @param time 时间（单位：秒）
     */
    public static void setCookie(String name, String val, int time) {
        Servlet.setCookie(response(),name,val,time);
    }

    /**
     * 清除所有cookie
     */
    public static void clearCookie() {
        Servlet.clearCookie(request(),response());
    }

    /**
     * 清除cookie
     * @param name name
     */
    public static void clearCookie(String name) {
        Servlet.clearCookie(response(),name);
    }

    /**
     * 获取所有session信息
     * @return map
     */
    public static Map<String, Object> getSessions() {
        return Servlet.getSessions(request());
    }

    /**
     * 获取session
     * @param name name
     * @return map
     */
    public static Object getSessions(String name) {
        return Servlet.getSession(request(),name);
    }

    /**
     * 设置session
     * @param name name
     * @param val value
     */
    public static void setSession(String name,String val) {
        Servlet.setSession(request(),name,val);
    }

    /**
     * 清除全部session信息
     */
    public static void clearSession() {
        Servlet.clearSession(request());
    }

    /**
     * 清除session
     * @param name name
     */
    public static void clearSession(String name) {
        Servlet.clearSession(request(),name);
    }

    /**
     * 获取客户端IP
     * @return string
     */
    public static String getIpAdrress() {
        return Servlet.getIpAdrress(request());
    }
}
