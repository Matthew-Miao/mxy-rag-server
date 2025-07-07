package com.mxy.ai.rag.datasource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天会话表实体类
 * 存储用户与AI助手的对话会话信息，支持Spring AI聊天记忆
 */
@TableName(value = "chat_sessions")
@Data
public class ChatSessionsDO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户标识（用户ID或会话标识）
     */
    private String userId;

    /**
     * Spring AI 对话ID（用于ChatMemoryRepository）
     */
    private String conversationId;

    /**
     * 会话标题（自动生成或用户自定义）
     */
    private String title;

    /**
     * 会话描述（可选的会话备注信息）
     */
    private String description;
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