package com.mxy.ai.rag.dto;

import lombok.Data;

/**
 * 创建会话数据传输对象
 * 用于在service层传递创建会话的数据
 */
@Data
public class CreateSessionDTO {

    /**
     * 会话标题（可选，如果不提供将自动生成）
     */
    private String title;

    /**
     * 会话描述（可选）
     */
    private String description;

    /**
     * 用户ID（必填）
     */
    private String userId;

    /**
     * Spring AI 对话ID（可选，如果不提供将自动生成）
     */
    private String conversationId;

    /**
     * 最大上下文消息数（可选，默认20）
     */
    private Integer maxContextMessages;

    /**
     * 上下文策略（可选，默认sliding_window）
     */
    private String contextStrategy;

    /**
     * 记忆保留时间（小时）（可选，默认168小时即7天）
     */
    private Integer memoryRetentionHours;
}