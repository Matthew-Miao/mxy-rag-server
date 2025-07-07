package com.mxy.ai.rag.dto;

import lombok.Data;

/**
 * 会话查询数据传输对象
 * 用于在service层传递查询会话列表的数据
 */
@Data
public class SessionQueryDTO {

    /**
     * 用户ID（必填）
     */
    private String userId;

    /**
     * 搜索关键词（可选，用于搜索会话标题或描述）
     */
    private String keyword;



    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}