package com.mxy.ai.rag.web.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Min;

/**
 * 会话查询请求参数
 * 用于接收查询会话列表的请求数据
 */
@Data
@Schema(description = "会话查询请求参数")
public class SessionQueryRequest {

    /**
     * 用户ID（必填）
     */
    @Schema(description = "用户ID", example = "user123")
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 搜索关键词（可选，用于搜索会话标题或描述）
     */
    @Schema(description = "搜索关键词", example = "技术讨论")
    private String keyword;

    /**
     * 会话状态过滤（可选：active、archived、deleted）
     */
    @Schema(description = "会话状态", example = "active", allowableValues = {"active", "archived", "deleted"})
    private String status;

    /**
     * 页码（从1开始）
     */
    @Schema(description = "页码", example = "1", minimum = "1", defaultValue = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20", minimum = "1", defaultValue = "20")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 20;
}