package com.mxy.ai.rag.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户修改密码数据传输对象
 * 用于在service层传递用户修改密码数据
 *
 * @author Mxy
 */
@Data
public class UserChangePasswordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 原密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认新密码
     */
    private String confirmNewPassword;
}