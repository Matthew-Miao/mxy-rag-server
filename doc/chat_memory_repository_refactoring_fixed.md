# CustomChatMemoryRepository 重构文档

## 概述

本文档记录了对 `CustomChatMemoryRepository` 的重构过程，主要解决了原有实现中的"先删除后插入"问题，使其更好地支持 Spring AI 的 `MessageWindowChatMemory` 功能。

## 问题分析

### 原有问题

1. **"先删除后插入"策略的问题**：
   - 原有的 `saveAll` 方法采用了"先删除后插入"的策略
   - 这种做法会导致每次保存都删除所有现有消息，然后重新插入
   - 与 `MessageWindowChatMemory` 的设计理念不符
   - 可能导致消息历史丢失和性能问题

2. **Spring AI JdbcChatMemoryRepository 的问题**：
   - Spring AI 官方的 `JdbcChatMemoryRepository` 也存在同样的问题
   - 每次更新都会替换整个内存块
   - 操作不是事务性的
   - 无法正确持久化 `tool_calls` 字段

## 解决方案

### 新的 saveAll 实现策略

1. **增量保存**：
   ```java
   // 获取现有消息数量
   long existingCount = chatMessagesDAO.lambdaQuery()
           .eq(ChatMessagesDO::getConversationId, conversationId)
           .eq(ChatMessagesDO::getDeleted, 0)
           .count();
   
   // 只保存新增的消息
   List<Message> newMessages = findNewMessages(messages, (int) existingCount);
   if (!newMessages.isEmpty()) {
       List<ChatMessagesDO> messageDOs = saveMessagesWithTimestampSequence(sessionId, newMessages);
   }
   ```

2. **消息窗口管理**：
   ```java
   // 清理超出窗口大小的旧消息（保留系统消息）
   cleanupOldMessages(conversationId, maxMessages);
   ```

### 核心改进

1. **findNewMessages 方法**：
   - 识别需要保存的新消息
   - 避免重复保存已存在的消息

2. **cleanupOldMessages 方法**：
   - 清理超出窗口大小的旧消息
   - 保留系统消息不被删除
   - 使用软删除机制

3. **deleteMessagesByIds 方法**：
   - 在 `ChatMessagesDAO` 中新增批量删除方法
   - 支持根据消息ID列表进行软删除

## 技术实现

### 新增的辅助方法

```java
/**
 * 找出需要保存的新消息
 */
private List<Message> findNewMessages(List<Message> allMessages, int existingCount) {
    if (existingCount >= allMessages.size()) {
        return Collections.emptyList();
    }
    return allMessages.subList(existingCount, allMessages.size());
}

/**
 * 清理超出窗口大小的旧消息，但保留系统消息
 */
private void cleanupOldMessages(String conversationId, int maxMessages) {
    // 获取所有消息，按时间戳排序
    List<ChatMessagesDO> allMessages = chatMessagesDAO.lambdaQuery()
            .eq(ChatMessagesDO::getConversationId, conversationId)
            .eq(ChatMessagesDO::getDeleted, 0)
            .orderByAsc(ChatMessagesDO::getTimestampSequence)
            .list();
    
    if (allMessages.size() <= maxMessages) {
        return;
    }
    
    // 分离系统消息和其他消息
    List<ChatMessagesDO> systemMessages = allMessages.stream()
            .filter(msg -> "SYSTEM".equals(msg.getMessageType()))
            .collect(Collectors.toList());
    
    List<ChatMessagesDO> nonSystemMessages = allMessages.stream()
            .filter(msg -> !"SYSTEM".equals(msg.getMessageType()))
            .collect(Collectors.toList());
    
    // 计算需要保留的非系统消息数量
    int maxNonSystemMessages = maxMessages - systemMessages.size();
    
    if (nonSystemMessages.size() > maxNonSystemMessages) {
        // 删除最旧的非系统消息
        int messagesToDelete = nonSystemMessages.size() - maxNonSystemMessages;
        List<Long> idsToDelete = nonSystemMessages.stream()
                .limit(messagesToDelete)
                .map(ChatMessagesDO::getId)
                .collect(Collectors.toList());
        
        if (!idsToDelete.isEmpty()) {
            chatMessagesDAO.deleteMessagesByIds(idsToDelete);
        }
    }
}
```

### DAO 层改进

在 `ChatMessagesDAO` 中新增了 `deleteMessagesByIds` 方法：

```java
/**
 * 根据消息ID列表批量删除消息（软删除）
 */
public void deleteMessagesByIds(List<Long> messageIds) {
    if (messageIds == null || messageIds.isEmpty()) {
        return;
    }
    
    lambdaUpdate().in(ChatMessagesDO::getId, messageIds)
            .set(ChatMessagesDO::getDeleted, 1)
            .set(ChatMessagesDO::getGmtModified, LocalDateTime.now())
            .update();
}
```

## 测试验证

### 更新的测试用例

1. **testSaveAllWithValidInput**：
   - 验证首次保存消息的正确性
   - 确保不会删除现有消息

2. **testIncrementalSaveWithExistingMessages**：
   - 验证增量保存功能
   - 确保只保存新消息

3. **testMessageTypeMapping**：
   - 验证消息类型映射的正确性

## 配置说明

在 `ChatMemoryConfig` 中配置了默认的最大消息数量：

```java
private static final int DEFAULT_MAX_MESSAGES = 10;

@Bean
@Primary
public MessageWindowChatMemory chatMemory() {
   return MessageWindowChatMemory.builder()
            .chatMemoryRepository(customChatMemoryRepository)
            .maxMessages(DEFAULT_MAX_MESSAGES).build();
}
```

## 优势总结

1. **性能提升**：
   - 避免了不必要的删除和重新插入操作
   - 只处理新增的消息

2. **数据完整性**：
   - 保持了消息的连续性
   - 系统消息得到特殊保护

3. **Spring AI 兼容性**：
   - 完全符合 `MessageWindowChatMemory` 的设计理念
   - 正确实现了消息窗口管理

4. **事务安全**：
   - 所有数据库操作都在事务中执行
   - 确保数据一致性

## 结论

通过这次重构，`CustomChatMemoryRepository` 现在能够：
- 正确支持 Spring AI 的 `MessageWindowChatMemory` 功能
- 提供更好的性能和数据完整性
- 避免了原有"先删除后插入"策略的问题
- 为聊天应用提供了可靠的消息持久化解决方案