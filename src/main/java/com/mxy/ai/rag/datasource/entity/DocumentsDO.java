package com.mxy.ai.rag.datasource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档管理表实体类
 * 存储上传文档的基本信息和处理状态
 */
@TableName(value = "documents")
@Data
public class DocumentsDO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户标识（文档所有者）
     */
    private String userId;

    /**
     * 文件名（原始文件名）
     */
    private String filename;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型（pdf, docx, txt, md等）
     */
    private String fileType;

    /**
     * MIME类型
     */
    private String mimeType;

    /**
     * 文件内容哈希值（用于去重）
     */
    private String contentHash;

    /**
     * 处理状态（pending:待处理；processing:处理中；completed:已完成；failed:失败）
     */
    private String processingStatus;

    /**
     * 文档分块数量
     */
    private Integer chunkCount;

    /**
     * 文档总Token数
     */
    private Integer totalTokens;

    /**
     * 错误信息（处理失败时的详细信息）
     */
    private String errorMessage;

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