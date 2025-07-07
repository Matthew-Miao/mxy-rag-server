package com.mxy.ai.rag.datasource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 聊天消息表实体类
 * 存储会话中的具体消息内容和元数据信息，支持Spring AI聊天记忆
 */
@TableName(value = "chat_messages")
@Data
public class ChatMessagesDO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID（关联chat_sessions表）
     */
    private Long sessionId;

    /**
     * Spring AI 对话ID（冗余字段，便于查询）
     */
    private String conversationId;

    /**
     * Spring AI 消息类型
     * USER: 用户输入消息
     * ASSISTANT: AI助手回复消息
     * SYSTEM: 系统提示消息
     * TOOL: 工具调用消息
     */
    private String messageType;

    /**
     * 消息内容（用户问题或AI回答的完整文本）
     */
    private String content;

    /**
     * 用户评分（1-5分，用户对AI回答的满意度评价）
     */
    private Integer rating;



    /**
     * 0正常，1删除
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    private LocalDateTime gmtModified;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 修改人
     */
    private String modifier;
}