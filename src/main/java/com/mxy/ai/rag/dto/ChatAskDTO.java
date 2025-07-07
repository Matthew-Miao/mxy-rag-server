package com.mxy.ai.rag.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 智能问答请求数据传输对象
 * 用于传输智能问答接口的请求参数
 * 
 * @author Mxy
 */
@Data
public class ChatAskDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * Spring AI 对话ID（可选）
     */
    private String conversationId;

    /**
     * 用户问题
     */
    private String question;

    /**
     * 检索文档数量（默认5）
     */
    private Integer topK = 5;

    /**
     * 是否使用知识库增强（默认true）
     */
    private Boolean useKnowledgeBase = true;

    /**
     * 模型温度参数（0.0-1.0，默认0.7）
     */
    private Double temperature = 0.7;

    /**
     * 最大Token数量（默认2000）
     */
    private Integer maxTokens = 2000;

    /**
     * 是否启用聊天记忆（默认true）
     */
    private Boolean enableMemory = true;


}