package com.mxy.ai.rag.dto;

import lombok.Data;

import java.io.Serializable;
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
     * 用户问题
     */
    private String question;

    /**
     * 检索文档数量（默认5）
     */
    private Integer topK = 5;


}