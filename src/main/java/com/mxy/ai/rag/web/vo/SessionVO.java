package com.mxy.ai.rag.web.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 会话信息响应对象
 * 用于返回会话的详细信息
 */
@Data
@Schema(description = "会话信息响应对象")
public class SessionVO {

    /**
     * 会话ID
     */
    @Schema(description = "会话ID", example = "1")
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "user123")
    private String userId;

    /**
     * Spring AI 对话ID
     */
    @Schema(description = "Spring AI 对话ID", example = "conv_123456")
    private String conversationId;

    /**
     * 会话标题
     */
    @Schema(description = "会话标题", example = "我的聊天会话")
    private String title;

    /**
     * 会话描述
     */
    @Schema(description = "会话描述", example = "这是一个关于技术讨论的会话")
    private String description;

    /**
     * 消息数量
     */
    @Schema(description = "消息数量", example = "10")
    private Integer messageCount;

    /**
     * Token总数
     */
    @Schema(description = "Token总数", example = "1500")
    private Integer totalTokens;

    /**
     * 会话状态
     */
    @Schema(description = "会话状态", example = "active", allowableValues = {"active", "archived", "deleted"})
    private String status;

    /**
     * 最大上下文消息数
     */
    @Schema(description = "最大上下文消息数", example = "20")
    private Integer maxContextMessages;

    /**
     * 上下文策略
     */
    @Schema(description = "上下文策略", example = "sliding_window", allowableValues = {"sliding_window", "summary", "hybrid"})
    private String contextStrategy;

    /**
     * 记忆保留时间（小时）
     */
    @Schema(description = "记忆保留时间（小时）", example = "168")
    private Integer memoryRetentionHours;

    /**
     * 最后活动时间
     */
    @Schema(description = "最后活动时间", example = "2024-01-01T11:45:00")
    private LocalDateTime lastActivityTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间", example = "2024-01-01T12:00:00")
    private LocalDateTime gmtModified;

    /**
     * 最后一条消息内容（用于列表展示）
     */
    @Schema(description = "最后一条消息内容", example = "你好，有什么可以帮助你的吗？")
    private String lastMessage;

    /**
     * 最后一条消息时间
     */
    @Schema(description = "最后一条消息时间", example = "2024-01-01T11:30:00")
    private LocalDateTime lastMessageTime;
}