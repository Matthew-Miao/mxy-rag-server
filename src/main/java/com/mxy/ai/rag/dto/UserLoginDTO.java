package com.mxy.ai.rag.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录数据传输对象
 * 用于在service层传递用户登录数据
 *
 * @author Mxy
 */
@Data
public class UserLoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}