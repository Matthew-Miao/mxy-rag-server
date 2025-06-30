package com.mxy.ai.rag.datasource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置表实体类
 * 存储系统运行时的各种配置参数
 */
@TableName(value = "system_config")
@Data
public class SystemConfigDO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置键（唯一标识）
     */
    private String configKey;

    /**
     * 配置值（支持长文本）
     */
    private String configValue;

    /**
     * 配置类型（string:字符串；number:数字；boolean:布尔值；json:JSON对象）
     */
    private String configType;

    /**
     * 配置描述（说明该配置项的作用）
     */
    private String description;

    /**
     * 是否加密（敏感信息如API密钥需要加密存储）
     */
    private Integer isEncrypted;

    /**
     * 是否系统配置（系统配置不允许用户修改）
     */
    private Integer isSystem;

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