package com.mxy.ai.rag.dto;

import lombok.Data;

/**
 * 会话查询数据传输对象
 * 用于在service层传递查询会话列表的数据
 */
@Data
public class SessionQueryDTO {
    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;
    
    /**
     * 用户ID
     */
    private String userId;
}