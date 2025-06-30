package com.mxy.ai.rag.datasource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户表实体类
 * 存储系统用户的基本信息和认证数据
 */
@TableName(value = "users")
@Data
public class UsersDO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（唯一标识）
     */
    private String username;

    /**
     * 邮箱地址（可选）
     */
    private String email;

    /**
     * 密码哈希值（BCrypt加密）
     */
    private String passwordHash;

    /**
     * 显示名称（用户昵称）
     */
    private String displayName;

    /**
     * 头像URL地址
     */
    private String avatarUrl;

    /**
     * 用户角色（admin:管理员；user:普通用户）
     */
    private String role;

    /**
     * 用户状态（active:活跃；inactive:非活跃；banned:禁用）
     */
    private String status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

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