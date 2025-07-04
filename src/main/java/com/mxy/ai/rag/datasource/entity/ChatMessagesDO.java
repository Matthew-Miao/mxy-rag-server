package com.mxy.ai.rag.datasource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.sql.Timestamp;

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
     * 消息角色（user:用户；assistant:AI助手；system:系统）
     */
    private String role;

    /**
     * Spring AI 消息类型（USER/ASSISTANT/SYSTEM/TOOL）
     */
    private String messageType;

    /**
     * 消息内容（用户问题或AI回答的完整文本）
     */
    private String content;

    /**
     * 知识来源（JSON格式，包含文档来源、相似度分数等信息）
     */
    private String sources;

    /**
     * 额外元数据（扩展信息，如模型版本、处理参数等）
     */
    private String metadata;

    /**
     * Token使用量（该条消息消耗的Token数量）
     */
    private Integer tokensUsed;

    /**
     * 响应时间（AI回答的响应时间，单位：毫秒）
     */
    private Integer responseTime;

    /**
     * 用户评分（1-5分，用户对AI回答的满意度评价）
     */
    private Integer rating;

    /**
     * 上下文权重（用于记忆重要性计算）
     */
    private BigDecimal contextWeight;

    /**
     * 相关性分数（用于记忆检索）
     */
    private BigDecimal relevanceScore;

    /**
     * 语义哈希值（用于相似消息检测）
     */
    private String semanticHash;

    /**
     * Spring AI 标准时间戳
     */
    private Timestamp timestamp;

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