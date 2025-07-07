package com.mxy.ai.rag.web.param;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Size;

/**
 * 创建会话请求参数
 * 用于接收创建新会话的请求数据
 */
@Data
@Schema(description = "创建会话请求参数")
public class CreateSessionRequest {

    /**
     * 会话标题（可选，如果不提供将自动生成）
     */
    @Schema(description = "会话标题", example = "我的聊天会话", maxLength = 200)
    @Size(max = 200, message = "会话标题长度不能超过200个字符")
    private String title;

}