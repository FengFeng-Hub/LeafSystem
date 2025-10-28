package leaf.system.interceptor;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import leaf.system.common.SysUser;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 页面全局拦截器
 */
public class PageGlobalInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException {
//        response.setCharacterEncoding("GBK");
        String uri = request.getRequestURI();

        if(uri.startsWith("/backend")) {
            if(SysUser.getUserByToken(true) == null) {
                response.sendRedirect("/backend/login");
                return false;
            }
        } else if(uri.startsWith("/log")) {
            //查看该用户的角色是有包含获取日志的权限
            if(!ApiGlobalInterceptor.permission("lspk:ls:log:get")) {
                request.getRequestDispatcher("/error/403.html").forward(request,response);
                return false;
            }
        }
        return true;
    }
}
