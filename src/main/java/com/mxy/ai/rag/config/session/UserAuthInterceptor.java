package com.mxy.ai.rag.config.session;

import com.mxy.ai.rag.datasource.dao.UsersDAO;
import com.mxy.ai.rag.datasource.entity.UsersDO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * 用户认证拦截器
 * 拦截所有请求，验证请求头中的userId，并将用户信息存储到UserSession中
 * 
 * @author Mxy
 */
@Component
public class UserAuthInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(UserAuthInterceptor.class);
    
    /**
     * 用户ID请求头名称
     */
    private static final String USER_ID_HEADER = "X-User-Id";
    
    /**
     * 备用用户ID请求头名称
     */
    private static final String USER_ID_HEADER_ALT = "userId";
    
    @Resource
    private UsersDAO usersDAO;
    
    /**
     * 请求处理前的拦截方法
     * 验证用户身份并设置用户会话
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @return 是否继续处理请求
     * @throws Exception 处理异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("拦截请求: {} {}", method, requestURI);
        
        // 跳过健康检查和静态资源等不需要认证的请求
        if (shouldSkipAuthentication(requestURI)) {
            logger.debug("跳过认证检查: {}", requestURI);
            return true;
        }
        
        // 从请求头获取用户ID
        String userId = getUserIdFromHeader(request);
        
        if (!StringUtils.hasText(userId)) {
            logger.warn("请求缺少用户ID: {} {}", method, requestURI);
            sendUnauthorizedResponse(response, "缺少用户ID");
            return false;
        }
        
        // 验证用户是否存在
        try {
            UsersDO user = usersDAO.getByUserId(userId);
            if (user == null || user.getDeleted() == 1) {
                logger.warn("用户不存在或已删除: userId={}", userId);
                sendUnauthorizedResponse(response, "用户不存在或已删除");
                return false;
            }
            
            // 创建用户会话并设置到ThreadLocal
            UserSession userSession = createUserSession(user);
            UserSessionHolder.setUserSession(userSession);
            
            logger.debug("用户认证成功: userId={}, username={}", userId, user.getUsername());
            return true;
            
        } catch (Exception e) {
            logger.error("用户认证过程中发生异常: userId={}, error={}", userId, e.getMessage(), e);
            sendUnauthorizedResponse(response, "认证失败");
            return false;
        }
    }
    
    /**
     * 请求处理后的方法
     * 更新用户最后访问时间
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @param modelAndView 模型视图
     * @throws Exception 处理异常
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 更新最后访问时间
    }
    
    /**
     * 请求完成后的清理方法
     * 清除ThreadLocal中的用户会话，防止内存泄漏
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @param ex 异常信息
     * @throws Exception 处理异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // 清除用户会话
            UserSessionHolder.clearUserSession();
        } catch (Exception e) {
            logger.error("清除用户会话时发生异常", e);
        }
    }
    
    /**
     * 从请求头获取用户ID
     * 
     * @param request 请求对象
     * @return 用户ID
     */
    private String getUserIdFromHeader(HttpServletRequest request) {
        // 优先从X-User-Id头获取
        String userId = request.getHeader(USER_ID_HEADER);
        if (StringUtils.hasText(userId)) {
            return userId.trim();
        }
        
        // 备用方案：从userId头获取
        userId = request.getHeader(USER_ID_HEADER_ALT);
        if (StringUtils.hasText(userId)) {
            return userId.trim();
        }
        
        return null;
    }
    
    /**
     * 创建用户会话对象
     * 
     * @param user 用户实体
     * @return 用户会话对象
     */
    private UserSession createUserSession(UsersDO user) {
        return new UserSession(user.getUserId(), user.getUsername());
    }
    
    /**
     * 判断是否应该跳过认证检查
     * 
     * @param requestURI 请求URI
     * @return 是否跳过
     */
    private boolean shouldSkipAuthentication(String requestURI) {
        // 健康检查接口
        if (requestURI.contains("/actuator/")) {
            return true;
        }
        
        // 静态资源
        if (requestURI.contains("/static/") || requestURI.contains("/public/")) {
            return true;
        }
        
        // Swagger文档
        if (requestURI.contains("/swagger") || requestURI.contains("/v3/api-docs")) {
            return true;
        }
        
        // 错误页面
        if (requestURI.contains("/error")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 发送未授权响应
     * 
     * @param response 响应对象
     * @param message 错误消息
     * @throws IOException IO异常
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        String jsonResponse = String.format(
            "{\"code\":%d,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
            HttpServletResponse.SC_UNAUTHORIZED,
            message,
            System.currentTimeMillis()
        );
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}