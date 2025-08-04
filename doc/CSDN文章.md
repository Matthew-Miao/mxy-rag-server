# 🚀 基于Spring AI Alibaba的智能知识助手系统：从零到一的RAG实战开发

## 📖 项目概述

在人工智能快速发展的今天，RAG（Retrieval-Augmented Generation）技术已成为构建智能问答系统的核心技术。本文将详细介绍一个基于**Spring AI Alibaba DashScope**深度集成的智能知识助手系统的完整开发过程，该系统采用现代化的技术栈，实现了企业级的RAG解决方案。

**项目地址**：[https://github.com/Matthew-Miao/mxy-rag-server](https://github.com/Matthew-Miao/mxy-rag-server)

## 🎯 项目核心价值

### 技术创新点
- **深度集成Spring AI Alibaba**：原生支持阿里云通义千问模型，提供统一的AI接口
- **双模式AI支持**：同时支持Spring AI Alibaba DashScope和OpenAI兼容模式
- **企业级RAG架构**：完整的检索增强生成系统，支持多种文档格式
- **现代化技术栈**：Spring Boot 3.x + PostgreSQL + pgvector + MySQL
- **用户会话管理**：基于ThreadLocal的用户上下文管理系统

### 业务价值
- **智能知识问答**：基于用户上传的文档进行精准问答
- **多会话管理**：支持多个对话会话，保持上下文连续性
- **实时流式对话**：支持流式响应，提升用户体验
- **知识库管理**：完整的文档上传、处理、检索功能

## 🏗️ 系统架构设计

### 整体架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端展示层     │    │   业务逻辑层     │    │   数据存储层     │
│                │    │                │    │                │
│ HTML5 + CSS3   │◄──►│ Spring Boot    │◄──►│ PostgreSQL     │
│ 原生JavaScript │    │ Spring AI      │    │ + pgvector     │
│ Font Awesome   │    │ MyBatis        │    │                │
│ WebSocket      │    │ WebSocket      │    │ MySQL          │
│                │    │                │    │                │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │   AI服务层      │
                    │                │
                    │ 阿里云通义千问   │
                    │ DashScope API  │
                    │ 文本嵌入模型    │
                    └─────────────────┘
```

### 技术栈详解

#### 后端技术栈
- **Spring Boot 3.2.0**：现代化的Java企业级框架
- **Spring AI Alibaba**：阿里云AI服务的Spring集成
- **MyBatis**：灵活的持久层框架
- **PostgreSQL + pgvector**：向量数据库，支持相似性搜索
- **MySQL**：业务数据存储
- **ThreadLocal**：用户会话管理
- **WebSocket**：实时通信支持

#### 前端技术栈
- **HTML5**：现代化的标记语言，支持语义化标签
- **CSS3**：样式设计，支持Flexbox、Grid、动画等现代特性
- **原生JavaScript (ES6+)**：无框架依赖的纯JavaScript实现
- **Font Awesome 6.0**：图标库，提供丰富的矢量图标
- **Marked.js**：Markdown解析库，支持消息格式化
- **Highlight.js**：代码高亮库，支持多种编程语言
- **Fetch API**：现代化的HTTP请求接口
- **WebSocket**：实时通信支持

## 🔧 核心功能实现

### 1. Spring AI Alibaba集成

#### Maven依赖配置

```xml
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-dashscope</artifactId>
    <version>1.0.0.2</version>
</dependency>
```

#### 配置文件设置

```yaml
spring:
  ai:
        vectorstore:
      pgvector:
        table-name: mxy_rag_vector
        initialize-schema: true
        dimensions: 1024
        index-type: hnsw
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-plus-latest
      embedding:
        options:
          model: text-embedding-v3
          dimensions: 1024
 #    openai:
#      api-key: ${AI_DASHSCOPE_API_KEY}
#      base-url: https://dashscope.aliyuncs.com/compatible-mode
#      chat:
#        options:
#          model: qwen-plus-latest
#      embedding:
#        options:
#          model: text-embedding-v3
#          dimensions: 1024
```

#### 核心服务实现

**KnowledgeBaseServiceImpl.java - 知识库服务核心实现：**

```java
/**
 * 知识库服务实现类
 * @author Mxy
 */
@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseServiceImpl.class);
    
    /**
     * 系统提示词：指导AI智能地处理不同类型的问题
     */
    private static final String SYSTEM_PROMPT = "你是一个智能助手。请始终使用中文回答用户的问题。当回答用户问题时，请遵循以下策略：\n" +
            "1. 对于基础知识问题（如数学计算、常识问题等），直接使用你的通用知识准确回答\n" +
            "2. 对于专业或特定领域的问题，优先从向量数据库中检索相关知识来回答\n" +
            "3. 如果向量数据库中没有找到相关信息，请从聊天记忆中寻找之前讨论过的相关内容\n" +
            "4. 如果以上都没有相关信息，请基于你的通用知识给出准确、有帮助的回答\n" +
            "5. 只有在确实无法回答时，才诚实地告知用户并建议他们提供更多信息\n" +
            "请确保回答准确、相关且有帮助。不要因为向量数据库中没有信息就拒绝回答基础问题。所有回答都必须使用中文。";

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    
    /**
     * 构造函数：初始化知识库服务
     * 
     * @param vectorStore 向量存储
     * @param chatModel 聊天模型
     * @param messageWindowChatMemory 消息窗口聊天记忆
     */
    public KnowledgeBaseServiceImpl(VectorStore vectorStore, 
                                    @Qualifier("dashscopeChatModel") ChatModel chatModel,
                                    MessageWindowChatMemory messageWindowChatMemory) {
        this.vectorStore = vectorStore;
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(
                    SimpleLoggerAdvisor.builder().build(),
                    MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build()
                )
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).build())
                .build();
    }
    
    /**
     * 与知识库进行对话
     * 
     * @param query 用户查询
     * @param conversationId 对话ID
     * @param topK 检索文档数量
     * @return 回答内容
     */
    @Override
    public String chatWithKnowledge(String query, String conversationId, int topK) {
        Assert.hasText(query, "查询问题不能为空");
        logger.info("开始知识库对话，查询: '{}', conversationId: {}", query, conversationId);
        
        try {
            String prompt = getRagStr(query, topK);
            
            // 调用LLM生成回答
            String answer = chatClient.prompt(prompt)
                    .system(SYSTEM_PROMPT)
                    .user(query)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call().content();

            logger.info("知识库对话完成，查询: '{}'", query);
            return answer;
            
        } catch (Exception e) {
            logger.error("知识库对话失败，查询: '{}'", query, e);
            return "对话过程中发生错误: " + e.getMessage();
        }
    }
    
    /**
     * 流式知识库对话
     * 
     * @param query 用户查询
     * @param conversationId 对话ID
     * @param topK 检索文档数量
     * @return 流式回答内容
     */
    @Override
    public Flux<String> chatWithKnowledgeStream(String query, String conversationId, int topK) {
        Assert.hasText(query, "查询问题不能为空");
        logger.info("开始流式知识库对话，查询: '{}', conversationId: {}", query, conversationId);

        try {
            String prompt = getRagStr(query, topK);

            return chatClient.prompt(prompt)
                    .system(SYSTEM_PROMPT)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .user(query)
                    .stream()
                    .content();
        } catch (Exception e) {
             logger.error("流式知识库对话失败，查询: '{}'", query, e);
             return Flux.just("对话过程中发生错误: " + e.getMessage());
         }
    }
    
    /**
     * 相似性搜索
     * 
     * @param query 查询字符串
     * @param topK 返回的相似文档数量
     * @return 相似文档列表
     */
    @Override
    public List<Document> similaritySearch(String query, int topK) {
        Assert.hasText(query, "查询不能为空");
        logger.info("执行相似性搜索: query={}, topK={}", query, topK);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);
        logger.info("相似性搜索完成，找到 {} 个相关文档", results.size());
        return results;
    }
    
    /**
     * 获取RAG提示词
     *
     * @param query 用户查询
     * @param topK 检索文档数量
     * @return 提示词
     */
    private String getRagStr(String query, int topK) {
        List<Document> documents = similaritySearch(query, topK);
        String prompt = "";
        if (documents != null && !documents.isEmpty()){
            // 构建提示词
            String context = documents.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n\n"));
            prompt = String.format("知识库内容：\n%s\n\n", context);
        }
        return prompt;
    }
}
```

### 2. 用户会话管理系统

#### 用户会话信息类

```java
@Data
public class UserSession {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;

    public UserSession() {
    }
    
    public UserSession(String userId, String username) {
        this();
        this.userId = userId;
        this.username = username;
    }
}
```

#### 用户上下文设计（支持TTL）

```java
/**
 * 用户会话持有者
 * 使用阿里TTL（TransmittableThreadLocal）存储当前线程的用户会话信息
 * 支持线程池环境下的上下文传递，确保线程安全
 * 
 * @author Mxy
 */
public class UserSessionHolder {
    
    private static final Logger logger = LoggerFactory.getLogger(UserSessionHolder.class);
    
    /**
     * TransmittableThreadLocal存储用户会话信息
     * 相比ThreadLocal，TTL支持线程池环境下的上下文传递
     */
    private static final TransmittableThreadLocal<UserSession> USER_SESSION_THREAD_LOCAL = new TransmittableThreadLocal<>();
    
    /**
     * 设置当前线程的用户会话
     * 
     * @param userSession 用户会话信息
     */
    public static void setUserSession(UserSession userSession) {
        if (userSession != null) {
            logger.debug("设置用户会话: {}", userSession);
            USER_SESSION_THREAD_LOCAL.set(userSession);
        } else {
            logger.warn("尝试设置空的用户会话");
        }
    }
    
    /**
     * 获取当前线程的用户会话
     * 
     * @return 用户会话信息，如果未设置则返回null
     */
    public static UserSession getUserSession() {
        return USER_SESSION_THREAD_LOCAL.get();
    }
    
    /**
     * 获取当前用户ID
     * 
     * @return 用户ID，如果未设置会话则返回null
     */
    public static String getCurrentUserId() {
        UserSession session = getUserSession();
        return session != null ? session.getUserId() : null;
    }
    
    /**
     * 获取当前用户名
     * 
     * @return 用户名，如果未设置会话则返回null
     */
    public static String getCurrentUsername() {
        UserSession session = getUserSession();
        return session != null ? session.getUsername() : null;
    }
    
    /**
     * 清除当前线程的用户会话
     * 重要：在请求处理完成后必须调用此方法，避免内存泄漏
     */
    public static void clearUserSession() {
        UserSession session = getUserSession();
        if (session != null) {
            logger.debug("清除用户会话: userId={}", session.getUserId());
        }
        USER_SESSION_THREAD_LOCAL.remove();
    }
}
```

#### 用户认证拦截器

```java
/**
 * 用户认证拦截器
 * 拦截所有请求，验证请求头中的userId，并将用户信息存储到UserSession中
 * 
 * @author Mxy
 */
@Component
public class UserAuthInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(UserAuthInterceptor.class);
    
    /**
     * 用户ID请求头名称
     */
    private static final String USER_ID_HEADER = "X-User-Id";
    
    /**
     * 备用用户ID请求头名称
     */
    private static final String USER_ID_HEADER_ALT = "userId";
    
    @Resource
    private UsersDAO usersDAO;
    
    /**
     * 请求处理前的拦截方法
     * 验证用户身份并设置用户会话
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("拦截请求: {} {}", method, requestURI);
        
        // 跳过健康检查和静态资源等不需要认证的请求
        if (shouldSkipAuthentication(requestURI)) {
            logger.debug("跳过认证检查: {}", requestURI);
            return true;
        }
        
        // 从请求头获取用户ID
        String userId = getUserIdFromHeader(request);
        
        if (!StringUtils.hasText(userId)) {
            logger.warn("请求缺少用户ID: {} {}", method, requestURI);
            sendUnauthorizedResponse(response, "缺少用户ID");
            return false;
        }
        
        // 验证用户是否存在
        try {
            UsersDO user = usersDAO.getByUserId(userId);
            if (user == null || user.getDeleted() == 1) {
                logger.warn("用户不存在或已删除: userId={}", userId);
                sendUnauthorizedResponse(response, "用户不存在或已删除");
                return false;
            }
            
            // 创建用户会话并设置到ThreadLocal
            UserSession userSession = createUserSession(user);
            UserSessionHolder.setUserSession(userSession);
            
            logger.debug("用户认证成功: userId={}, username={}", userId, user.getUsername());
            return true;
            
        } catch (Exception e) {
            logger.error("用户认证过程中发生异常: userId={}, error={}", userId, e.getMessage(), e);
            sendUnauthorizedResponse(response, "认证失败");
            return false;
        }
    }
    
    /**
     * 请求完成后的清理方法
     * 清除ThreadLocal中的用户会话，防止内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // 清除用户会话
            UserSessionHolder.clearUserSession();
        } catch (Exception e) {
            logger.error("清除用户会话时发生异常", e);
        }
    }
    
    /**
     * 从请求头获取用户ID
     */
    private String getUserIdFromHeader(HttpServletRequest request) {
        // 优先从X-User-Id头获取
        String userId = request.getHeader(USER_ID_HEADER);
        if (StringUtils.hasText(userId)) {
            return userId.trim();
        }
        
        // 备用方案：从userId头获取
        userId = request.getHeader(USER_ID_HEADER_ALT);
        if (StringUtils.hasText(userId)) {
            return userId.trim();
        }
        
        return null;
    }
    
    /**
     * 创建用户会话对象
     */
    private UserSession createUserSession(UsersDO user) {
        return new UserSession(user.getUserId(), user.getUsername());
    }
    
    /**
     * 判断是否应该跳过认证检查
     */
    private boolean shouldSkipAuthentication(String requestURI) {
        // 健康检查接口
        if (requestURI.contains("/actuator/")) {
            return true;
        }
        
        // 静态资源
        if (requestURI.contains("/static/") || requestURI.contains("/public/")) {
            return true;
        }
        
        // Swagger文档
        if (requestURI.contains("/swagger") || requestURI.contains("/v3/api-docs")) {
            return true;
        }
        
        // 错误页面
        if (requestURI.contains("/error")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        String jsonResponse = String.format(
            "{\"code\":%d,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
            HttpServletResponse.SC_UNAUTHORIZED,
            message,
            System.currentTimeMillis()
        );
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
```

#### Web配置类

```java
/**
 * Web配置类
 * 配置Spring MVC相关设置，包括拦截器注册和页面重定向
 * 
 * @author Mxy
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Resource
    private UserAuthInterceptor userAuthInterceptor;
    
    @Resource(name = "ttlTaskExecutor")
    private ThreadPoolTaskExecutor ttlTaskExecutor;
    
    /**
     * 添加拦截器配置
     * 注册用户认证拦截器，拦截所有API请求进行用户身份验证
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userAuthInterceptor)
                // 拦截所有API请求
                .addPathPatterns("/api/**")
                // 排除不需要认证的路径
                .excludePathPatterns(
                    // 健康检查
                    "/actuator/**",
                    // Swagger文档
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    // 静态资源
                    "/static/**",
                    "/public/**",
                    "/favicon.ico",
                    // 错误页面
                    "/error/**",
                    // 用户认证相关接口（无需登录）
                    "/api/v1/user/register",
                    "/api/v1/user/login",
                    "/api/v1/user/check-username"
                )
                // 设置拦截器顺序（数字越小优先级越高）
                .order(1);
    }
    
    /**
     * 配置CORS跨域支持
     * 允许前端页面跨域访问API接口
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许所有来源（开发环境）
                .allowedOriginPatterns("*")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许发送Cookie
                .allowCredentials(true)
                // 预检请求缓存时间（1小时）
                .maxAge(3600);
    }
    
    /**
     * 配置异步支持
     * 使用自定义的TTL任务执行器替代默认的SimpleAsyncTaskExecutor
     * 解决生产环境下的异步处理性能问题，并保持用户上下文传递
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 设置异步请求的任务执行器
        configurer.setTaskExecutor(ttlTaskExecutor);
        // 设置异步请求超时时间（30秒）
        configurer.setDefaultTimeout(30000);
    }
}
```

#### TTL线程池配置

```java
/**
 * TTL（TransmittableThreadLocal）配置类
 * 配置支持TTL的线程池，确保异步任务中能够正确传递用户上下文
 * 
 * @author Mxy
 */
@Configuration
public class TtlConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(TtlConfig.class);
    
    /**
     * 配置支持TTL的异步任务执行器
     * 使用TTL装饰器包装线程池，确保异步任务中能够获取到主线程的用户上下文
     */
    @Bean("ttlTaskExecutor")
    public ThreadPoolTaskExecutor ttlTaskExecutor() {
        logger.info("初始化支持TTL的线程池执行器");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(20);
        // 队列容量
        executor.setQueueCapacity(100);
        // 线程名前缀
        executor.setThreadNamePrefix("ttl-async-");
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        // 使用TTL装饰器包装线程池，支持上下文传递
        return executor;
    }
    
    /**
     * 配置支持TTL的调度任务执行器
     * 用于定时任务等场景
     */
    @Bean("ttlScheduledExecutor")
    public Executor ttlScheduledExecutor() {
        logger.info("初始化支持TTL的调度线程池执行器");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ttl-scheduled-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        return TtlExecutors.getTtlExecutor(executor.getThreadPoolExecutor());
    }
}
```

#### 用户上下文工具类

```java
/**
 * 用户上下文工具类
 * 提供便捷的方法获取当前用户信息
 * 
 * @author Mxy
 */
public class UserContextUtil {
    
    /**
     * 获取当前用户ID
     * 
     * @return 当前用户ID
     */
    public static String getCurrentUserId() {
        return UserSessionHolder.getCurrentUserId();
    }
    
    /**
     * 获取当前用户名
     * 
     * @return 当前用户名
     */
    public static String getCurrentUsername() {
        return UserSessionHolder.getCurrentUsername();
    }
    
    /**
     * 获取当前用户会话
     * 
     * @return 当前用户会话
     */
    public static UserSession getCurrentUserSession() {
        return UserSessionHolder.getUserSession();
    }
}
```

### 3. 控制器层实现

#### 智能对话控制器

```java
/**
 * 智能对话控制器
 * 提供智能问答、流式对话、对话历史查询等REST API接口
 *
 * @author Mxy
 */
@Tag(name = "智能对话管理", description = "提供智能问答、流式对话、对话历史查询等功能")
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Resource
    private ChatService chatService;

    /**
     * 智能问答（阻塞式）
     * 基于知识库进行问答，返回完整的回答结果
     *
     * @param request 问答请求参数
     * @return 问答结果
     */
    @Operation(summary = "智能问答", description = "基于知识库进行智能问答，返回完整的回答结果")
    @PostMapping("/ask")
    public ApiResult<String> askQuestion(
            @Parameter(description = "问答请求参数", required = true)
            @Valid @RequestBody ChatAskRequest request) {
        try {
            String currentUsername = UserContextUtil.getCurrentUsername();
            logger.info("接收智能问答请求: sessionId={}, currentUsername={}, question={}",
                    request.getSessionId(), currentUsername, request.getQuestion());

            // 转换为DTO
            ChatAskDTO dto = new ChatAskDTO();
            BeanUtils.copyProperties(request, dto);

            String result = chatService.askQuestion(dto);
            return ApiResult.success("问答成功", result);
        } catch (Exception e) {
            logger.error("智能问答失败: {}", e.getMessage(), e);
            return ApiResult.error("智能问答失败: " + e.getMessage());
        }
    }

    /**
     * 流式智能问答
     * 基于知识库进行问答，以流的形式返回回答内容
     *
     * @param request 流式问答请求参数
     * @return 流式回答内容
     */
    @Operation(summary = "流式智能问答", description = "基于知识库进行智能问答，以流的形式返回回答内容")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> askQuestionStream(
            @Parameter(description = "流式问答请求参数", required = true)
            @Valid @RequestBody ChatAskRequest request) {
        try {
            String currentUsername = UserContextUtil.getCurrentUsername();
            logger.info("接收流式智能问答请求: sessionId={}, currentUsername={}, question={}",
                    request.getSessionId(), currentUsername, request.getQuestion());

            // 转换为DTO
            ChatAskDTO dto = new ChatAskDTO();
            BeanUtils.copyProperties(request, dto);

            return chatService.askQuestionStream(dto);
        } catch (Exception e) {
            logger.error("流式智能问答失败: {}", e.getMessage(), e);
            return Flux.error(new RuntimeException("流式智能问答失败: " + e.getMessage()));
        }
    }

    /**
     * 获取对话历史
     * 分页查询指定会话的对话历史记录
     *
     * @return 分页的对话历史
     */
    @Operation(summary = "获取对话历史", description = "分页查询指定会话的对话历史记录")
    @PostMapping("/getChatHistory")
    public ApiResult<PageResult<ChatMessageVO>> getChatHistory(@RequestBody ChatMessagePageRequest chatMessagePageRequest) {
        try {
            logger.info("获取对话历史: sessionId={}, pageNum={}, pageSize={}",
                    chatMessagePageRequest.getSessionId(),
                    chatMessagePageRequest.getPageNum(), chatMessagePageRequest.getPageSize());
            ChatMessagePageRequestDTO chatMessagePageRequestDTO = new ChatMessagePageRequestDTO();
            BeanUtils.copyProperties(chatMessagePageRequest, chatMessagePageRequestDTO);

            PageResult<ChatMessageVO> result = chatService.getChatHistory(chatMessagePageRequestDTO);
            return ApiResult.success(result);
        } catch (Exception e) {
            logger.error("获取对话历史失败: {}", e.getMessage(), e);
            return ApiResult.error("获取对话历史失败: " + e.getMessage());
        }
    }

    /**
     * 用户反馈
     * 用户对AI回答进行评分和反馈
     *
     * @param request 反馈请求参数
     * @return 反馈处理结果
     */
    @Operation(summary = "用户反馈", description = "用户对AI回答进行评分和反馈")
    @PostMapping("/feedback")
    public ApiResult<Void> submitFeedback(
            @Parameter(description = "反馈请求参数", required = true)
            @Valid @RequestBody ChatFeedbackRequest request) {
        try {
            logger.info("接收用户反馈: messageId={}, rating={}",
                    request.getMessageId(), request.getRating());

            // 转换为DTO
            ChatFeedbackDTO dto = new ChatFeedbackDTO();
            BeanUtils.copyProperties(request, dto);

            chatService.submitFeedback(dto);
            return ApiResult.success();
        } catch (Exception e) {
            logger.error("用户反馈失败: {}", e.getMessage(), e);
            return ApiResult.error("用户反馈失败: " + e.getMessage());
        }
    }

    /**
     * 自动生成会话标题
     * 根据会话的聊天记录自动生成合适的标题
     *
     * @param sessionId 会话ID
     * @return 生成的标题
     */
    @Operation(summary = "自动生成会话标题", description = "根据会话的聊天记录自动生成合适的标题")
    @PostMapping("/generateTitle/{sessionId}")
    public ApiResult<String> generateSessionTitle(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long sessionId) {
        try {
            logger.info("生成会话标题: sessionId={}", sessionId);
            
            String title = chatService.generateSessionTitle(sessionId);
            return ApiResult.success("标题生成成功", title);
        } catch (Exception e) {
            logger.error("生成会话标题失败: {}", e.getMessage(), e);
            return ApiResult.error("生成会话标题失败: " + e.getMessage());
        }
    }
}
```

#### 聊天会话管理控制器

```java
/**
 * 聊天会话管理控制器
 * 提供会话的创建、查询、更新、删除等REST API接口
 *
 * @author Mxy
 */
@Tag(name = "聊天会话管理", description = "提供聊天会话的创建、查询、更新、删除等功能")
@RestController
@RequestMapping("/api/v1/chat/sessions")
public class ChatSessionController {

    private static final Logger logger = LoggerFactory.getLogger(ChatSessionController.class);

    @Resource
    private ChatSessionService chatSessionService;

    /**
     * 创建新的聊天会话
     *
     * @param request 创建会话请求参数
     * @return 创建的会话信息
     */
    @Operation(summary = "创建聊天会话", description = "创建一个新的聊天会话")
    @PostMapping("/create")
    public ApiResult<Long> createSession(
            @Parameter(description = "创建会话请求参数", required = true)
            @Valid @RequestBody CreateSessionRequest request) {
        try {
            // 从用户上下文获取当前用户ID
            String currentUserId = UserContextUtil.getCurrentUserId();
            logger.info("接收创建会话请求: userId={}, title={}", currentUserId, request.getTitle());

            // 转换为DTO
            CreateSessionDTO dto = new CreateSessionDTO();
            BeanUtils.copyProperties(request, dto);
            Long sessionId = chatSessionService.createSession(dto);
            return ApiResult.success(sessionId);
        } catch (Exception e) {
            logger.error("创建会话失败: {}", e.getMessage(), e);
            return ApiResult.error("创建会话失败: " + e.getMessage());
        }
    }

    /**
     * 根据会话ID获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话详情
     */
    @Operation(summary = "获取会话详情", description = "根据会话ID获取会话的详细信息")
    @GetMapping("/detail/{sessionId}")
    public ApiResult<SessionVO> getSessionById(
            @Parameter(description = "会话ID", required = true) @PathVariable Long sessionId) {
        try {
            // 从用户上下文获取当前用户ID
            String currentUserId = UserContextUtil.getCurrentUserId();
            logger.info("获取会话详情: sessionId={}, userId={}", sessionId, currentUserId);
            SessionVO sessionVO = chatSessionService.getSessionById(sessionId);
            return ApiResult.success(sessionVO);
        } catch (Exception e) {
            logger.error("获取会话详情失败: {}", e.getMessage(), e);
            return ApiResult.error("获取会话详情失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询用户的会话列表
     *
     * @param request 查询请求参数
     * @return 分页查询结果
     */
    @Operation(summary = "查询会话列表", description = "分页查询用户的会话列表，支持关键词搜索和状态过滤")
    @PostMapping("/list")
    public ApiResult<PageResult<SessionVO>> getSessionList(
            @Parameter(description = "查询请求参数", required = true)
            @Valid @RequestBody SessionQueryRequest request) {
        try {
            // 从用户上下文获取当前用户ID
            String currentUserId = UserContextUtil.getCurrentUserId();
            logger.info("查询会话列表:pageNum={}, pageSize={}", request.getPageNum(), request.getPageSize());

            // 转换为DTO
            SessionQueryDTO dto = new SessionQueryDTO();
            BeanUtils.copyProperties(request, dto);
            PageResult<SessionVO> result = chatSessionService.getSessionList(dto);
            return ApiResult.success(result);
        } catch (Exception e) {
            logger.error("查询会话列表失败: {}", e.getMessage(), e);
            return ApiResult.error("查询会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新会话标题
     *
     * @param request 更新标题请求参数（包含sessionId、userId和title）
     * @return 更新后的会话信息
     */
    @Operation(summary = "更新会话标题", description = "更新指定会话的标题")
    @PostMapping("/update-title")
    public ApiResult<SessionVO> updateSessionTitle(
            @Parameter(description = "更新标题请求参数", required = true)
            @Valid @RequestBody UpdateSessionTitleRequest request) {
        try {
            // 从用户上下文获取当前用户ID
            String currentUserId = UserContextUtil.getCurrentUserId();
            logger.info("更新会话标题: sessionId={}, userId={}, title={}",
                    request.getSessionId(), currentUserId, request.getTitle());

            // 转换为DTO
            UpdateSessionTitleDTO dto = new UpdateSessionTitleDTO();
            BeanUtils.copyProperties(request, dto);

            chatSessionService.updateSessionTitle(dto);
            return ApiResult.success();
        } catch (Exception e) {
            logger.error("更新会话标题失败: {}", e.getMessage(), e);
            return ApiResult.error("更新会话标题失败: " + e.getMessage());
        }
    }

    /**
     * 删除会话
     *
     * @param request 删除会话请求参数
     * @return 删除结果
     */
    @Operation(summary = "删除会话", description = "删除指定的会话")
    @PostMapping("/delete")
    public ApiResult<Void> deleteSession(
            @Parameter(description = "删除会话请求参数", required = true)
            @Valid @RequestBody DeleteSessionRequest request) {
        try {
            // 从用户上下文获取当前用户ID
            String currentUserId = UserContextUtil.getCurrentUserId();
            logger.info("删除会话: sessionId={}, userId={}", request.getSessionId(), currentUserId);

            // 转换为DTO
            DeleteSessionDTO dto = new DeleteSessionDTO();
            BeanUtils.copyProperties(request, dto);
            dto.setUserId(currentUserId);

            chatSessionService.deleteSession(dto);
            return ApiResult.success();
        } catch (Exception e) {
            logger.error("删除会话失败: {}", e.getMessage(), e);
            return ApiResult.error("删除会话失败: " + e.getMessage());
        }
    }
}
```

### 4. 向量数据库集成

#### 多数据源配置

```java
/**
 * 多数据源配置类
 * 配置MySQL业务数据库和PostgreSQL向量数据库
 * 
 * @author Mxy
 */
@Configuration
@Slf4j
public class MultiDataSourceConfig {
    
    /**
     * 配置向量数据库数据源属性
     * 使用@ConfigurationProperties注解绑定配置文件中的属性
     */
    @Bean(name = "pgVectorDataSourceProperties")
    @ConfigurationProperties("spring.datasource.pgvector")
    public DataSourceProperties pgVectorDataSourceProperties() {
        return new DataSourceProperties();
    }
    
    /**
     * 初始化向量数据库数据源
     * 使用HikariDataSource确保属性正确绑定
     */
    @Bean("pgVectorDataSource")
    public DataSource vectorDataSource(@Qualifier("pgVectorDataSourceProperties") DataSourceProperties dataSourceProperties) {
        log.info("初始化向量数据库");
        return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    /**
     * 配置向量数据库JdbcTemplate
     * 用于执行向量相关的SQL操作
     */
    @Bean
    @Primary
    public JdbcTemplate pgVectorJdbcTemplate(@Qualifier("pgVectorDataSource") DataSource dataSource) {
        log.info("初始化JdbcTemplate");
        return new JdbcTemplate(dataSource);
    }

    /**
     * 配置MySQL业务数据库数据源属性
     */
    @Primary
    @Bean(name = "mysqlDataSourceProperties")
    @ConfigurationProperties("spring.datasource.mysql")
    public DataSourceProperties mysqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * 初始化MySQL业务数据源
     * 使用HikariDataSource确保属性正确绑定
     */
    @Bean("mysqlDataSource")
    @Primary
    public DataSource masterDataSource(@Qualifier("mysqlDataSourceProperties")
                                           DataSourceProperties dataSourceProperties) {
        log.info("初始化主数据源");
        return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    /**
     * 配置MyBatis SqlSessionFactory
     * 用于MySQL数据库的ORM操作
     */
    @Bean("sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("mysqlDataSource") DataSource mysqlDataSource) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(mysqlDataSource);
        // 设置mapper文件位置
        sessionFactory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*Mapper*.xml"));
        // 设置MyBatis Plus插件
        sessionFactory.setPlugins(mybatisPlusInterceptor());
        log.info("初始化SqlSessionFactory");
        return sessionFactory.getObject();
    }

    /**
     * 配置事务管理器
     * 管理MySQL数据库的事务
     */
    @Bean("transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(@Qualifier("mysqlDataSource") DataSource mysqlDataSource) {
        log.info("初始化事务管理器");
        return new DataSourceTransactionManager(mysqlDataSource);
    }

    /**
     * 配置MyBatis Plus插件
     * 包括分页插件等功能增强
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setMaxLimit(1000L); // 设置最大分页限制
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        log.info("初始化MyBatis Plus拦截器");
        return interceptor;
    }
}
```

**自定义聊天记忆仓库：**
```java
@Component
public class CustomChatMemoryRepository implements ChatMemoryRepository {

    private final ChatSessionsMapper chatSessionsMapper;
    private final ChatMessagesMapper chatMessagesMapper;

    public CustomChatMemoryRepository(ChatSessionsMapper chatSessionsMapper, ChatMessagesMapper chatMessagesMapper) {
        this.chatSessionsMapper = chatSessionsMapper;
        this.chatMessagesMapper = chatMessagesMapper;
    }

    @Override
    public List<Message> getMessages(String conversationId) {
        List<ChatMessagesDO> messageDOs = chatMessagesMapper.findBySessionId(conversationId);
        return messageDOs.stream()
                .map(this::convertToMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void saveMessages(String conversationId, List<Message> messages) {
        for (Message message : messages) {
            ChatMessagesDO messageDO = convertToChatMessagesDO(conversationId, message);
            chatMessagesMapper.insert(messageDO);
        }
    }

    @Override
    public void deleteMessages(String conversationId) {
        chatMessagesMapper.deleteBySessionId(conversationId);
    }
}
```

#### 文档处理服务

```java
@Override
public void loadFile(MultipartFile file) {
    try {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        logger.info("开始处理文件: {}", originalFilename);

        List<Document> documents;
        String fileExtension = getFileExtension(originalFilename).toLowerCase();

        if ("pdf".equals(fileExtension)) {
            // PDF文件处理
            PdfDocumentReader pdfReader = new PdfDocumentReader(file.getResource());
            documents = pdfReader.get();
        } else {
            // 其他文件类型使用Tika处理
            TikaDocumentReader tikaReader = new TikaDocumentReader(file.getResource());
            documents = tikaReader.get();
        }

        if (documents.isEmpty()) {
            throw new RuntimeException("未能从文件中提取到任何内容");
        }

        // 文本分块处理
        TextSplitter textSplitter = new TokenTextSplitter(500, 100, 5, 10000, true);
        List<Document> splitDocuments = textSplitter.apply(documents);

        // 添加元数据
        for (Document doc : splitDocuments) {
            doc.getMetadata().put("source", originalFilename);
            doc.getMetadata().put("upload_time", System.currentTimeMillis());
        }

        // 存储到向量数据库
        vectorStore.add(splitDocuments);
        logger.info("文件 {} 处理完成，共生成 {} 个文档块", originalFilename, splitDocuments.size());

    } catch (Exception e) {
        logger.error("文件处理失败: {}", file.getOriginalFilename(), e);
        throw new RuntimeException("文件处理失败: " + e.getMessage(), e);
    }
}

private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf('.');
    return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
}
```



## 🎨 前端实现详解

### 1. 项目结构

前端采用原生HTML+CSS+JavaScript实现，无框架依赖，结构清晰简洁：

```
src/main/resources/static/
├── css/
│   ├── common.css      # 公共样式
│   ├── login.css       # 登录页样式
│   ├── chat.css        # 聊天页样式
│   └── knowledge.css   # 知识库管理样式
├── js/
│   └── api.js          # API客户端封装
├── login.html          # 登录注册页面
├── chat.html           # 智能聊天页面
└── knowledge.html      # 知识库管理页面
```

### 2. API客户端封装

**api.js - 统一的HTTP请求处理：**

```javascript
class ApiClient {
    constructor() {
        this.baseURL = '';
        this.token = localStorage.getItem('token');
        this.userId = localStorage.getItem('userId');
    }

    // 通用请求方法
    async request(url, options = {}) {
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };

        // 添加认证头
        if (this.token) {
            config.headers['Authorization'] = `Bearer ${this.token}`;
        }
        if (this.userId) {
            config.headers['X-User-Id'] = this.userId;
        }

        try {
            const response = await fetch(this.baseURL + url, config);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            }
            return await response.text();
        } catch (error) {
            console.error('API请求失败:', error);
            throw error;
        }
    }

    // GET请求
    async get(url, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const fullUrl = queryString ? `${url}?${queryString}` : url;
        return this.request(fullUrl, { method: 'GET' });
    }

    // POST请求
    async post(url, data = {}) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    // 文件上传
    async uploadFile(url, file, onProgress) {
        return new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            const formData = new FormData();
            formData.append('file', file);

            // 设置认证头
            if (this.token) {
                xhr.setRequestHeader('Authorization', `Bearer ${this.token}`);
            }
            if (this.userId) {
                xhr.setRequestHeader('X-User-Id', this.userId);
            }

            // 上传进度回调
            if (onProgress) {
                xhr.upload.onprogress = (e) => {
                    if (e.lengthComputable) {
                        const percentComplete = (e.loaded / e.total) * 100;
                        onProgress(percentComplete);
                    }
                };
            }

            xhr.onload = () => {
                if (xhr.status === 200) {
                    resolve(xhr.responseText);
                } else {
                    reject(new Error(`上传失败: ${xhr.statusText}`));
                }
            };

            xhr.onerror = () => reject(new Error('网络错误'));
            xhr.open('POST', this.baseURL + url);
            xhr.send(formData);
        });
    }
}

// 全局API客户端实例
const api = new ApiClient();
```

### 3. 智能聊天界面实现

**chat.html - 核心聊天功能：**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>智能知识助手</title>
    <link rel="stylesheet" href="css/common.css">
    <link rel="stylesheet" href="css/chat.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/marked/4.3.0/marked.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/highlight.min.js"></script>
</head>
<body>
    <div class="chat-container">
        <!-- 聊天消息区域 -->
        <div class="chat-messages" id="chatMessages">
            <div class="welcome-message">
                <i class="fas fa-robot"></i>
                <h3>欢迎使用智能知识助手！</h3>
                <p>我可以帮您解答问题，分析文档内容。请输入您的问题开始对话。</p>
            </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input-container">
            <div class="input-wrapper">
                <textarea id="messageInput" placeholder="请输入您的问题..." rows="1"></textarea>
                <button id="sendButton" class="send-btn">
                    <i class="fas fa-paper-plane"></i>
                </button>
            </div>
        </div>
    </div>

    <script src="js/api.js"></script>
    <script>
        class ChatApp {
            constructor() {
                this.messagesContainer = document.getElementById('chatMessages');
                this.messageInput = document.getElementById('messageInput');
                this.sendButton = document.getElementById('sendButton');
                this.isLoading = false;
                
                this.initEventListeners();
                this.autoResizeTextarea();
            }

            initEventListeners() {
                // 发送按钮点击事件
                this.sendButton.addEventListener('click', () => this.sendMessage());
                
                // 回车发送消息
                this.messageInput.addEventListener('keydown', (e) => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                        e.preventDefault();
                        this.sendMessage();
                    }
                });
            }

            async sendMessage() {
                const message = this.messageInput.value.trim();
                if (!message || this.isLoading) return;

                // 添加用户消息
                this.addMessage(message, 'user');
                this.messageInput.value = '';
                this.isLoading = true;
                this.updateSendButton();

                // 添加加载消息
                const loadingId = this.addLoadingMessage();

                try {
                    // 调用聊天API
                    const response = await api.post('/api/chat', {
                        message: message,
                        useKnowledgeBase: true
                    });

                    // 移除加载消息
                    this.removeMessage(loadingId);
                    
                    // 添加AI回复
                    this.addMessage(response.content || response, 'assistant');
                } catch (error) {
                    console.error('发送消息失败:', error);
                    this.removeMessage(loadingId);
                    this.addMessage('抱歉，发生了错误，请稍后重试。', 'assistant', true);
                } finally {
                    this.isLoading = false;
                    this.updateSendButton();
                }
            }

            addMessage(content, role, isError = false) {
                const messageId = 'msg-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
                const messageDiv = document.createElement('div');
                messageDiv.className = `message ${role}-message ${isError ? 'error' : ''}`;
                messageDiv.id = messageId;

                const avatar = document.createElement('div');
                avatar.className = 'message-avatar';
                avatar.innerHTML = role === 'user' ? 
                    '<i class="fas fa-user"></i>' : 
                    '<i class="fas fa-robot"></i>';

                const messageContent = document.createElement('div');
                messageContent.className = 'message-content';
                
                if (role === 'assistant' && !isError) {
                    // 使用marked.js渲染Markdown
                    messageContent.innerHTML = marked.parse(content);
                    // 代码高亮
                    messageContent.querySelectorAll('pre code').forEach(block => {
                        hljs.highlightElement(block);
                    });
                } else {
                    messageContent.textContent = content;
                }

                messageDiv.appendChild(avatar);
                messageDiv.appendChild(messageContent);
                this.messagesContainer.appendChild(messageDiv);
                
                // 滚动到底部
                this.scrollToBottom();
                
                return messageId;
            }

            addLoadingMessage() {
                const messageId = 'loading-' + Date.now();
                const messageDiv = document.createElement('div');
                messageDiv.className = 'message assistant-message loading';
                messageDiv.id = messageId;

                messageDiv.innerHTML = `
                    <div class="message-avatar">
                        <i class="fas fa-robot"></i>
                    </div>
                    <div class="message-content">
                        <div class="typing-indicator">
                            <span></span><span></span><span></span>
                        </div>
                    </div>
                `;

                this.messagesContainer.appendChild(messageDiv);
                this.scrollToBottom();
                return messageId;
            }

            removeMessage(messageId) {
                const message = document.getElementById(messageId);
                if (message) {
                    message.remove();
                }
            }

            updateSendButton() {
                this.sendButton.disabled = this.isLoading;
                this.sendButton.innerHTML = this.isLoading ? 
                    '<i class="fas fa-spinner fa-spin"></i>' : 
                    '<i class="fas fa-paper-plane"></i>';
            }

            scrollToBottom() {
                this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
            }

            autoResizeTextarea() {
                this.messageInput.addEventListener('input', () => {
                    this.messageInput.style.height = 'auto';
                    this.messageInput.style.height = Math.min(this.messageInput.scrollHeight, 120) + 'px';
                });
            }
        }

        // 初始化聊天应用
        document.addEventListener('DOMContentLoaded', () => {
            new ChatApp();
        });
    </script>
</body>
</html>
```

## 🗄️ 数据库设计

### PostgreSQL（向量数据）

```sql
-- 启用pgvector扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 向量存储表（Spring AI默认表结构）
CREATE TABLE vector_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content TEXT NOT NULL,
    metadata JSON,
    embedding vector(1536)
);

-- 创建向量索引（余弦相似度）
CREATE INDEX vector_store_embedding_idx ON vector_store 
USING hnsw (embedding vector_cosine_ops);

-- 创建内容全文搜索索引
CREATE INDEX vector_store_content_idx ON vector_store 
USING gin (to_tsvector('english', content));
```

### MySQL（业务数据）

```sql
-- 用户表
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(100) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(255) COMMENT '邮箱',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 聊天会话表
CREATE TABLE chat_sessions (
    id VARCHAR(50) PRIMARY KEY COMMENT '会话ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    title VARCHAR(255) NOT NULL COMMENT '会话标题',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE chat_messages (
    id VARCHAR(50) PRIMARY KEY COMMENT '消息ID',
    session_id VARCHAR(50) NOT NULL COMMENT '会话ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '消息内容',
    message_type VARCHAR(20) NOT NULL COMMENT '消息类型：USER/ASSISTANT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 系统配置表
CREATE TABLE system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) COMMENT '配置描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
```

## 🚀 部署与运维

### 生产环境配置

**application-prod.yaml：**
```yaml
spring:
  datasource:
    pg-vector:
      driver-class-name: org.postgresql.Driver
      url: jdbc:postgresql://${PG_HOST:localhost}:${PG_PORT:5432}/${PG_DATABASE:rag_vector}
      username: ${PG_USERNAME:postgres}
      password: ${PG_PASSWORD:password}
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5
        idle-timeout: 300000
        max-lifetime: 1200000
        connection-timeout: 20000
    mysql:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:rag_business}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      username: ${MYSQL_USERNAME:root}
      password: ${MYSQL_PASSWORD:password}
      hikari:
        maximum-pool-size: 30
        minimum-idle: 10
        idle-timeout: 300000
        max-lifetime: 1200000
        connection-timeout: 20000
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        options:
          model: ${DASHSCOPE_MODEL:qwen-plus}
          temperature: ${DASHSCOPE_TEMPERATURE:0.7}

logging:
  level:
    com.mxy.rag: INFO
    org.springframework.ai: INFO
  file:
    name: logs/mxy-rag-server.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /
  tomcat:
    max-threads: 200
    min-spare-threads: 10
```

### Docker部署

**Dockerfile：**
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# 复制jar文件
COPY target/mxy-rag-server-*.jar app.jar

# 创建日志目录
RUN mkdir -p logs

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-Xms512m", "-Xmx2g", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

### Docker Compose配置

**docker-compose.yml：**

```yaml
version: '3.8'
services:
  app:
    build: ..
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DASHSCOPE_API_KEY=${DASHSCOPE_API_KEY}
      - PG_HOST=postgres
      - PG_PORT=5432
      - PG_DATABASE=rag_vector
      - PG_USERNAME=postgres
      - PG_PASSWORD=postgres123
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - MYSQL_DATABASE=rag_business
      - MYSQL_USERNAME=root
      - MYSQL_PASSWORD=mysql123
    depends_on:
      - postgres
      - mysql
    volumes:
      - ./logs:/app/logs
    restart: unless-stopped

  postgres:
    image: pgvector/pgvector:pg16
    environment:
      - POSTGRES_DB=rag_vector
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-scripts/init-postgres.sql:/docker-entrypoint-initdb.d/init.sql
    restart: unless-stopped

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=mysql123
      - MYSQL_DATABASE=rag_business
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init-scripts/init-mysql.sql:/docker-entrypoint-initdb.d/init.sql
    restart: unless-stopped

volumes:
  postgres_data:
  mysql_data:
```

## 📝 总结

本文详细介绍了基于Spring AI和阿里云DashScope构建RAG系统的完整实现方案。主要特点包括：

### 🎯 核心特性
- **多模型支持**：集成阿里云DashScope，支持通义千问系列模型
- **向量检索**：基于pgvector的高效向量存储和检索
- **多数据源**：PostgreSQL存储向量数据，MySQL存储业务数据
- **聊天记忆**：支持多轮对话的上下文记忆
- **文档处理**：支持PDF、Word等多种文档格式
- **用户认证**：完整的用户管理和会话管理

### 🛠️ 技术栈
- **后端框架**：Spring Boot 3.x + Spring AI Alibaba
- **AI模型**：阿里云DashScope（通义千问）
- **向量数据库**：PostgreSQL + pgvector
- **业务数据库**：MySQL 8.0
- **ORM框架**：MyBatis Plus
- **文档处理**：Apache Tika + Spring AI Document Readers

### 🚀 部署方式
- **容器化部署**：Docker + Docker Compose
- **生产环境**：支持环境变量配置
- **日志管理**：结构化日志输出
- **监控运维**：完整的错误处理和日志记录

通过本方案，可以快速构建一个功能完整、性能优异的企业级RAG系统，为知识管理和智能问答提供强有力的技术支撑。


**项目地址**：[https://github.com/Matthew-Miao/mxy-rag-server](https://github.com/Matthew-Miao/mxy-rag-server)

欢迎Star和Fork，一起探讨AI应用开发的最佳实践！
*如果这篇文章对你有帮助，请点赞、收藏并关注，我会持续分享更多AI开发实战经验！*