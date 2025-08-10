package com.auth.interceptor;

import com.auth.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Autowired
    public JwtAuthenticationInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取Token和UserID
        String token = jwtUtils.getTokenFromRequest(request);
        String userIdHeader = request.getHeader("X-User-Id");

        // 验证Token和UserID是否存在
        if (token == null || userIdHeader == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "缺少认证信息");
            return false;
        }

        // 验证Token有效性
        if (!jwtUtils.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "无效的Token");
            return false;
        }

        // 验证Token中的UserID与请求头是否匹配
        Long userIdFromToken = jwtUtils.getUserIdFromToken(token);
        if (!userIdFromToken.toString().equals(userIdHeader)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "用户ID不匹配");
            return false;
        }

        // 验证通过，将UserID添加到请求属性中
        request.setAttribute("userId", userIdFromToken);
        return true;
    }
}