package com.mxy.ai.rag.web.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

/**
 * 恢复会话请求参数
 * 用于接收恢复会话的请求数据
 */
@Data
@Schema(description = "恢复会话请求参数")
public class RestoreSessionRequest {

    /**
     * 会话ID
     */
    @Schema(description = "会话ID", example = "1", required = true)
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "user123", required = true)
    @NotBlank(message = "用户ID不能为空")
    private String userId;
}