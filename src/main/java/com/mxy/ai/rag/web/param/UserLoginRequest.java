package com.mxy.ai.rag.web.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户登录请求参数
 * 用于接收用户登录接口的请求参数
 *
 * @author Mxy
 */
@Data
@Schema(description = "用户登录请求参数")
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "testuser")
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码", example = "password123")
    @NotBlank(message = "密码不能为空")
    private String password;
}