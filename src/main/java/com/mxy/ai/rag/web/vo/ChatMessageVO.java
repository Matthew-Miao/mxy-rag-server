package com.mxy.ai.rag.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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

    @Schema(description = "消息ID", example = "1")
    private Long id;

    @Schema(description = "会话ID", example = "1")
    private Long sessionId;

    @Schema(description = "消息角色", example = "user")
    private String role;

    @Schema(description = "消息内容", example = "你好，请问什么是人工智能？")
    private String content;

    @Schema(description = "知识来源信息")
    private List<KnowledgeSource> sources;

    @Schema(description = "Token使用量", example = "150")
    private Integer tokensUsed;

    @Schema(description = "响应时间（毫秒）", example = "1200")
    private Integer responseTime;

    @Schema(description = "用户评分（1-5分）", example = "5")
    private Integer rating;

    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime gmtCreate;

    @Schema(description = "修改时间", example = "2024-01-01T10:00:00")
    private LocalDateTime gmtModified;

    /**
     * 知识来源信息
     */
    @Data
    @Schema(description = "知识来源信息")
    public static class KnowledgeSource implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "文档标题", example = "人工智能基础知识")
        private String title;

        @Schema(description = "文档内容片段", example = "人工智能是计算机科学的一个分支...")
        private String content;

        @Schema(description = "相似度分数", example = "0.85")
        private Double similarity;

        @Schema(description = "文档来源", example = "knowledge_base.pdf")
        private String source;
    }
}