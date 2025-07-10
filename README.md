# 🤖 智能知识助手 - RAG知识问答系统

> 基于Spring AI的企业级RAG智能知识问答系统，让文档知识瞬间变成AI助手

[![GitHub](https://img.shields.io/badge/GitHub-mxy--rag--server-blue?logo=github)](https://github.com/Matthew-Miao/mxy-rag-server)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-orange)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-17-red)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](https://opensource.org/licenses/MIT)

## 🎯 项目简介

智能知识助手是一个基于RAG（检索增强生成）技术的企业级知识问答系统，支持多格式文档上传、智能向量化存储、语义检索和AI问答。

### 核心特性
- 🧠 **智能问答**：基于知识库的精准AI问答
- 📚 **文档管理**：支持PDF、Word、TXT等多格式文档
- 🔍 **语义检索**：向量化存储和相似度搜索
- 💬 **实时对话**：流式响应和会话管理
- 👥 **用户系统**：完整的用户注册、登录、权限管理

## ✨ 功能特性

- **📄 文档管理**：支持PDF、Word、TXT、Markdown等格式文档上传和管理
- **🔍 智能检索**：基于向量相似度的语义搜索，精准匹配相关内容
- **🤖 AI问答**：集成阿里云通义千问，支持流式和阻塞式问答
- **💬 会话管理**：完整的对话会话创建、查询、删除功能
- **👤 用户系统**：用户注册、登录、密码修改等完整用户管理
- **🎨 前端界面**：现代化Web界面，支持聊天、知识库管理等功能
- **⚡ 高性能**：PostgreSQL + pgvector向量存储，HikariCP连接池优化

## 🏗 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.4.5 + Spring AI 1.0.0
- **语言**: Java 17
- **数据库**: PostgreSQL + pgvector (向量存储) + MySQL (业务数据)
- **ORM**: MyBatis-Plus 3.5.5 + Spring AI VectorStore
- **AI模型**: 阿里云通义千问 (qwen-plus-latest) + text-embedding-v3

### 前端技术栈
- **技术**: 原生HTML5 + CSS3 + JavaScript ES6+
- **界面**: 登录页面、聊天界面、知识库管理界面
- **通信**: Fetch API + 流式响应
- **样式**: 响应式设计 + 现代化UI组件

## 📁 项目结构

```
mxy-rag-server/
├── doc/                                    # 文档目录
│   ├── prd.md                             # 产品需求文档
│   ├── 综合技术文档.md                      # 技术架构文档
│   ├── init_database.sql                  # PostgreSQL初始化脚本
│   └── init_mysql_database.sql            # MySQL初始化脚本
├── src/main/java/com/mxy/ai/rag/
│   ├── web/controller/                    # 控制器层
│   │   ├── UserController.java           # 用户管理API
│   │   ├── ChatController.java           # 智能对话API
│   │   ├── ChatSessionController.java    # 会话管理API
│   │   └── KnowledgeBaseController.java  # 知识库管理API
│   ├── service/                          # 业务服务层
│   │   ├── UserService.java              # 用户服务
│   │   ├── ChatService.java              # 对话服务
│   │   ├── ChatSessionService.java       # 会话服务
│   │   └── KnowledgeBaseService.java     # 知识库服务
│   ├── datasource/                       # 数据访问层
│   │   ├── entity/                       # 实体类
│   │   ├── mapper/                       # MyBatis Mapper
│   │   └── dao/                          # DAO层
│   └── config/                           # 配置类
│       └── MultiDataSourceConfig.java   # 多数据源配置
├── src/main/resources/
│   ├── static/                           # 前端静态资源
│   │   ├── login.html                    # 登录页面
│   │   ├── chat.html                     # 聊天界面
│   │   ├── knowledge.html                # 知识库管理
│   │   ├── css/                          # 样式文件
│   │   └── js/                           # JavaScript文件
│   ├── application.yaml                  # 应用配置
│   └── mapper/                           # MyBatis XML映射
└── pom.xml                               # Maven依赖配置
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
## 🎨 前端界面

### 页面功能
- **🔐 登录页面** (`login.html`): 用户登录界面，支持用户名/密码登录
- **💬 聊天界面** (`chat.html`): 智能对话界面，支持实时问答、会话管理、Markdown渲染
- **📚 知识库管理** (`knowledge.html`): 文档上传、管理和搜索界面

### 界面特性
- **响应式设计**: 适配桌面和移动设备
- **现代化UI**: 简洁美观的界面设计
- **实时交互**: 流式响应显示，提升用户体验
- **Markdown支持**: 支持代码高亮和格式化显示
- **文件拖拽**: 支持拖拽上传文档文件

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

- ✅ **数据库设计**: PostgreSQL + pgvector + MySQL双数据源集成
- ✅ **知识库管理**: 文档上传、文本插入、向量存储、语义搜索
- ✅ **用户系统**: 用户注册、登录、密码修改、权限管理
- ✅ **智能对话**: AI问答、流式响应、会话管理、对话历史
- ✅ **前端界面**: 登录页面、聊天界面、知识库管理界面
- ✅ **API接口**: 完整的RESTful API，支持Swagger文档
- 📋 **WebSocket**: 实时通信优化 (规划中)
- 📋 **部署方案**: Docker容器化部署 (规划中)

## 📚 相关文档

- [产品需求文档 (PRD)](./doc/prd.md) - 详细的产品设计和业务需求
- [技术实现文档](./doc/technical_implementation.md) - 技术架构和实现细节
- [综合技术文档](./doc/综合技术文档.md) - 技术架构和实现细节
- [PostgreSQL数据库初始化](./doc/init_database.sql) - 向量数据库初始化脚本
- [MySQL数据库初始化](./doc/init_mysql_database.sql) - 业务数据库初始化脚本
- [GitHub仓库](https://github.com/Matthew-Miao/mxy-rag-server) - 源代码和版本管理

## 📞 联系我

- **项目负责人**: Matthew-Miao
- **邮箱**: 2329385737@qq.com
- **GitHub**: [Matthew-Miao/mxy-rag-server](https://github.com/Matthew-Miao/mxy-rag-server)
---
**版本**: v1.0  
**最后更新**: 2025年7月