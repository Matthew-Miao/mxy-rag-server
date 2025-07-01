package com.mxy.ai.rag.web.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 更新会话描述请求参数
 * 用于接收更新会话描述的请求数据
 */
@Data
@Schema(description = "更新会话描述请求参数")
public class UpdateSessionDescriptionRequest {

    /**
     * 会话ID
     */
    @Schema(description = "会话ID", example = "1")
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "user123")
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 新的会话描述
     */
    @Schema(description = "新的会话描述", example = "这是更新后的会话描述", maxLength = 500)
    @Size(max = 500, message = "会话描述长度不能超过500个字符")
    private String description;
}