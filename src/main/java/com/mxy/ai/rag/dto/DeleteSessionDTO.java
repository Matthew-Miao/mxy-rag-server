package com.mxy.ai.rag.dto;

import lombok.Data;

/**
 * 删除会话数据传输对象
 * 用于在service层传递删除会话的数据
 */
@Data
public class DeleteSessionDTO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 用户ID
     */
    private String userId;
}