package com.mxy.ai.rag.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息视图对象
 * 用于返回用户信息给前端
 *
 * @author Mxy
 */
@Data
@Schema(description = "用户信息")
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "testuser")
    private String username;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime gmtModified;
}