-- MXY RAG Server 向量数据库初始化脚本
-- 步骤1：创建向量数据库（在默认数据库中执行）
-- 如果数据库已存在，先删除
DROP DATABASE IF EXISTS mxy_rag_vector_db;

-- 创建新的向量数据库
CREATE DATABASE mxy_rag_vector_db
    WITH 
    ENCODING = 'UTF8'
    LC_COLLATE = 'C'
    LC_CTYPE = 'C'
    TEMPLATE = template0;

-- =====================================================
-- 注意：以下部分需要连接到 mxy_rag_vector_db 数据库后执行
-- 执行方式：
-- 1. 先执行上面的 CREATE DATABASE 语句
-- 2. 然后连接到 mxy_rag_vector_db 数据库
-- 3. 再执行下面的扩展安装和配置
-- =====================================================

-- 步骤2：启用必要扩展（在 mxy_rag_vector_db 数据库中执行）
CREATE EXTENSION IF NOT EXISTS vector;        -- 向量支持
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";   -- UUID 生成
CREATE EXTENSION IF NOT EXISTS pg_trgm;       -- 全文搜索优化
CREATE EXTENSION IF NOT EXISTS btree_gin;     -- GIN 索引优化
CREATE EXTENSION IF NOT EXISTS btree_gist;    -- GiST 索引优化
CREATE EXTENSION IF NOT EXISTS pg_stat_statements; -- SQL 性能统计
CREATE EXTENSION IF NOT EXISTS unaccent;      -- 移除重音符号
CREATE EXTENSION IF NOT EXISTS fuzzystrmatch; -- 模糊字符串匹配
CREATE EXTENSION IF NOT EXISTS hstore;        -- 键值对存储
CREATE EXTENSION IF NOT EXISTS citext;        -- 不区分大小写文本

-- 步骤3：设置向量维度参数
SET vector.max_dimensions = 2048;