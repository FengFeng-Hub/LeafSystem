package leaf.common.net;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * servlet操作类
 * 依赖 javax.servlet-api
 */
public class Servlet {
    /**
     * 获取所有cookie信息
     * @param req Request
     * @return 获取到的cookie信息，Map类型
     */
    public static Map<String, String> getCookies(HttpServletRequest req) {
        Map<String, String> map = new LinkedHashMap<>();
        Cookie[] cookies = req.getCookies();

        if(cookies == null) {
            return map;
        }

        for(Cookie cookie:cookies) {
            map.put(cookie.getName(),cookie.getValue());
        }

        return map;
    }

    /**
     * 通过属性名获取cookie
     * @param req Request对象
     * @param name 属性名
     * @return cookie属性值
     */
    public static String getCookie(HttpServletRequest req,String name) {
        Cookie[] cookies = req.getCookies();
        if(cookies == null) return null;
        for(Cookie cookie:cookies) {
            if(cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 设置cookie
     * @param resp Response对象
     * @param name 属性名
     * @param val 属性值
     */
    public static void setCookie(HttpServletResponse resp, String name, String val) {
        Cookie cookie = new Cookie(name,val);
        cookie.setPath("/"); //项目所有目录均有效
        resp.addCookie(cookie);
    }

    /**
     * 设置cookie并设置cookie有效时间
     * @param resp Response对象
     * @param name 属性名
     * @param val 属性值
     * @param time 有效时间（单位：秒）
     */
    public static void setCookie(HttpServletResponse resp, String name, String val, int time) {
        Cookie cookie = new Cookie(name,val);
        cookie.setPath("/"); //项目所有目录均有效
        cookie.setMaxAge(time);
        resp.addCookie(cookie);
    }

    /**
     * 清除所有cookie
     * @param req Request对象
     * @param resp Response对象
     */
    public static void clearCookie(HttpServletRequest req,HttpServletResponse resp) {
        Cookie[] cookies = req.getCookies();
        Cookie cook;
        for(Cookie cookie:cookies) {
            cook = new Cookie(cookie.getName(),null);
            cook.setPath("/"); //项目所有目录均有效，这句很关键，否则不敢保证删除
            cook.setMaxAge(0);//删除
            resp.addCookie(cook);//重新写入，将覆盖之前的
        }
    }

    /**
     * 清除cookie
     * @param resp Response对象
     * @param name 属性名
     */
    public static void clearCookie(HttpServletResponse resp,String name) {
        Cookie cookie = new Cookie(name,null);
        cookie.setPath("/"); //项目所有目录均有效，这句很关键，否则不敢保证删除
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
    }

    /**
     * 获取所有session信息
     * @param req Request对象
     * @return 获取到的session信息，Map类型
     */
    public static Map<String, Object> getSessions(HttpServletRequest req) {
        Map<String, Object> map = new LinkedHashMap<>();
        HttpSession session = req.getSession();//获取session
        Enumeration<?> enumeration = session.getAttributeNames();// 获取session中所有的键值
        // 遍历enumeration
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement().toString();// 获取session的属性名称
            map.put(name,session.getAttribute(name));
        }
        return map;
    }

    /**
     * 通过属性名获取session
     * @param req Request对象
     * @param name 属性名
     * @return 属性值，Object类型
     */
    public static Object getSession(HttpServletRequest req,String name) {
        return req.getSession().getAttribute(name);
    }

    /**
     * 设置session
     * @param req Request对象
     * @param name 属性名
     * @param val 属性值
     */
    public static void setSession(HttpServletRequest req,String name,String val) {
        HttpSession session = req.getSession();
        session.setAttribute(name,val);
    }

    /**
     * 清除全部session信息
     * @param req Request对象
     */
    public static void clearSession(HttpServletRequest req) {
        req.getSession().invalidate();
    }

    /**
     * 清除session
     * @param req Request对象
     * @param name 属性名
     */
    public static void clearSession(HttpServletRequest req,String name) {
        req.getSession().removeAttribute(name);
    }

    /**
     * 获取客户端IP
     * @param request Request对象
     * @return 获取到的客户端IP
     */
    public static String getIpAdrress(HttpServletRequest request) {
        String ip = null;
        //X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");
//        System.out.println("====ipAddresses:"+ipAddresses);
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            //打印所有头信息
            String s = headerNames.nextElement();
            String header = request.getHeader(s);
//            System.out.println(s+"::::"+header);
        }

//        System.out.println("headerNames:"+headerNames);
//        System.out.println("RemoteHost:"+request.getRemoteHost());
//        System.out.println("RemoteAddr:"+request.getRemoteAddr());
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("Proxy-Client-IP");//Proxy-Client-IP：apache 服务代理
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");//WL-Proxy-Client-IP：weblogic 服务代理
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");//HTTP_CLIENT_IP：有些代理服务器
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("X-Real-IP");//X-Real-IP：nginx服务代理
        }

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }

        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
}
