# MXY-RAG 智能问答系统

[![Spring AI Alibaba](https://img.shields.io/badge/Spring%20AI%20Alibaba-1.0.0-blue.svg)](https://github.com/alibaba/spring-ai-alibaba)
[![DashScope](https://img.shields.io/badge/DashScope-阿里云百炼-orange.svg)](https://bailian.console.aliyun.com/console?tab=model#/model-market)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

基于 **Spring AI Alibaba DashScope** 构建的智能RAG（检索增强生成）知识问答系统，深度集成阿里云通义千问模型，提供企业级的文档检索和智能问答服务。

## ✨ 项目特色

- 🚀 **Spring AI Alibaba DashScope**：采用Spring AI Alibaba社区最新代码，通过DashScope深度集成阿里云通义千问模型
- 🧠 **统一千问生态**：LLM和向量模型均使用阿里云通义千问系列（qwen-plus-latest + text-embedding-v3），确保模型一致性
- 📚 **智能文档处理**：支持PDF、Word、TXT等多种格式文档的智能解析和向量化存储
- 🔍 **高效向量检索**：基于PostgreSQL + pgvector实现毫秒级向量相似度搜索
- 💬 **流式对话体验**：支持实时流式响应，提供类ChatGPT的流畅对话体验
- 🎯 **精准上下文理解**：结合检索到的知识库内容和对话历史，提供准确、相关的回答
- 🌐 **现代化前端界面**：基于HTML5/CSS3/JavaScript构建的响应式Web界面

## ✨ 功能特性

- **📄 文档管理**：支持PDF、Word、TXT、Markdown等格式文档上传和管理
- **🔍 智能检索**：基于向量相似度的语义搜索，精准匹配相关内容
- **🤖 AI问答**：集成阿里云通义千问，支持流式和阻塞式问答
- **💬 会话管理**：完整的对话会话创建、查询、删除功能
- **👤 用户系统**：用户注册、登录、密码修改等完整用户管理
- **🎨 前端界面**：现代化Web界面，支持聊天、知识库管理等功能
- **⚡ 高性能**：PostgreSQL + pgvector向量存储，HikariCP连接池优化

## 🏗️ 技术架构

### 后端技术栈
- **Spring Boot 3.4.5** - 现代化Java应用框架
- **Spring AI Alibaba 1.0.2** - 阿里云AI应用开发框架，社区官方支持
- **DashScope Integration** - 阿里云通义千问模型服务集成
- **Java 17** - 长期支持版本
- **PostgreSQL + pgvector** - 向量数据库，高性能向量检索
- **MySQL** - 关系型数据库，存储业务数据
- **MyBatis-Plus** - 数据访问层框架

### AI模型配置（DashScope）

项目使用 **Spring AI Alibaba DashScope** 深度集成阿里云通义千问模型：

- **对话模型**：阿里云通义千问 qwen-plus-latest（通过Spring AI Alibaba DashScope集成）
- **嵌入模型**：阿里云通义千问 text-embedding-v3（1024维向量，DashScope原生支持）
- **集成方式**：使用Spring AI Alibaba的DashScopeChatModel和DashScopeEmbeddingModel
- **技术优势**：统一使用通义千问生态，模型兼容性更好，性能更稳定，社区持续维护

**application.yaml配置**：
```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-plus-latest
          temperature: 0.7
      embedding:
        options:
          model: text-embedding-v3
```

**Java代码示例**：
```java
// KnowledgeBaseServiceImpl.java 中的DashScope配置
@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {
    
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    
    public KnowledgeBaseServiceImpl(ChatClient.Builder chatClientBuilder,
                                   EmbeddingModel embeddingModel) {
        this.chatClient = chatClientBuilder
            .defaultOptions(DashScopeChatOptions.builder()
                .withModel("qwen-plus-latest")
                .withTemperature(0.7f)
                .build())
            .build();
        this.embeddingModel = embeddingModel;
    }
    
    // 知识库问答实现
    public String chatWithKnowledge(String question, String sessionId) {
        // 使用DashScope进行向量检索和智能问答
        // ...
    }
}
```

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

在运行项目前，需要配置以下环境变量：

```bash
# 阿里云DashScope API配置
export AI_DASHSCOPE_API_KEY=your_dashscope_api_key  # 从阿里云控制台获取

# PostgreSQL配置（向量数据库）
export POSTGRES_URL=jdbc:postgresql://localhost:5432/mxy_rag_vector
export POSTGRES_USERNAME=postgres
export POSTGRES_PASSWORD=your_postgres_password

# MySQL配置（业务数据库）
export MYSQL_URL=jdbc:mysql://localhost:3306/mxy_rag_business
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=your_mysql_password
```

**获取DashScope API Key**：
1. 访问 [阿里云模型服务灵积](https://dashscope.aliyuncs.com/)
2. 注册并登录阿里云账号
3. 开通DashScope服务
4. 在控制台获取API Key

### Maven依赖配置

项目使用Spring AI Alibaba DashScope Starter，在`pom.xml`中添加：

```xml
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-dashscope</artifactId>
    <version>1.0.0-M2</version>
</dependency>
```

**Spring AI Alibaba特性**：
- 🚀 开箱即用的DashScope集成
- 🔧 自动配置ChatModel和EmbeddingModel
- 📊 统一的Spring AI接口
- 🛡️ 企业级稳定性保障

### 替代方案：OpenAI兼容模式

除了使用Spring AI Alibaba DashScope原生集成外，项目也支持通过**Spring AI OpenAI兼容模式**使用阿里云通义千问模型：

**Maven依赖（OpenAI兼容模式）**：
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

**application.yaml配置（OpenAI兼容模式）**：
```yaml
spring:
  ai:
    openai:
      api-key: ${AI_DASHSCOPE_API_KEY}
      base-url: https://dashscope.aliyuncs.com/compatible-mode
      chat:
        options:
          model: qwen-plus-latest
      embedding:
        options:
          model: text-embedding-v3
          dimensions: 1024
```

**技术说明**：
- 🔄 **兼容性**：通过OpenAI兼容接口调用千问模型
- 🎯 **灵活性**：可在DashScope原生和OpenAI兼容模式间切换
- 🚀 **性能**：底层仍使用阿里云通义千问系列模型
- 📈 **迁移友好**：便于从其他OpenAI兼容服务迁移

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

### 核心功能 ✅
- ✅ **Spring AI Alibaba DashScope深度集成**
  - 完整的阿里云通义千问生态集成
  - qwen-plus-latest对话模型
  - text-embedding-v3向量模型
  - 统一的Spring AI接口

- ✅ **RAG智能问答系统**
  - 基于pgvector的向量检索
  - 多格式文档解析（PDF、Word、TXT等）
  - 智能分块与向量化存储
  - 上下文感知的问答生成

- ✅ **企业级特性**
  - 会话记忆管理
  - 多数据库支持（PostgreSQL + MySQL）
  - RESTful API接口
  - 现代化Web界面

### 开发进度 🔄
- 🔄 性能优化与调优
- 📋 更多AI能力集成规划
- 🚀 生产环境部署优化

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