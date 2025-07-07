package com.mxy.ai.rag.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户反馈请求数据传输对象
 * 用于传输用户对AI回答的反馈信息
 * 
 * @author Mxy
 */
@Data
public class ChatFeedbackDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 用户评分（1-5分）
     */
    private Integer rating;

}