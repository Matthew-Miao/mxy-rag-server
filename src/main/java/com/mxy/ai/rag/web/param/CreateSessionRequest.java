package com.mxy.ai.rag.web.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Size;

/**
 * 创建会话请求参数
 * 用于接收创建新会话的请求数据
 */
@Data
@Schema(description = "创建会话请求参数")
public class CreateSessionRequest {

    /**
     * 会话标题（可选，如果不提供将自动生成）
     */
    @Schema(description = "会话标题", example = "我的聊天会话", maxLength = 200)
    @Size(max = 200, message = "会话标题长度不能超过200个字符")
    private String title;

    /**
     * 会话描述（可选）
     */
    @Schema(description = "会话描述", example = "这是一个关于技术讨论的会话", maxLength = 500)
    @Size(max = 500, message = "会话描述长度不能超过500个字符")
    private String description;

    /**
     * 用户ID（必填）
     */
    @Schema(description = "用户ID", example = "user123")
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    
    /**
     * Spring AI 对话ID（可选，如果不提供将自动生成）
     */
    @Schema(description = "Spring AI 对话ID", example = "conv_123456")
    private String conversationId;
    
    /**
     * 最大上下文消息数（可选，默认20）
     */
    @Schema(description = "最大上下文消息数", example = "20")
    private Integer maxContextMessages = 20;
    
    /**
     * 上下文策略（可选，默认sliding_window）
     */
    @Schema(description = "上下文策略", example = "sliding_window", allowableValues = {"sliding_window", "summary", "hybrid"})
    private String contextStrategy = "sliding_window";
    
    /**
     * 记忆保留时间（小时，可选，默认168小时即7天）
     */
    @Schema(description = "记忆保留时间（小时）", example = "168")
    private Integer memoryRetentionHours = 168;
}