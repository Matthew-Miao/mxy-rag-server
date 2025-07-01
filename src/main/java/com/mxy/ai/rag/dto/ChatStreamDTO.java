package com.mxy.ai.rag.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 流式智能问答请求数据传输对象
 * 用于传输流式问答接口的请求参数
 * 
 * @author Mxy
 */
@Data
public class ChatStreamDTO implements Serializable {
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
}