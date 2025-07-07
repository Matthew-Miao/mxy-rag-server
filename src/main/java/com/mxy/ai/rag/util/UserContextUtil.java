package com.mxy.ai.rag.util;

import com.mxy.ai.rag.config.session.UserSessionHolder;

/**
 * 用户上下文工具类
 * 提供便捷的方法来获取当前用户信息和执行用户相关操作
 * 
 * @author Mxy
 */
public class UserContextUtil {
    /**
     * 获取当前用户ID
     * 
     * @return 用户ID，如果未登录则返回null
     */
    public static String getCurrentUserId() {
        return UserSessionHolder.getCurrentUserId();
    }
    
    /**
     * 获取当前用户名
     * 
     * @return 用户名，如果未登录则返回null
     */
    public static String getCurrentUsername() {
        return UserSessionHolder.getCurrentUsername();
    }
}