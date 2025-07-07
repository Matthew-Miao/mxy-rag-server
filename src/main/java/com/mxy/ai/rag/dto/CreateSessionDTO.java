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

}