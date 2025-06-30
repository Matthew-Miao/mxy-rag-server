# 🤖 智能知识助手 - AI驱动的全流程团队协作平台

> **"让企业知识瞬间变成AI助手"** - 基于Spring AI的RAG智能知识问答系统

## 🎯 项目愿景

**借助AI技术打通团队协作全流程**，从产品设计到研发实施，从前端开发到后端架构，从测试验证到运维部署，实现真正的AI驱动团队协作。

### 核心理念
- 🧠 **AI产品经理**：智能需求分析和产品设计
- 💻 **AI研发助手**：前后端代码生成和架构优化
- 🔍 **AI测试工程师**：自动化测试用例生成
- 🚀 **AI运维专家**：智能部署和监控

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
- **数据库**: PostgreSQL + pgvector (向量存储)
- **ORM**: MyBatis-Plus (业务数据) + Spring AI VectorStore (向量数据)
- **缓存**: Redis
- **AI模型**: 阿里云通义千问 + text-embedding-v3

### 前端技术栈
- **框架**: Vue 3 + TypeScript
- **UI库**: Element Plus
- **状态管理**: Pinia
- **构建工具**: Vite
- **实时通信**: WebSocket

### AI服务层
- **LLM模型**: qwen-plus-latest (阿里云通义千问)
- **嵌入模型**: text-embedding-v3 (1024维)
- **调用方式**: OpenAI兼容模式
- **文档处理**: Apache Tika

## 🚀 快速开始

### 1. 环境准备

**系统要求**:
- Java 17+
- PostgreSQL 12+ (已安装pgvector扩展)
- Redis 6+
- Node.js 16+ (前端开发)

**API密钥配置**:
```bash
# Windows
set AI_DASHSCOPE_API_KEY=your_dashscope_api_key

# Linux/Mac
export AI_DASHSCOPE_API_KEY=your_dashscope_api_key
```

### 2. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE smart_knowledge_assistant;

-- 启用pgvector扩展
\c smart_knowledge_assistant;
CREATE EXTENSION IF NOT EXISTS vector;

-- 创建业务表（详见PRD.md中的MyBatis集成实施指南）
\i sql/init.sql
```

### 3. 后端启动

```bash
# 克隆项目
git clone <repository-url>
cd mxy-rag-server

# 配置数据库连接 (application.yaml)
# 启动应用
mvn clean spring-boot:run
```

### 4. 前端启动 (开发中)

```bash
cd ui-vue3
npm install
npm run dev
```

**访问地址**:
- 后端API: `http://localhost:9000`
- 前端界面: `http://localhost:5173` (开发中)
- API文档: `http://localhost:9000/swagger-ui.html`

## 📡 核心API接口

### 🤖 智能对话API

```http
# 发送消息 (支持流式响应)
POST /api/v1/chat/send
Content-Type: application/json
{
  "sessionId": "session_123",
  "message": "什么是RAG技术？",
  "stream": true
}

# 获取会话列表
GET /api/v1/chat/sessions?page=1&size=20

# 创建新会话
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

# 插入文本
POST /api/v1/knowledge-base/insert-text
Content-Type: application/json
{
  "content": "要插入的文本内容",
  "title": "文档标题"
}

# 相似性搜索
GET /api/v1/knowledge-base/search?query=搜索内容&topK=5

# 获取文档列表
GET /api/v1/knowledge-base/documents
```

### 🔌 WebSocket实时通信

```javascript
// 连接WebSocket
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

## 🎯 AI驱动的团队协作流程

### 🎨 产品设计阶段
- **需求分析**: AI辅助用户故事生成和需求优先级排序
- **原型设计**: 基于最佳实践的UI/UX设计建议
- **技术选型**: 智能推荐最适合的技术栈

### 💻 研发实施阶段
- **后端开发**: Spring Boot + MyBatis自动代码生成
- **前端开发**: Vue 3组件和页面自动生成
- **API设计**: RESTful接口和文档自动生成
- **数据库设计**: 表结构和索引优化建议

### 🔍 测试验证阶段
- **单元测试**: 自动生成测试用例和Mock数据
- **集成测试**: API接口自动化测试
- **性能测试**: 负载测试和性能优化建议

### 🚀 运维部署阶段
- **容器化**: Docker配置自动生成
- **CI/CD**: GitHub Actions工作流配置
- **监控告警**: 系统监控和日志分析
- **扩容策略**: 基于负载的自动扩容建议

## ⚙️ 配置说明

### AI模型配置
- **Chat模型**: qwen-plus-latest (阿里云通义千问)
- **嵌入模型**: text-embedding-v3 (1024维向量)
- **调用方式**: OpenAI兼容模式
- **API地址**: https://dashscope.aliyuncs.com/compatible-mode

### 系统配置
- **文件上传**: 最大支持50MB文件
- **向量维度**: 1024维 (text-embedding-v3)
- **数据库连接池**: HikariCP (最大20个连接)
- **缓存策略**: Redis + 本地缓存双层架构

## 📋 项目状态

- ✅ **后端核心功能**: RAG问答、知识库管理、会话系统
- ✅ **数据库设计**: PostgreSQL + pgvector + MyBatis集成
- 🚧 **前端界面**: Vue 3 + Element Plus (开发中)
- 📋 **测试用例**: 单元测试和集成测试 (规划中)
- 📋 **部署方案**: Docker + K8s (规划中)

## 📚 相关文档

- [产品需求文档 (PRD)](./PRD.md) - 详细的产品设计和技术架构
- [API文档](http://localhost:9000/swagger-ui.html) - 完整的接口文档
- [数据库设计](./docs/database.md) - 数据表结构和关系
- [部署指南](./docs/deployment.md) - 生产环境部署说明

## 🤝 贡献指南

我们欢迎所有形式的贡献！无论是:
- 🐛 Bug报告和修复
- ✨ 新功能建议和实现
- 📖 文档改进
- 🎨 UI/UX优化
- 🧪 测试用例补充

## 📞 联系我们

- **项目负责人**: mixaoxiaoyu
- **技术交流**: [GitHub Issues](https://github.com/your-repo/issues)
- **产品反馈**: [GitHub Discussions](https://github.com/your-repo/discussions)

---

**许可证**: MIT License  
**版本**: v1.0 MVP  
**最后更新**: 2024年