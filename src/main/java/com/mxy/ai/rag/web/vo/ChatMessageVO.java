package com.mxy.ai.rag.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 聊天消息响应对象
 * 用于返回聊天消息的详细信息
 * 
 * @author Mxy
 */
@Data
@Schema(description = "聊天消息响应对象")
public class ChatMessageVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "消息Id")
    private Long id;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "Spring AI 对话ID", example = "conv_123456")
    private String conversationId;

    @Schema(description = "Spring AI 消息类型（USER:用户输入消息；ASSISTANT:AI助手回复消息；SYSTEM:系统提示消息；TOOL:工具调用消息）", example = "USER")
    private String messageType;

    @Schema(description = "消息内容", example = "你好，请问什么是人工智能？")
    private String content;

}