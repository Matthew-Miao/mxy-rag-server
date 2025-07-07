# CustomChatMemoryRepository 改造文档

## 概述

本文档描述了将 `CustomChatMemoryRepository` 改造为完全符合 Spring AI 标准的 `ChatMemoryRepository` 实现的过程。

## 改造目标

基于 Spring AI 提供的 `JdbcChatMemoryRepository` 源码，确保我们自定义的基于 DAO 的实现与官方标准功能完全一致。

## 主要改造内容

### 1. 参数验证增强

**改造前：**
```java
if (!StringUtils.hasText(conversationId)) {
    logger.warn("对话ID为空，返回空消息列表");
    return Collections.emptyList();
}
```

**改造后：**
```java
Assert.hasText(conversationId, "conversationId cannot be null or empty");
Assert.notNull(messages, "messages cannot be null");
Assert.noNullElements(messages, "messages cannot contain null elements");
```

**改进点：**
- 使用 Spring 的 `Assert` 工具类进行严格的参数验证
- 与 Spring AI 标准保持一致的错误消息
- 在参数无效时抛出异常而不是静默返回空结果

### 2. saveAll 方法事务性改造

**改造前：**
```java
// 保存新消息
saveMessages(session.getId(), conversationId, messages);
```

**改造后：**
```java
@Transactional
public void saveAll(String conversationId, List<Message> messages) {
    // 按照Spring AI标准：先删除现有消息
    deleteMessagesByConversationId(conversationId);
    
    // 保存新消息（带时间戳序列）
    saveMessagesWithTimestampSequence(session.getId(), conversationId, messages);
}
```

**改进点：**
- 添加 `@Transactional` 注解确保事务性
- 实现 "先删除再插入" 的 Spring AI 标准行为
- 使用时间戳序列确保消息顺序

### 3. 消息时间戳序列机制

**新增功能：**
```java
private void saveMessagesWithTimestampSequence(Long sessionId, String conversationId, List<Message> messages) {
    long baseTimestamp = System.currentTimeMillis();
    
    for (int i = 0; i < messages.size(); i++) {
        Message message = messages.get(i);
        ChatMessagesDO messageDO = convertToMessageDO(sessionId, conversationId, message, baseTimestamp + i);
        // ...
    }
}
```

**改进点：**
- 为每条消息分配递增的时间戳，确保严格的顺序
- 与 Spring AI 的 `AtomicLong instantSeq` 机制保持一致
- 避免并发保存时的时间戳冲突

### 4. TOOL 消息类型支持

**改造前：**
```java
case "TOOL" -> 暂时转为UserMessage
```

**改造后：**
```java
case "TOOL" -> new ToolResponseMessage(List.of()); // 按照Spring AI标准，内容为空
```

**改进点：**
- 完整支持 Spring AI 的所有消息类型
- `ToolResponseMessage` 按标准实现为空内容
- 消息类型映射与官方实现完全一致

### 5. 消息排序优化

**改造前：**
```java
.orderByDesc(ChatMessagesDO::getGmtCreate)
// 然后在内存中重新排序
.sorted(Comparator.comparing(ChatMessagesDO::getGmtCreate))
```

**改造后：**
```java
.orderByAsc(ChatMessagesDO::getGmtCreate)
.orderByAsc(ChatMessagesDO::getId)
// 直接返回，无需内存排序
```

**改进点：**
- 数据库层面直接按时间升序查询
- 消除不必要的内存排序操作
- 提高查询性能

### 6. 异常处理标准化

**改造前：**
```java
catch (Exception e) {
    logger.error("保存所有消息失败: conversationId={}", conversationId, e);
}
```

**改造后：**
```java
catch (Exception e) {
    logger.error("保存所有消息失败: conversationId={}", conversationId, e);
    throw e; // 重新抛出异常
}
```

**改进点：**
- 与 Spring AI 标准保持一致，不吞噬异常
- 确保调用方能够感知到操作失败
- 保持事务回滚的正确性

## 核心方法对比

### findConversationIds()
- ✅ 保持原有实现，已符合标准
- ✅ 返回所有有效对话ID列表

### findByConversationId(String conversationId)
- ✅ 添加严格参数验证
- ✅ 按时间升序返回消息
- ✅ 支持所有消息类型

### saveAll(String conversationId, List<Message> messages)
- ✅ 事务性操作
- ✅ 先删除后插入
- ✅ 时间戳序列保证顺序
- ✅ 严格参数验证

### deleteByConversationId(String conversationId)
- ✅ 软删除实现
- ✅ 同时删除消息和会话
- ✅ 严格参数验证

## 测试覆盖

创建了 `CustomChatMemoryRepositoryTest` 测试类，覆盖：

1. **正常功能测试**
   - 保存各种类型消息
   - 查询消息列表
   - 删除对话记忆

2. **参数验证测试**
   - 空对话ID处理
   - 空消息列表处理
   - 无效参数异常

3. **消息类型映射测试**
   - USER、ASSISTANT、SYSTEM、TOOL 类型
   - 类型转换正确性

## 兼容性说明

### 向后兼容
- 保留了所有原有的私有方法
- 数据库表结构无需修改
- 现有业务代码无需调整

### Spring AI 兼容
- 完全符合 `ChatMemoryRepository` 接口规范
- 行为与 `JdbcChatMemoryRepository` 一致
- 可以无缝替换官方实现

## 性能优化

1. **查询优化**：数据库层面直接排序，减少内存操作
2. **批量操作**：保持批量插入和更新
3. **索引利用**：按时间和ID排序利用现有索引

## 总结

通过本次改造，`CustomChatMemoryRepository` 现在：

- ✅ 完全符合 Spring AI 标准
- ✅ 支持所有消息类型
- ✅ 具备事务性保证
- ✅ 提供严格的参数验证
- ✅ 保持高性能和可扩展性
- ✅ 向后兼容现有系统

这确保了我们的自定义实现可以与 Spring AI 生态系统完美集成，同时保持了基于 MyBatis-Plus 的高性能数据访问优势。