# 用户拦截层设计文档

## 概述

本文档描述了用户拦截层的设计和实现，该拦截层用于验证请求头中的用户ID，确保用户存在，并将用户信息存储到全局UserSession中，供后续业务逻辑使用。

## 架构设计

### 核心组件

1. **UserSession** - 用户会话信息类
2. **UserSessionHolder** - 用户会话持有者（ThreadLocal管理）
3. **UserAuthInterceptor** - 用户认证拦截器
4. **WebConfig** - Web配置类
5. **UserContextUtil** - 用户上下文工具类

### 组件关系图

```
请求 → UserAuthInterceptor → 验证用户ID → 查询用户信息 → 创建UserSession → 存储到ThreadLocal
                ↓
            业务Controller → UserContextUtil → UserSessionHolder → 获取用户信息
                ↓
            请求完成 → 清理ThreadLocal
```

## 详细设计

### 1. UserSession 用户会话信息类

**位置**: `com.mxy.ai.rag.web.session.UserSession`

**功能**: 存储用户会话相关信息

**主要字段**:
- `userId` - 用户ID
- `username` - 用户名
- `usersDO` - 用户实体对象
- `sessionCreateTime` - 会话创建时间
- `lastAccessTime` - 最后访问时间
- `ipAddress` - 客户端IP地址
- `userAgent` - 客户端User-Agent

**主要方法**:
- `updateLastAccessTime()` - 更新最后访问时间
- `isAuthenticated()` - 检查用户是否已认证

### 2. UserSessionHolder 用户会话持有者

**位置**: `com.mxy.ai.rag.web.session.UserSessionHolder`

**功能**: 使用ThreadLocal管理用户会话信息，确保线程安全

**主要方法**:
- `setUserSession(UserSession)` - 设置当前线程的用户会话
- `getUserSession()` - 获取当前线程的用户会话
- `getCurrentUserId()` - 获取当前用户ID
- `getCurrentUsername()` - 获取当前用户名
- `isCurrentUserAuthenticated()` - 检查当前用户是否已认证
- `clearUserSession()` - 清除当前线程的用户会话

### 3. UserAuthInterceptor 用户认证拦截器

**位置**: `com.mxy.ai.rag.web.interceptor.UserAuthInterceptor`

**功能**: 拦截所有API请求，验证用户身份并设置用户会话

**拦截流程**:
1. **preHandle**: 请求前验证
   - 检查是否需要跳过认证（健康检查、静态资源等）
   - 从请求头获取用户ID（支持 `X-User-Id` 和 `userId` 两种头）
   - 验证用户是否存在且未删除
   - 创建UserSession并存储到ThreadLocal

2. **postHandle**: 请求后处理
   - 更新用户最后访问时间

3. **afterCompletion**: 请求完成后清理
   - 清除ThreadLocal中的用户会话，防止内存泄漏

**支持的请求头**:
- `X-User-Id`: 主要的用户ID请求头
- `userId`: 备用的用户ID请求头

**跳过认证的路径**:
- `/actuator/**` - 健康检查
- `/static/**`, `/public/**` - 静态资源
- `/swagger**`, `/v3/api-docs/**` - API文档
- `/error/**` - 错误页面

### 4. WebConfig Web配置类

**位置**: `com.mxy.ai.rag.config.session.WebConfig`

**功能**: 注册用户认证拦截器到Spring MVC配置

**配置内容**:
- 拦截路径: `/api/**`
- 排除路径: 健康检查、静态资源、API文档等
- 拦截器顺序: 1（高优先级）

### 5. UserContextUtil 用户上下文工具类

**位置**: `com.mxy.ai.rag.web.util.UserContextUtil`

**功能**: 提供便捷的方法来获取当前用户信息和执行用户相关操作

**主要方法**:
- `getCurrentUserId()` - 获取当前用户ID
- `getCurrentUsername()` - 获取当前用户名
- `getCurrentUser()` - 获取当前用户实体对象
- `isUserLoggedIn()` - 检查当前用户是否已登录
- `requireLogin()` - 要求用户必须已登录
- `requireCurrentUser(String userId)` - 要求指定用户ID必须为当前用户
- `logUserOperation(String operation)` - 记录用户操作日志

## 使用方法

### 1. 客户端请求

客户端在发送API请求时，需要在请求头中包含用户ID：

```http
GET /api/v1/chat/sessions/list
X-User-Id: user123
Content-Type: application/json
```

或者使用备用头：

```http
GET /api/v1/chat/sessions/list
userId: user123
Content-Type: application/json
```

### 2. 在Controller中使用

```java
@RestController
@RequestMapping("/api/v1/example")
public class ExampleController {
    
    @GetMapping("/user-info")
    public ApiResult<UserInfo> getUserInfo() {
        // 获取当前用户ID
        String userId = UserContextUtil.getCurrentUserId();
        
        // 获取当前用户实体
        UsersDO user = UserContextUtil.getCurrentUser();
        
        // 检查用户是否登录
        if (!UserContextUtil.isUserLoggedIn()) {
            return ApiResult.error("用户未登录");
        }
        
        // 记录用户操作
        UserContextUtil.logUserOperation("获取用户信息");
        
        return ApiResult.success(user);
    }
    
    @PostMapping("/secure-operation")
    public ApiResult<String> secureOperation(@RequestBody SomeRequest request) {
        // 要求用户必须登录
        UserContextUtil.requireLogin();
        
        // 要求操作的资源必须属于当前用户
        UserContextUtil.requireCurrentUser(request.getUserId());
        
        // 执行业务逻辑
        // ...
        
        return ApiResult.success("操作成功");
    }
}
```

### 3. 在Service中使用

```java
@Service
public class ExampleService {
    
    public void doSomething() {
        // 获取当前用户ID
        String currentUserId = UserContextUtil.getCurrentUserId();
        
        // 记录操作日志
        UserContextUtil.logUserOperation("执行某项操作", "操作详情");
        
        // 业务逻辑
        // ...
    }
}
```

## 错误处理

### 1. 用户ID缺失

当请求头中没有用户ID时，拦截器会返回401状态码：

```json
{
  "code": 401,
  "message": "缺少用户ID",
  "data": null,
  "timestamp": 1703123456789
}
```

### 2. 用户不存在

当用户ID对应的用户不存在或已删除时：

```json
{
  "code": 401,
  "message": "用户不存在或已删除",
  "data": null,
  "timestamp": 1703123456789
}
```

### 3. 认证异常

当认证过程中发生异常时：

```json
{
  "code": 401,
  "message": "认证失败",
  "data": null,
  "timestamp": 1703123456789
}
```

## 安全考虑

### 1. 线程安全

- 使用ThreadLocal确保用户会话信息的线程安全
- 在请求完成后自动清理ThreadLocal，防止内存泄漏

### 2. 用户验证

- 每次请求都会验证用户是否存在且未删除
- 支持软删除用户的过滤

### 3. IP地址记录

- 记录用户的真实IP地址，支持代理和负载均衡环境
- 支持多种IP头的解析（X-Forwarded-For、X-Real-IP等）

### 4. 操作日志

- 提供用户操作日志记录功能
- 包含用户ID、用户名、IP地址、操作描述等信息

## 性能考虑

### 1. 数据库查询优化

- 每次请求都会查询用户表，建议在用户表的ID字段上建立索引
- 可以考虑添加Redis缓存来减少数据库查询

### 2. 内存管理

- 使用ThreadLocal存储用户会话，内存占用较小
- 请求完成后自动清理，避免内存泄漏

## 扩展建议

### 1. 添加缓存支持

```java
@Component
public class CachedUserAuthInterceptor extends UserAuthInterceptor {
    
    @Resource
    private RedisTemplate<String, UsersDO> redisTemplate;
    
    private UsersDO getUserFromCacheOrDB(String userId) {
        // 先从缓存获取
        UsersDO user = redisTemplate.opsForValue().get("user:" + userId);
        if (user != null) {
            return user;
        }
        
        // 缓存未命中，从数据库获取
        user = usersDAO.getById(userId);
        if (user != null) {
            // 存入缓存，设置过期时间
            redisTemplate.opsForValue().set("user:" + userId, user, Duration.ofMinutes(30));
        }
        
        return user;
    }
}
```

### 2. 添加JWT支持

可以扩展拦截器支持JWT token认证：

```java
private String getUserIdFromToken(HttpServletRequest request) {
    String token = request.getHeader("Authorization");
    if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
        // 解析JWT token获取用户ID
        return jwtUtil.getUserIdFromToken(token.substring(7));
    }
    return null;
}
```

### 3. 添加权限控制

可以在UserSession中添加用户角色和权限信息：

```java
public class UserSession {
    private List<String> roles;
    private List<String> permissions;
    
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
}
```

## 测试

### 1. 单元测试

```java
@SpringBootTest
class UserAuthInterceptorTest {
    
    @Test
    void testValidUser() {
        // 测试有效用户的认证
    }
    
    @Test
    void testInvalidUser() {
        // 测试无效用户的认证
    }
    
    @Test
    void testMissingUserId() {
        // 测试缺少用户ID的情况
    }
}
```

### 2. 集成测试

```java
@SpringBootTest
@AutoConfigureTestDatabase
class UserInterceptorIntegrationTest {
    
    @Test
    void testApiWithValidUser() {
        // 测试带有效用户ID的API调用
    }
    
    @Test
    void testApiWithoutUserId() {
        // 测试不带用户ID的API调用
    }
}
```

## 总结

用户拦截层提供了一个完整的用户认证和会话管理解决方案，具有以下特点：

1. **简单易用**: 通过请求头传递用户ID，无需复杂的认证流程
2. **线程安全**: 使用ThreadLocal确保多线程环境下的安全性
3. **自动清理**: 请求完成后自动清理资源，防止内存泄漏
4. **灵活配置**: 支持灵活的路径配置和跳过规则
5. **丰富功能**: 提供用户信息获取、权限验证、操作日志等功能
6. **易于扩展**: 支持缓存、JWT、权限控制等扩展

该设计适用于内部系统或已有用户认证体系的场景，可以快速集成到现有项目中。