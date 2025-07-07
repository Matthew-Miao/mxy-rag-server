package com.mxy.ai.rag.config.session;

import lombok.Data;

/**
 * 用户会话信息类
 * 用于在请求处理过程中存储当前用户的会话信息
 * 
 * @author Mxy
 */
@Data
public class UserSession {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;

    public UserSession() {
    }
    
    public UserSession(String userId, String username) {
        this();
        this.userId = userId;
        this.username = username;
    }
}