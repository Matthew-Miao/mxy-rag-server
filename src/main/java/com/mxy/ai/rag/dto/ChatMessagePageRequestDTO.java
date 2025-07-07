package com.mxy.ai.rag.dto;

import lombok.Data;

/**
 * @author mixaoxiaoyu
 * @date 2025-07-07 17:33
 */
@Data
public class ChatMessagePageRequestDTO {
    private Integer pageNum = 1;


    private Integer pageSize = 20;

    private Long sessionId;
}
