package com.mxy.ai.rag.dto;

import lombok.Data;

/**
 * 恢复会话数据传输对象
 * 用于在service层传递恢复会话的数据
 */
@Data
public class RestoreSessionDTO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 用户ID
     */
    private String userId;
}