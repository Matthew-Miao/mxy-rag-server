# 聊天会话管理 API 文档

## 概述

聊天会话管理模块提供了完整的会话生命周期管理功能，包括会话的创建、查询、更新、归档和删除等操作。

## 基础信息

- **基础路径**: `/api/v1/chat/sessions`
- **认证方式**: 通过 `userId` 参数进行用户身份验证
- **响应格式**: 统一使用 `ApiResult<T>` 包装响应数据

## 接口列表

### 1. 创建会话

**接口地址**: `POST /api/v1/chat/sessions`

**请求参数**:
```json
{
  "userId": "user123",
  "title": "我的新对话",
  "description": "关于AI技术的讨论"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "会话创建成功",
  "data": {
    "id": 1,
    "userId": "user123",
    "title": "我的新对话",
    "description": "关于AI技术的讨论",
    "messageCount": 0,
    "totalTokens": 0,
    "status": "active",
    "gmtCreate": "2024-01-01T10:00:00",
    "gmtModified": "2024-01-01T10:00:00"
  },
  "timestamp": 1704067200000
}
```

### 2. 获取会话详情

**接口地址**: `GET /api/v1/chat/sessions/{sessionId}`

**路径参数**:
- `sessionId`: 会话ID

**查询参数**:
- `userId`: 用户ID（必填）

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": "user123",
    "title": "我的新对话",
    "description": "关于AI技术的讨论",
    "messageCount": 5,
    "totalTokens": 1200,
    "status": "active",
    "gmtCreate": "2024-01-01T10:00:00",
    "gmtModified": "2024-01-01T10:30:00",
    "lastMessage": "最后一条消息内容",
    "lastMessageTime": "2024-01-01T10:30:00"
  },
  "timestamp": 1704067200000
}
```

### 3. 查询会话列表

**接口地址**: `GET /api/v1/chat/sessions`

**查询参数**:
- `userId`: 用户ID（必填）
- `keyword`: 搜索关键词（可选）
- `status`: 会话状态过滤（可选：active、archived、deleted）
- `pageNum`: 页码，从1开始（默认：1）
- `pageSize`: 每页大小（默认：20）

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": "user123",
        "title": "我的新对话",
        "description": "关于AI技术的讨论",
        "messageCount": 5,
        "totalTokens": 1200,
        "status": "active",
        "gmtCreate": "2024-01-01T10:00:00",
        "gmtModified": "2024-01-01T10:30:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 20,
    "pages": 1,
    "hasNext": false,
    "hasPrevious": false
  },
  "timestamp": 1704067200000
}
```

### 4. 更新会话标题

**接口地址**: `PUT /api/v1/chat/sessions/{sessionId}/title`

**路径参数**:
- `sessionId`: 会话ID

**查询参数**:
- `userId`: 用户ID（必填）

**请求参数**:
```json
{
  "title": "更新后的标题"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "会话标题更新成功",
  "data": {
    "id": 1,
    "userId": "user123",
    "title": "更新后的标题",
    "description": "关于AI技术的讨论",
    "messageCount": 5,
    "totalTokens": 1200,
    "status": "active",
    "gmtCreate": "2024-01-01T10:00:00",
    "gmtModified": "2024-01-01T11:00:00"
  },
  "timestamp": 1704067200000
}
```

### 5. 更新会话描述

**接口地址**: `PUT /api/v1/chat/sessions/{sessionId}/description`

**路径参数**:
- `sessionId`: 会话ID

**查询参数**:
- `userId`: 用户ID（必填）
- `description`: 新描述（必填）

**响应示例**:
```json
{
  "code": 200,
  "message": "会话描述更新成功",
  "data": {
    "id": 1,
    "userId": "user123",
    "title": "我的新对话",
    "description": "更新后的描述",
    "messageCount": 5,
    "totalTokens": 1200,
    "status": "active",
    "gmtCreate": "2024-01-01T10:00:00",
    "gmtModified": "2024-01-01T11:00:00"
  },
  "timestamp": 1704067200000
}
```

### 6. 归档会话

**接口地址**: `PUT /api/v1/chat/sessions/{sessionId}/archive`

**路径参数**:
- `sessionId`: 会话ID

**查询参数**:
- `userId`: 用户ID（必填）

**响应示例**:
```json
{
  "code": 200,
  "message": "会话归档成功",
  "data": true,
  "timestamp": 1704067200000
}
```

### 7. 删除会话

**接口地址**: `DELETE /api/v1/chat/sessions/{sessionId}`

**路径参数**:
- `sessionId`: 会话ID

**查询参数**:
- `userId`: 用户ID（必填）

**响应示例**:
```json
{
  "code": 200,
  "message": "会话删除成功",
  "data": true,
  "timestamp": 1704067200000
}
```

### 8. 恢复会话

**接口地址**: `PUT /api/v1/chat/sessions/{sessionId}/restore`

**路径参数**:
- `sessionId`: 会话ID

**查询参数**:
- `userId`: 用户ID（必填）

**响应示例**:
```json
{
  "code": 200,
  "message": "会话恢复成功",
  "data": true,
  "timestamp": 1704067200000
}
```

### 9. 获取会话统计信息

**接口地址**: `GET /api/v1/chat/sessions/statistics`

**查询参数**:
- `userId`: 用户ID（必填）

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalCount": 10,
    "activeCount": 7,
    "archivedCount": 2,
    "deletedCount": 1
  },
  "timestamp": 1704067200000
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权访问 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 会话状态说明

| 状态 | 说明 |
|------|------|
| active | 活跃状态，正常使用中 |
| archived | 已归档，不再活跃但保留 |
| deleted | 已删除，逻辑删除状态 |

## 注意事项

1. 所有接口都需要提供有效的 `userId` 进行权限验证
2. 删除操作为逻辑删除，数据不会真正从数据库中移除
3. 归档的会话可以通过状态过滤查询到
4. 已删除的会话可以通过恢复接口重新激活
5. 分页查询默认按创建时间倒序排列
6. 关键词搜索支持标题和描述字段的模糊匹配

## 使用示例

### 创建并管理会话的完整流程

```bash
# 1. 创建会话
curl -X POST "http://localhost:8080/api/v1/chat/sessions" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "title": "AI学习讨论",
    "description": "关于人工智能技术的学习和讨论"
  }'

# 2. 查询会话列表
curl "http://localhost:8080/api/v1/chat/sessions?userId=user123&pageNum=1&pageSize=10"

# 3. 更新会话标题
curl -X PUT "http://localhost:8080/api/v1/chat/sessions/1/title?userId=user123" \
  -H "Content-Type: application/json" \
  -d '{"title": "深度学习讨论"}'

# 4. 归档会话
curl -X PUT "http://localhost:8080/api/v1/chat/sessions/1/archive?userId=user123"

# 5. 获取统计信息
curl "http://localhost:8080/api/v1/chat/sessions/statistics?userId=user123"
```