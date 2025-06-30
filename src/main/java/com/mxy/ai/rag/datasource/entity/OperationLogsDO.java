package com.mxy.ai.rag.datasource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志表实体类
 * 记录系统中所有重要操作的详细日志
 */
@TableName(value = "operation_logs")
@Data
public class OperationLogsDO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户ID（可为空，表示系统操作）
     */
    private String userId;

    /**
     * 操作类型（如：upload_document、delete_document、chat_query等）
     */
    private String operationType;

    /**
     * 操作目标（如：文档ID、会话ID等）
     */
    private String operationTarget;

    /**
     * 操作详情（JSON格式，记录具体的操作参数和结果）
     */
    private String operationDetails;

    /**
     * IP地址（支持IPv4和IPv6）
     */
    private String ipAddress;

    /**
     * 用户代理（浏览器信息）
     */
    private String userAgent;

    /**
     * 请求ID（用于追踪完整的请求链路）
     */
    private String requestId;

    /**
     * 执行时间（毫秒）
     */
    private Integer executionTime;

    /**
     * 操作状态（success:成功；failed:失败；pending:进行中）
     */
    private String status;

    /**
     * 错误信息（操作失败时的详细错误描述）
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