# 🤖 智能知识助手 - AI驱动的RAG知识问答系统

> **"让企业知识瞬间变成AI助手"** - 基于Spring AI的RAG智能知识问答系统

[![GitHub](https://img.shields.io/badge/GitHub-mxy--rag--server-blue?logo=github)](https://github.com/Matthew-Miao/mxy-rag-server)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-orange)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-17-red)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](https://opensource.org/licenses/MIT)

## 📖 项目背景

作为一名Java程序员，我深知在现代软件开发中，一个人往往需要承担多重角色：
- 🎯 **产品经理**：需求分析、产品设计、用户体验规划
- 💻 **研发工程师**：架构设计、代码实现、技术选型
- 🧪 **测试工程师**：测试用例设计、质量保证、性能优化
- 🔧 **运维工程师**：部署配置、监控告警、故障处理

**智能知识助手**项目正是在这样的背景下诞生的——它展示了如何借助AI技术，让一个Java程序员能够高效完成从产品设计到运维部署的全流程工作。

### 🎯 AI赋能的全流程开发
- **产品设计阶段**：利用AI辅助需求分析、PRD文档生成、技术方案设计
- **研发实现阶段**：通过AI代码生成、架构优化、最佳实践推荐
- **测试验证阶段**：AI驱动的测试用例生成、自动化测试、质量评估
- **运维部署阶段**：智能化的部署脚本、监控配置、故障诊断

> 🚀 **项目愿景**：构建下一代智能知识管理平台，让每个人都能拥有专属的AI知识助手
>
> 💡 **核心理念**：通过RAG（检索增强生成）技术，将静态知识转化为动态智能，实现人机协同的知识创新
>
> 🎪 **实践案例**：本项目是Java程序员借助AI完成全栈开发的真实案例，从产品规划到技术实现，展示AI如何成为开发者的得力助手

## 🎯 项目愿景

**基于RAG技术构建企业级智能知识问答系统**，帮助企业快速将文档知识转化为智能AI助手，提升知识管理和检索效率。

### 核心理念
- 🧠 **智能问答**：基于企业文档的精准知识问答
- 📚 **知识管理**：多格式文档上传和智能处理
- 🔍 **语义检索**：向量化存储和相似度搜索
- 💬 **对话体验**：流式响应和上下文理解

## ✨ 产品特性

### 🎨 产品层面
- **3分钟部署**：极简安装，快速上手
- **5分钟建库**：拖拽上传，自动处理
- **即问即答**：智能检索，精准回答
- **来源可追溯**：每个答案都有依据

### 🛠 技术层面
- **智能问答**：基于RAG的知识库问答
- **多格式支持**：PDF、Word、TXT、Markdown等文档
- **向量存储**：PostgreSQL + pgvector高性能检索
- **流式响应**：WebSocket实时对话体验
- **混合架构**：MyBatis + Spring AI双引擎驱动

## 🏗 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.4.5 + Spring AI 1.0.0
- **语言**: Java 17
- **数据库**: PostgreSQL + pgvector (向量存储) + MySQL (业务数据)
- **ORM**: MyBatis-Plus 3.5.5 (业务数据) + Spring AI VectorStore (向量数据)
- **连接池**: HikariCP (高性能数据库连接池)
- **AI模型**: 阿里云通义千问 (qwen-plus-latest) + text-embedding-v3

### 前端技术栈 (规划中)
- **框架**: Vue 3 + TypeScript
- **UI库**: Element Plus
- **状态管理**: Pinia
- **构建工具**: Vite
- **实时通信**: WebSocket

### AI服务层
- **LLM模型**: qwen-plus-latest (阿里云通义千问)
- **嵌入模型**: text-embedding-v3 (1024维向量)
- **调用方式**: OpenAI兼容模式 (DashScope API)
- **文档处理**: Spring AI Tika Document Reader + PDF Reader
- **向量存储**: Spring AI PGVector Store

### 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                     客户端 (Client)                         │
│                   HTTP/REST API                            │
└─────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                Spring Boot 应用层                           │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │ KnowledgeBase   │  │   Chat API      │                  │
│  │   Controller    │  │  (规划中)       │                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                    业务服务层                                │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │ KnowledgeBase   │  │   Spring AI     │                  │
│  │    Service      │  │   Integration   │                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                    数据访问层                                │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │   MyBatis-Plus  │  │  Spring AI      │                  │
│  │   (业务数据)     │  │  VectorStore    │                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                    数据存储层                                │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │     MySQL       │  │   PostgreSQL    │                  │
│  │   (业务数据)     │  │   + pgvector    │                  │
│  │                 │  │   (向量数据)     │                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                   外部AI服务                                │
│              阿里云通义千问 DashScope API                    │
└─────────────────────────────────────────────────────────────┘
```

## 📁 项目结构

```
mxy-rag-server/
├── doc/                                    # 文档目录
│   ├── PRD.md                              # 产品需求文档
│   ├── technical_implementation.md         # 技术实现文档
│   ├── init_database.sql                   # PostgreSQL初始化脚本
│   └── init_mysql_database.sql             # MySQL初始化脚本
├── src/main/java/com/mxy/ai/rag/
│   ├── MxyAIRagApplication.java            # Spring Boot启动类
│   ├── config/
│   │   └── MultiDataSourceConfig.java     # 多数据源配置
│   ├── controller/
│   │   └── KnowledgeBaseController.java   # 知识库API控制器
│   ├── service/
│   │   ├── KnowledgeBaseService.java      # 知识库服务接口
│   │   └── impl/
│   │       └── KnowledgeBaseServiceImpl.java # 知识库服务实现
│   └── datasource/                        # 数据访问层
│       ├── entity/                        # 实体类(DO)
│       │   ├── UsersDO.java
│       │   ├── ChatSessionsDO.java
│       │   ├── ChatMessagesDO.java
│       │   ├── DocumentsDO.java
│       │   ├── DocumentChunksDO.java
│       │   ├── SystemConfigDO.java
│       │   └── OperationLogsDO.java
│       ├── mapper/                        # MyBatis Mapper接口
│       │   ├── UsersMapper.java
│       │   ├── ChatSessionsMapper.java
│       │   ├── ChatMessagesMapper.java
│       │   ├── DocumentsMapper.java
│       │   ├── DocumentChunksMapper.java
│       │   ├── SystemConfigMapper.java
│       │   └── OperationLogsMapper.java
│       └── dao/                           # DAO业务层
│           ├── UsersDAO.java
│           ├── ChatSessionsDAO.java
│           ├── ChatMessagesDAO.java
│           ├── DocumentsDAO.java
│           ├── DocumentChunksDAO.java
│           ├── SystemConfigDAO.java
│           └── OperationLogsDAO.java
├── src/main/resources/
│   ├── application.yaml                   # 应用配置文件
│   └── mapper/                           # MyBatis XML映射文件
│       ├── UsersMapper.xml
│       ├── ChatSessionsMapper.xml
│       ├── ChatMessagesMapper.xml
│       ├── DocumentsMapper.xml
│       ├── DocumentChunksMapper.xml
│       ├── SystemConfigMapper.xml
│       └── OperationLogsMapper.xml
├── pom.xml                               # Maven依赖配置
└── README.md                             # 项目说明文档
```

## 🚀 快速开始

### 1. 环境准备

**系统要求**:
- Java 17+
- PostgreSQL 12+ (需安装pgvector扩展)
- MySQL 8.0+
- Maven 3.6+

**API密钥配置**:
```bash
# Windows
set AI_DASHSCOPE_API_KEY=your_dashscope_api_key

# Linux/Mac
export AI_DASHSCOPE_API_KEY=your_dashscope_api_key
```

### 2. 数据库初始化

**PostgreSQL (向量数据库)**:
```sql
-- 创建向量数据库
CREATE DATABASE mxy_rag_vector_db;

-- 启用pgvector扩展
\c mxy_rag_vector_db;
CREATE EXTENSION IF NOT EXISTS vector;

-- 执行向量数据库初始化脚本
\i doc/init_database.sql
```

**MySQL (业务数据库)**:
```sql
-- 创建业务数据库
CREATE DATABASE mxy_rag_db DEFAULT CHARACTER SET utf8mb4;

-- 执行业务数据库初始化脚本
USE mxy_rag_db;
source doc/init_mysql_database.sql;
```

### 3. 后端启动

```bash
# 克隆项目
git clone https://github.com/Matthew-Miao/mxy-rag-server.git
cd mxy-rag-server

# 配置环境变量
set AI_DASHSCOPE_API_KEY=your_api_key
set postgresql.host=localhost
set mysql.host=localhost
set mysql.username=root
set mysql.password=your_password

# 编译并启动应用
mvn clean spring-boot:run
```

### 4. 验证启动

**访问地址**:
- 后端API: `http://localhost:9000`
- API文档: `http://localhost:9000/swagger-ui.html` (如已配置)
- 健康检查: `http://localhost:9000/actuator/health` (如已配置)

**测试API**:
```bash
# 插入测试文本
curl -X POST "http://localhost:9000/api/v1/knowledge-base/insert-text" \
     -d "content=这是一个测试文档内容"

# 相似性搜索
curl "http://localhost:9000/api/v1/knowledge-base/search?query=测试&topK=5"
```

## 📡 核心API接口

### 🤖 智能对话API (开发中)

```http
# 发送消息 (规划中)
POST /api/v1/chat/send
Content-Type: application/json
{
  "sessionId": "session_123",
  "message": "什么是RAG技术？",
  "stream": true
}

# 获取会话列表 (规划中)
GET /api/v1/chat/sessions?page=1&size=20

# 创建新会话 (规划中)
POST /api/v1/chat/sessions
{
  "title": "新对话"
}
```

### 📚 知识库管理API

```http
# 上传文档
POST /api/v1/knowledge-base/upload-file
Content-Type: multipart/form-data
file=@document.pdf

# 插入文本内容
POST /api/v1/knowledge-base/insert-text
Content-Type: application/x-www-form-urlencoded
content=要插入的文本内容

# 相似性搜索
GET /api/v1/knowledge-base/search?query=搜索内容&topK=5

# 获取文档列表
GET /api/v1/knowledge-base/documents

# 删除文档
DELETE /api/v1/knowledge-base/documents/{id}
```

### 🔌 WebSocket实时通信 (规划中)

```javascript
// 连接WebSocket (规划中)
const ws = new WebSocket('ws://localhost:9000/api/v1/chat/stream');

// 发送消息
ws.send(JSON.stringify({
  "type": "chat",
  "sessionId": "session_123",
  "message": "用户问题"
}));

// 接收响应
ws.onmessage = (event) => {
  const response = JSON.parse(event.data);
  console.log(response.content);
};
```

## 🎯 核心功能特性

### 📚 知识库管理
- **文档上传**: 支持PDF、Word、TXT、Markdown等多种格式
- **智能处理**: 自动文档解析和文本提取
- **向量化存储**: 基于text-embedding-v3的1024维向量存储
- **批量管理**: 支持批量上传和文档管理

### 🔍 智能检索
- **语义搜索**: 基于向量相似度的智能检索
- **精准匹配**: 支持关键词和语义双重匹配
- **来源追溯**: 每个答案都标注具体的文档来源
- **相关性排序**: 按相似度分数排序检索结果

### 🤖 AI问答 (规划中)
- **自然对话**: 支持自然语言问答交互
- **上下文理解**: 多轮对话的上下文记忆
- **流式响应**: 实时显示AI回答过程
- **质量评价**: 支持用户对回答质量进行评分

## ⚙️ 配置说明

### 环境变量配置
```bash
# 必需配置
AI_DASHSCOPE_API_KEY=your_dashscope_api_key    # 阿里云DashScope API密钥
postgresql.host=localhost                       # PostgreSQL主机地址
mysql.host=localhost                           # MySQL主机地址
mysql.username=root                            # MySQL用户名
mysql.password=your_password                   # MySQL密码
```

### AI模型配置
- **Chat模型**: qwen-plus-latest (阿里云通义千问)
- **嵌入模型**: text-embedding-v3 (1024维向量)
- **调用方式**: OpenAI兼容模式
- **API地址**: https://dashscope.aliyuncs.com/compatible-mode

### 系统配置
- **服务端口**: 9000
- **文件上传**: 最大支持10MB文件
- **向量维度**: 1024维 (text-embedding-v3)
- **数据库连接池**: HikariCP (PostgreSQL: 12个连接, MySQL: 12个连接)
- **多数据源**: PostgreSQL(向量) + MySQL(业务)

## 📋 项目状态

- ✅ **数据库设计**: PostgreSQL + pgvector + MySQL + MyBatis-Plus集成
- ✅ **知识库管理**: 文档上传、文本插入、向量存储
- ✅ **智能检索**: 基于向量相似度的语义搜索
- ✅ **多数据源配置**: PostgreSQL(向量) + MySQL(业务)双数据源
- 🚧 **AI对话功能**: 智能问答和会话管理 (开发中)
- 🚧 **前端界面**: Vue 3 + Element Plus (规划中)
- 📋 **WebSocket**: 实时通信和流式响应 (规划中)
- 📋 **部署方案**: Docker + K8s (规划中)

## 📚 相关文档

- [产品需求文档 (PRD)](./doc/PRD.md) - 详细的产品设计和业务需求
- [技术实现文档](./doc/technical_implementation.md) - 技术架构和实现细节
- [PostgreSQL数据库初始化](./doc/init_database.sql) - 向量数据库初始化脚本
- [MySQL数据库初始化](./doc/init_mysql_database.sql) - 业务数据库初始化脚本
- [GitHub仓库](https://github.com/Matthew-Miao/mxy-rag-server) - 源代码和版本管理

## 🤝 贡献指南

我们欢迎所有形式的贡献！无论是:
- 🐛 Bug报告和修复
- ✨ 新功能建议和实现
- 📖 文档改进和完善
- 🧪 测试用例补充
- 🔧 代码优化和重构

**贡献流程**:
1. Fork 项目到你的GitHub账户
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的修改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📞 联系我们

- **项目负责人**: Matthew-Miao
- **邮箱**: 2329385737@qq.com
- **GitHub**: [Matthew-Miao/mxy-rag-server](https://github.com/Matthew-Miao/mxy-rag-server)
- **技术交流**: [GitHub Issues](https://github.com/Matthew-Miao/mxy-rag-server/issues)
- **产品反馈**: [GitHub Discussions](https://github.com/Matthew-Miao/mxy-rag-server/discussions)

---

**许可证**: MIT License  
**版本**: v1.0 MVP  
**最后更新**: 2025年1月