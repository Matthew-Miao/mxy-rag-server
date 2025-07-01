package com.mxy.ai.rag.dto;

import lombok.Data;

/**
 * 更新会话标题数据传输对象
 * 用于在service层传递更新会话标题的数据
 */
@Data
public class UpdateSessionTitleDTO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 新的会话标题
     */
    private String title;
}