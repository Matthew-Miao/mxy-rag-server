package com.mxy.ai.rag.config.session;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户会话持有者
 * 使用阿里TTL（TransmittableThreadLocal）存储当前线程的用户会话信息
 * 支持线程池环境下的上下文传递，确保线程安全
 * 
 * @author Mxy
 */
public class UserSessionHolder {
    
    private static final Logger logger = LoggerFactory.getLogger(UserSessionHolder.class);
    
    /**
     * TransmittableThreadLocal存储用户会话信息
     * 相比ThreadLocal，TTL支持线程池环境下的上下文传递
     */
    private static final TransmittableThreadLocal<UserSession> USER_SESSION_THREAD_LOCAL = new TransmittableThreadLocal<>();
    
    /**
     * 设置当前线程的用户会话
     * 
     * @param userSession 用户会话信息
     */
    public static void setUserSession(UserSession userSession) {
        if (userSession != null) {
            logger.debug("设置用户会话: {}", userSession);
            USER_SESSION_THREAD_LOCAL.set(userSession);
        } else {
            logger.warn("尝试设置空的用户会话");
        }
    }
    
    /**
     * 获取当前线程的用户会话
     * 
     * @return 用户会话信息，如果未设置则返回null
     */
    public static UserSession getUserSession() {
        return USER_SESSION_THREAD_LOCAL.get();
    }
    
    /**
     * 获取当前用户ID
     * 
     * @return 用户ID，如果未设置会话则返回null
     */
    public static String getCurrentUserId() {
        UserSession session = getUserSession();
        return session != null ? session.getUserId() : null;
    }
    
    /**
     * 获取当前用户名
     * 
     * @return 用户名，如果未设置会话则返回null
     */
    public static String getCurrentUsername() {
        UserSession session = getUserSession();
        return session != null ? session.getUsername() : null;
    }
    
    /**
     * 清除当前线程的用户会话
     * 重要：在请求处理完成后必须调用此方法，避免内存泄漏
     */
    public static void clearUserSession() {
        UserSession session = getUserSession();
        if (session != null) {
            logger.debug("清除用户会话: userId={}", session.getUserId());
        }
        USER_SESSION_THREAD_LOCAL.remove();
    }
}