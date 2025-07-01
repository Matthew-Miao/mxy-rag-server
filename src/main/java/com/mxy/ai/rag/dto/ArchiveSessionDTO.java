package com.mxy.ai.rag.dto;

import lombok.Data;

/**
 * 归档会话数据传输对象
 * 用于在service层传递归档会话的数据
 */
@Data
public class ArchiveSessionDTO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 用户ID
     */
    private String userId;
}