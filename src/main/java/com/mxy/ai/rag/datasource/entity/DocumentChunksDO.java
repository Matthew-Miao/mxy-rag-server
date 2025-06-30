package com.mxy.ai.rag.datasource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档分块表实体类
 * 存储文档切分后的文本块和向量化信息
 */
@TableName(value = "document_chunks")
@Data
public class DocumentChunksDO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文档ID（关联documents表）
     */
    private Long documentId;

    /**
     * 分块索引（在文档中的顺序，从0开始）
     */
    private Integer chunkIndex;

    /**
     * 分块文本内容（实际的文本片段）
     */
    private String content;

    /**
     * 内容长度（字符数）
     */
    private Integer contentLength;

    /**
     * Token数量（该分块的Token计数）
     */
    private Integer tokenCount;

    /**
     * 分块内容哈希值（用于去重）
     */
    private String chunkHash;

    /**
     * 分块元数据（页码、章节、位置等信息）
     */
    private String metadata;

    /**
     * 向量化状态（pending:待处理；processing:处理中；completed:已完成；failed:失败）
     */
    private String embeddingStatus;

    /**
     * 向量数据库中的ID（pgvector中的记录标识）
     */
    private String vectorId;

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