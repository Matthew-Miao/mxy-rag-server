package com.mxy.ai.rag.web.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 会话信息响应对象
 * 用于返回会话的详细信息
 */
@Data
@Schema(description = "会话信息响应对象")
public class SessionVO {

    /**
     * 会话ID
     */
    @Schema(description = "会话ID", example = "1")
    private Long id;

    /**
     * 会话标题
     */
    @Schema(description = "会话标题", example = "我的聊天会话")
    private String title;

    /**
     * 会话描述
     */
    @Schema(description = "会话描述", example = "这是一个关于技术讨论的会话")
    private String description;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间", example = "2024-01-01T12:00:00")
    private LocalDateTime gmtModified;

}