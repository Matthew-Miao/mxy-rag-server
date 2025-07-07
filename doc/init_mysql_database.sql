-- =====================================================
-- MXY RAG Server 业务数据库初始化脚本 (MySQL)
-- 智能知识助手 - AI驱动的全流程团队协作平台
-- =====================================================
-- 版本: v1.0
-- 创建日期: 2025年
-- 数据库: MySQL 8.0+
-- 描述: 专用于业务数据的MySQL数据库初始化脚本
-- 注意: 向量存储请使用 init_database.sql 脚本（PostgreSQL）
-- =====================================================

-- =====================================================
-- 第一步：创建业务数据库（如果不存在）
-- =====================================================
-- 注意：此部分需要以管理员身份执行
-- 或者手动创建数据库后跳过此部分

-- 创建业务数据库
CREATE DATABASE IF NOT EXISTS mxy_rag_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

-- 使用业务数据库
USE mxy_rag_db;

-- =====================================================
-- 第二步：创建用户表（用于用户管理）
-- =====================================================

CREATE TABLE IF NOT EXISTS `users` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键（自增）',
    `username` varchar(50) NOT NULL COMMENT '用户名（唯一标识）',
    `password` varchar(100) NOT NULL COMMENT '密码',
    `user_id` varchar(50) NOT NULL COMMENT '用户ID',
    `deleted` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0正常，1删除',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `creator` varchar(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    `modifier` varchar(64) NOT NULL DEFAULT 'system' COMMENT '修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================
-- 聊天会话表（支持 Spring AI 聊天记忆）
-- =====================================================
CREATE TABLE IF NOT EXISTS `chat_sessions` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键（自增）',
    `title` varchar(200) NOT NULL COMMENT '会话标题（自动生成或用户自定义）',
    `description` text DEFAULT NULL COMMENT '会话描述（可选的会话备注信息）',
    `deleted` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0正常，1删除',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `creator` varchar(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    `modifier` varchar(64) NOT NULL DEFAULT 'system' COMMENT '修改人',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- =====================================================
-- 聊天消息表（支持 Spring AI 聊天记忆）
-- =====================================================
CREATE TABLE IF NOT EXISTS `chat_messages` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键（自增）',
    `session_id` bigint(20) NOT NULL COMMENT '会话ID（关联chat_sessions表）',
    `conversation_id` varchar(100) DEFAULT NULL COMMENT 'Spring AI 对话ID（冗余字段，便于查询）',
    `message_type` varchar(20) DEFAULT NULL COMMENT 'Spring AI 消息类型（USER:用户输入消息；ASSISTANT:AI助手回复消息；SYSTEM:系统提示消息；TOOL:工具调用消息）',
    `content` text NOT NULL COMMENT '消息内容（用户问题或AI回答的完整文本）',
    `rating` int(11) DEFAULT NULL COMMENT '用户评分（1-5分，用户对AI回答的满意度评价）',
    `deleted` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0正常，1删除',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `creator` varchar(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    `modifier` varchar(64) NOT NULL DEFAULT 'system' COMMENT '修改人',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- =====================================================
-- 脚本结束
-- =====================================================