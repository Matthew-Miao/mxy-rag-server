package com.mxy.ai.rag.dto;

import lombok.Data;

/**
 * 更新会话描述数据传输对象
 * 用于在service层传递更新会话描述的数据
 */
@Data
public class UpdateSessionDescriptionDTO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 新的会话描述
     */
    private String description;
}