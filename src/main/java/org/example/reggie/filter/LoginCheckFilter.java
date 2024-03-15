package org.example.reggie.filter;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.BaseContext;
import org.example.reggie.common.R;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否登录
 * 该过滤器只负责检查是否放行
 * 跳转操作在前端完成
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    //用于路径匹配，使之支持通配符 /**
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        //不需要拦截的请求
        // /employee/page这个请求不知道是什么，目前不加入放行队列会不断回退
        String[] goUrls = new String[]{"/employee/login",
                "/backend/**","/employee/loginOut",
                "/front/**", "/common/**"};

        //1.获取本次请求的URI
        String uri = request.getRequestURI();
        log.info("1.当前请求的URI为：{}", uri);

        //2.判断是否需要拦截
        boolean check = check(goUrls, uri);
        //若是不需拦截的请求，则放行
        if(check){
            log.info("2.不需要拦截的请求，放行");
            filterChain.doFilter(request, response);
            return;
        }
        //若已经登录过，则放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("3.已经登录过，放行，用户id为：{}", request.getSession().getAttribute("employee"));

            //使用BaseContext工具类中的setCurrentUserId方法在线程内共享当前用户id
            Long employeeId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentUserId(employeeId);

            log.info("LoginCheckFilter-doFilter 线程id为：{}", Thread.currentThread().getId());

            filterChain.doFilter(request, response);
            return;
        }
        //不属于上述情况，则跳转到登录页面，通过输出流的方式向客户端页面响应数据
        //前端收到 msg = “NOTLOGIN” 后执行跳转
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        log.info("4.未登录，跳转到登录页面");
        return;
    }

    /**
     * 判断当前请求是否需要登录
     * @param goUrls
     * @param requestURI
     * @return
     */
    public boolean check(String[] goUrls, String requestURI){
        for(String goUrl : goUrls){
            if(PATH_MATCHER.match(goUrl, requestURI)){
                return true;
            }
        }
        return false;
    }
}
