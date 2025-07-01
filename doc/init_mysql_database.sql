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
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱地址（可选）',
    `password_hash` varchar(255) DEFAULT NULL COMMENT '密码哈希值（BCrypt加密）',
    `display_name` varchar(100) DEFAULT NULL COMMENT '显示名称（用户昵称）',
    `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像URL地址',
    `role` varchar(20) NOT NULL DEFAULT 'user' COMMENT '用户角色（admin:管理员；user:普通用户）',
    `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '用户状态（active:活跃；inactive:非活跃；banned:禁用）',
    `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
    `deleted` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0正常，1删除',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `creator` varchar(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    `modifier` varchar(64) NOT NULL DEFAULT 'system' COMMENT '修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表 - 存储系统用户的基本信息和认证数据';

-- =====================================================
-- 聊天会话表（支持 Spring AI 聊天记忆）
-- =====================================================
CREATE TABLE IF NOT EXISTS `chat_sessions` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键（自增）',
    `user_id` varchar(50) NOT NULL COMMENT '用户标识（用户ID或会话标识）',
    `conversation_id` varchar(100) DEFAULT NULL COMMENT 'Spring AI 对话ID（用于ChatMemoryRepository）',
    `title` varchar(200) NOT NULL COMMENT '会话标题（自动生成或用户自定义）',
    `description` text DEFAULT NULL COMMENT '会话描述（可选的会话备注信息）',
    `message_count` int(11) NOT NULL DEFAULT '0' COMMENT '消息数量（该会话中的消息总数）',
    `total_tokens` int(11) NOT NULL DEFAULT '0' COMMENT 'Token总数（该会话消耗的总Token数）',
    `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '会话状态（active:活跃；archived:归档；deleted:已删除）',
    `max_context_messages` int(11) NOT NULL DEFAULT '20' COMMENT '最大上下文消息数（记忆窗口大小）',
    `context_strategy` varchar(50) NOT NULL DEFAULT 'sliding_window' COMMENT '上下文策略（sliding_window:滑动窗口；summary:摘要；hybrid:混合）',
    `memory_retention_hours` int(11) NOT NULL DEFAULT '168' COMMENT '记忆保留时间（小时，默认7天）',
    `last_activity_time` datetime DEFAULT NULL COMMENT '最后活动时间（用于记忆清理）',
    `deleted` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0正常，1删除',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `creator` varchar(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    `modifier` varchar(64) NOT NULL DEFAULT 'system' COMMENT '修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conversation_id` (`conversation_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_last_activity` (`last_activity_time`),
    KEY `idx_user_status_create` (`user_id`, `status`, `gmt_create`),
    KEY `idx_gmt_modified` (`gmt_modified`),
    KEY `idx_gmt_create` (`gmt_create`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表 - 存储用户与AI助手的对话会话信息，支持Spring AI聊天记忆';

-- =====================================================
-- 聊天消息表（支持 Spring AI 聊天记忆）
-- =====================================================
CREATE TABLE IF NOT EXISTS `chat_messages` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键（自增）',
    `session_id` bigint(20) NOT NULL COMMENT '会话ID（关联chat_sessions表）',
    `conversation_id` varchar(100) DEFAULT NULL COMMENT 'Spring AI 对话ID（冗余字段，便于查询）',
    `role` varchar(20) NOT NULL COMMENT '消息角色（user:用户；assistant:AI助手；system:系统）',
    `message_type` varchar(20) DEFAULT NULL COMMENT 'Spring AI 消息类型（USER/ASSISTANT/SYSTEM/TOOL）',
    `content` text NOT NULL COMMENT '消息内容（用户问题或AI回答的完整文本）',
    `sources` json DEFAULT NULL COMMENT '知识来源（JSON格式，包含文档来源、相似度分数等信息）',
    `metadata` json DEFAULT NULL COMMENT '额外元数据（扩展信息，如模型版本、处理参数等）',
    `tokens_used` int(11) DEFAULT NULL COMMENT 'Token使用量（该条消息消耗的Token数量）',
    `response_time` int(11) DEFAULT NULL COMMENT '响应时间（AI回答的响应时间，单位：毫秒）',
    `rating` int(11) DEFAULT NULL COMMENT '用户评分（1-5分，用户对AI回答的满意度评价）',
    `context_weight` decimal(3,2) NOT NULL DEFAULT '1.00' COMMENT '上下文权重（用于记忆重要性计算）',
    `relevance_score` decimal(5,4) DEFAULT NULL COMMENT '相关性分数（用于记忆检索）',
    `semantic_hash` varchar(64) DEFAULT NULL COMMENT '语义哈希值（用于相似消息检测）',
    `timestamp` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT 'Spring AI 标准时间戳',
    `deleted` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0正常，1删除',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `creator` varchar(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    `modifier` varchar(64) NOT NULL DEFAULT 'system' COMMENT '修改人',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_conversation_timestamp` (`conversation_id`, `timestamp`),
    KEY `idx_session_role_create` (`session_id`, `role`, `gmt_create`),
    KEY `idx_session_create_id` (`session_id`, `gmt_create`, `id`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_gmt_create` (`gmt_create`),
    KEY `idx_role` (`role`),
    KEY `idx_rating` (`rating`),
    KEY `idx_relevance_score` (`relevance_score`),
    KEY `idx_semantic_hash` (`semantic_hash`),
    CONSTRAINT `fk_chat_messages_session_id` FOREIGN KEY (`session_id`) REFERENCES `chat_sessions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表 - 存储会话中的具体消息内容和元数据信息，支持Spring AI聊天记忆';
-- =====================================================
-- 第五步：创建文档管理表
-- =====================================================

CREATE TABLE IF NOT EXISTS `documents` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键（自增）',
    `user_id` varchar(50) NOT NULL COMMENT '用户标识（文档所有者）',
    `filename` varchar(255) NOT NULL COMMENT '文件名（原始文件名）',
    `file_path` varchar(500) DEFAULT NULL COMMENT '文件存储路径',
    `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小（字节）',
    `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型（pdf, docx, txt, md等）',
    `mime_type` varchar(100) DEFAULT NULL COMMENT 'MIME类型',
    `content_hash` varchar(64) DEFAULT NULL COMMENT '文件内容哈希值（用于去重）',
    `processing_status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '处理状态（pending:待处理；processing:处理中；completed:已完成；failed:失败）',
    `chunk_count` int(11) NOT NULL DEFAULT '0' COMMENT '文档分块数量',
    `total_tokens` int(11) NOT NULL DEFAULT '0' COMMENT '文档总Token数',
    `error_message` text DEFAULT NULL COMMENT '错误信息（处理失败时的详细信息）',
    `deleted` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0正常，1删除',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `creator` varchar(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    `modifier` varchar(64) NOT NULL DEFAULT 'system' COMMENT '修改人',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_processing_status` (`processing_status`),
    KEY `idx_file_type` (`file_type`),
    KEY `idx_content_hash` (`content_hash`),
    KEY `idx_gmt_create` (`gmt_create`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档管理表 - 存储上传文档的基本信息和处理状态';

-- =====================================================
-- 第六步：创建文档分块表
-- =====================================================

CREATE TABLE IF NOT EXISTS `document_chunks` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键（自增）',
    `document_id` bigint(20) NOT NULL COMMENT '文档ID（关联documents表）',
    `chunk_index` int(11) NOT NULL COMMENT '分块索引（在文档中的顺序，从0开始）',
    `content` text NOT NULL COMMENT '分块文本内容（实际的文本片段）',
    `content_length` int(11) NOT NULL COMMENT '内容长度（字符数）',
    `token_count` int(11) DEFAULT NULL COMMENT 'Token数量（该分块的Token计数）',
    `chunk_hash` varchar(64) NOT NULL COMMENT '分块内容哈希值（用于去重）',
    `metadata` json DEFAULT NULL COMMENT '分块元数据（页码、章节、位置等信息）',
    `embedding_status` enum('pending','processing','completed','failed') DEFAULT 'pending' COMMENT '向量化状态',
    `vector_id` varchar(100) DEFAULT NULL COMMENT '向量数据库中的ID（pgvector中的记录标识）',
    `deleted` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0正常，1删除',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `creator` varchar(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    `modifier` varchar(64) NOT NULL DEFAULT 'system' COMMENT '修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chunk_hash` (`chunk_hash`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_chunk_index` (`chunk_index`),
    KEY `idx_embedding_status` (`embedding_status`),
    KEY `idx_vector_id` (`vector_id`),
    KEY `idx_gmt_create` (`gmt_create`),
    CONSTRAINT `fk_document_chunks_document_id` FOREIGN KEY (`document_id`) REFERENCES `documents` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档分块表 - 存储文档切分后的文本块和向量化信息';

-- =====================================================
-- 第七步：创建系统配置表
-- =====================================================

CREATE TABLE IF NOT EXISTS `system_config` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键（自增）',
    `config_key` varchar(100) NOT NULL COMMENT '配置键（唯一标识）',
    `config_value` text DEFAULT NULL COMMENT '配置值（支持长文本）',
    `config_type` varchar(20) NOT NULL DEFAULT 'string' COMMENT '配置类型（string:字符串；number:数字；boolean:布尔值；json:JSON对象）',
    `description` varchar(500) DEFAULT NULL COMMENT '配置描述（说明该配置项的作用）',
    `is_encrypted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否加密（敏感信息如API密钥需要加密存储）',
    `is_system` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否系统配置（系统配置不允许用户修改）',
    `deleted` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0正常，1删除',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `creator` varchar(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    `modifier` varchar(64) NOT NULL DEFAULT 'system' COMMENT '修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    KEY `idx_config_type` (`config_type`),
    KEY `idx_is_system` (`is_system`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表 - 存储系统运行时的各种配置参数';

-- =====================================================
-- 第八步：创建操作日志表
-- =====================================================

CREATE TABLE IF NOT EXISTS `operation_logs` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键（自增）',
    `user_id` varchar(50) DEFAULT NULL COMMENT '操作用户ID（可为空，表示系统操作）',
    `operation_type` varchar(50) NOT NULL COMMENT '操作类型（如：upload_document、delete_document、chat_query等）',
    `operation_target` varchar(100) DEFAULT NULL COMMENT '操作目标（如：文档ID、会话ID等）',
    `operation_details` json DEFAULT NULL COMMENT '操作详情（JSON格式，记录具体的操作参数和结果）',
    `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP地址（支持IPv4和IPv6）',
    `user_agent` text DEFAULT NULL COMMENT '用户代理（浏览器信息）',
    `request_id` varchar(100) DEFAULT NULL COMMENT '请求ID（用于追踪完整的请求链路）',
    `execution_time` int(11) DEFAULT NULL COMMENT '执行时间（毫秒）',
    `status` varchar(20) NOT NULL DEFAULT 'success' COMMENT '操作状态（success:成功；failed:失败；pending:进行中）',
    `error_message` text DEFAULT NULL COMMENT '错误信息（操作失败时的详细错误描述）',
    `deleted` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0正常，1删除',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `creator` varchar(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    `modifier` varchar(64) NOT NULL DEFAULT 'system' COMMENT '修改人',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_status` (`status`),
    KEY `idx_gmt_create` (`gmt_create`),
    KEY `idx_request_id` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表 - 记录系统中所有重要操作的详细日志';

-- =====================================================
-- 第九步：插入初始化数据
-- =====================================================

-- 插入系统默认配置
INSERT INTO system_config (config_key, config_value, config_type, description, is_public) VALUES
('system.name', 'MXY RAG Server', 'string', '系统名称', true),
('system.version', '1.0.0', 'string', '系统版本', true),
('system.description', '智能知识助手 - AI驱动的全流程团队协作平台', 'string', '系统描述', true),
('ai.model.default', 'qwen-turbo', 'string', '默认AI模型', false),
('ai.embedding.model', 'text-embedding-v1', 'string', '默认嵌入模型', false),
('ai.max_tokens', '2000', 'number', '最大Token数', false),
('ai.temperature', '0.7', 'number', '模型温度参数', false),
('upload.max_file_size', '52428800', 'number', '最大文件上传大小（50MB）', false),
('upload.allowed_types', '["pdf", "docx", "doc", "txt", "md"]', 'json', '允许上传的文件类型', false),
('chat.max_history', '20', 'number', '最大聊天历史记录数', false),
('vector.dimension', '1536', 'number', '向量维度', false),
('vector.similarity_threshold', '0.7', 'number', '向量相似度阈值', false)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 创建默认管理员用户（可选）
-- 注意：密码为 'admin123' 的BCrypt哈希值，生产环境请修改
-- INSERT INTO users (username, email, password_hash, display_name, role) VALUES
-- ('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMcfOjRkNjjSQbRpHHJe6K', '系统管理员', 'admin')
-- ON DUPLICATE KEY UPDATE username = VALUES(username);

-- =====================================================
-- 初始化完成提示
-- =====================================================

-- 显示初始化完成信息
SELECT '==========================================\n' AS '',
       'MXY RAG Server 业务数据库初始化完成！\n' AS '',
       '==========================================\n' AS '',
       '已创建的表:\n' AS '',
       '- users: 用户表\n' AS '',
       '- chat_sessions: 聊天会话表\n' AS '',
       '- chat_messages: 聊天消息表\n' AS '',
       '- documents: 文档管理表\n' AS '',
       '- document_chunks: 文档分块表\n' AS '',
       '- system_config: 系统配置表\n' AS '',
       '- operation_logs: 操作日志表\n' AS '',
       '==========================================\n' AS '',
       '注意事项:\n' AS '',
       '1. 向量存储请使用PostgreSQL数据库\n' AS '',
       '2. 在application.yml中配置数据源\n' AS '',
       '3. 启动Spring Boot应用\n' AS '',
       '==========================================\n' AS '';

-- =====================================================
-- 脚本结束
-- =====================================================