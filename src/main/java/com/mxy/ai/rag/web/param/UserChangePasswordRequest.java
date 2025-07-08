package com.mxy.ai.rag.web.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户修改密码请求参数
 * 用于接收用户修改密码接口的请求参数
 *
 * @author Mxy
 */
@Data
@Schema(description = "用户修改密码请求参数")
public class UserChangePasswordRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 原密码
     */
    @Schema(description = "原密码", example = "oldpassword123")
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @Schema(description = "新密码", example = "newpassword123")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    private String newPassword;

    /**
     * 确认新密码
     */
    @Schema(description = "确认新密码", example = "newpassword123")
    @NotBlank(message = "确认新密码不能为空")
    private String confirmNewPassword;
}