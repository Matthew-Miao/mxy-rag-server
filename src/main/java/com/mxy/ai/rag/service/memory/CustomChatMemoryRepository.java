package com.mxy.ai.rag.service.memory;

import com.mxy.ai.rag.datasource.dao.ChatMessagesDAO;
import com.mxy.ai.rag.datasource.entity.ChatMessagesDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自定义聊天记忆仓库实现
 * 实现Spring AI的ChatMemoryRepository接口
 * 支持会话管理、消息存储和上下文记忆功能
 *
 * @author Mxy
 */
@Component
public class CustomChatMemoryRepository implements ChatMemoryRepository {

    private static final Logger logger = LoggerFactory.getLogger(CustomChatMemoryRepository.class);
    private static final int DEFAULT_MAX_MESSAGES = 10;

    @Resource
    private ChatMessagesDAO chatMessagesDAO;

    /**
     * 查找所有对话ID
     *
     * @return 对话ID列表
     */
    @Override
    public List<String> findConversationIds() {
        logger.debug("查找所有对话ID");

        try {
            return chatMessagesDAO.findConversationIds();
        } catch (Exception e) {
            logger.error("查找对话ID失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据对话ID查找消息列表
     *
     * @param conversationId 对话ID
     * @return 消息列表
     */
    @Override
    public List<Message> findByConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        
        logger.debug("根据对话ID查找消息: conversationId={}", conversationId);

        try {
            // 查询消息历史
            List<Message> messages = getMessagesByConversationId(conversationId, DEFAULT_MAX_MESSAGES);

            logger.debug("成功查找消息: conversationId={}, messageCount={}", conversationId, messages.size());
            return messages;

        } catch (Exception e) {
            logger.error("查找消息失败: conversationId={}", conversationId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 保存所有消息到指定对话
     * 修正实现：正确实现MessageWindowChatMemory的语义
     * - 保存当前窗口内的所有消息
     * - 删除窗口外的旧消息（但保留系统消息）
     * - 这样既保持了消息窗口管理，又避免了数据丢失
     *
     * @param conversationId 对话ID
     * @param messages       当前窗口内的消息列表
     */
    @Override
    @Transactional
    public void saveAll(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");
        
        logger.debug("保存消息窗口: conversationId={}, windowSize={}", conversationId, messages.size());

        try {
            // 获取所有现有消息
            List<Message> existingMessages = getMessagesByConversationId(conversationId, Integer.MAX_VALUE);
            
            // 找出需要保存的新消息
            List<Message> newMessages = findNewMessages(existingMessages, messages);
            
            if (!newMessages.isEmpty()) {
                // 保存新消息
                saveMessagesWithTimestampSequence(conversationId, newMessages);
                logger.debug("保存新增消息: conversationId={}, newMessageCount={}", conversationId, newMessages.size());
            }
            
            // 清理窗口外的旧消息（保留系统消息）
            cleanupOldMessages(conversationId, messages);
            
            logger.info("成功更新消息窗口: conversationId={}, windowSize={}", conversationId, messages.size());

        } catch (Exception e) {
            logger.error("保存消息窗口失败: conversationId={}", conversationId, e);
            throw e;
        }
    }

    /**
     * 根据对话ID删除聊天记忆
     *
     * @param conversationId 对话ID
     */
    @Override
    public void deleteByConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        
        logger.info("删除聊天记忆: conversationId={}", conversationId);

        try {
            // 软删除消息
            deleteMessagesByConversationId(conversationId);
            logger.info("成功删除聊天记忆: conversationId={}", conversationId);

        } catch (Exception e) {
            logger.error("删除聊天记忆失败: conversationId={}", conversationId, e);
            throw e;
        }
    }

    /**
     * 根据对话ID获取消息列表
     * 按照Spring AI标准：按时间升序返回消息
     */
    private List<Message> getMessagesByConversationId(String conversationId, Integer maxMessages) {
        List<ChatMessagesDO> messageDOs = chatMessagesDAO.getMessagesByConversationId(conversationId, maxMessages);

        // 转换为Spring AI Message对象
        return messageDOs.stream()
                .map(this::convertToMessage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    /**
     * 删除指定对话的所有消息（软删除）
     */
    private void deleteMessagesByConversationId(String conversationId) {
       chatMessagesDAO.deleteMessagesByConversationId(conversationId);
    }

    /**
     * 找出需要保存的新消息
     * 比较现有消息和当前窗口消息，找出新增的消息
     */
    private List<Message> findNewMessages(List<Message> existingMessages, List<Message> windowMessages) {
        List<Message> newMessages = new ArrayList<>();
        
        // 如果窗口消息数量大于现有消息数量，说明有新消息
        if (windowMessages.size() > existingMessages.size()) {
            // 取出新增的消息（从现有消息数量开始到窗口消息结束）
            newMessages = windowMessages.subList(existingMessages.size(), windowMessages.size());
        }
        
        return newMessages;
    }
    
    /**
     * 清理窗口外的旧消息（保留系统消息）
     * 根据MessageWindowChatMemory的语义，删除不在当前窗口内的旧消息，但保留系统消息
     */
    private void cleanupOldMessages(String conversationId, List<Message> windowMessages) {
        try {
            // 获取所有现有消息
            List<ChatMessagesDO> allMessages = chatMessagesDAO.getMessagesByConversationId(conversationId, Integer.MAX_VALUE);
            
            // 如果现有消息数量小于等于窗口大小，无需清理
            if (allMessages.size() <= windowMessages.size()) {
                return;
            }
            
            // 计算需要删除的消息数量（保留窗口大小的消息）
            int messagesToDelete = allMessages.size() - windowMessages.size();
            
            // 找出最旧的非系统消息进行删除
            List<Long> messageIdsToDelete = new ArrayList<>();
            int deletedCount = 0;
            
            for (ChatMessagesDO messageDO : allMessages) {
                // 跳过系统消息
                if ("SYSTEM".equals(messageDO.getMessageType())) {
                    continue;
                }
                
                messageIdsToDelete.add(messageDO.getId());
                deletedCount++;
                
                // 达到需要删除的数量就停止
                if (deletedCount >= messagesToDelete) {
                    break;
                }
            }
            
            // 执行删除
            if (!messageIdsToDelete.isEmpty()) {
                chatMessagesDAO.deleteMessagesByIds(messageIdsToDelete);
                logger.debug("清理旧消息: conversationId={}, deletedCount={}", conversationId, messageIdsToDelete.size());
            }
            
        } catch (Exception e) {
            logger.warn("清理旧消息失败: conversationId={}", conversationId, e);
            // 清理失败不影响主流程，只记录警告
        }
    }

    /**
     * 保存消息列表（带时间戳序列，确保消息顺序）
     */
    private void saveMessagesWithTimestampSequence(String conversationId, List<Message> messages) {
        List<ChatMessagesDO> messageDOs = new ArrayList<>();
        long baseTimestamp = System.currentTimeMillis();

        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            ChatMessagesDO messageDO = convertToMessageDO(-1L, conversationId, message, baseTimestamp + i);
            if (messageDO != null) {
                messageDOs.add(messageDO);
            }
        }

        if (!messageDOs.isEmpty()) {
            chatMessagesDAO.saveBatch(messageDOs);
            logger.debug("批量保存消息: conversationId={}, count={}", conversationId, messageDOs.size());
        }
    }

    /**
     * 将ChatMessagesDO转换为Spring AI Message
     */
    private Message convertToMessage(ChatMessagesDO messageDO) {
        if (messageDO == null || !StringUtils.hasText(messageDO.getContent())) {
            return null;
        }

        String messageType = messageDO.getMessageType();
        String content = messageDO.getContent();

        // 根据messageType转换为对应的Spring AI Message类型
        // USER: 用户输入消息 -> UserMessage
        // ASSISTANT: AI助手回复消息 -> AssistantMessage
        // SYSTEM: 系统提示消息 -> SystemMessage
        // TOOL: 工具调用消息 -> ToolResponseMessage
        return switch (messageType.toUpperCase()) {
            case "USER" -> new UserMessage(content);
            case "ASSISTANT" -> new AssistantMessage(content);
            case "SYSTEM" -> new SystemMessage(content);
            case "TOOL" -> new ToolResponseMessage(List.of()); // 按照Spring AI标准，内容为空
            default -> {
                logger.warn("未知的消息类型: {}", messageType);
                yield new UserMessage(content); // 默认作为用户消息
            }
        };
    }

    /**
     * 将Spring AI Message转换为ChatMessagesDO（带时间戳序列）
     */
    private ChatMessagesDO convertToMessageDO(Long sessionId, String conversationId, Message message, long timestampSequence) {
        if (message == null) {
            return null;
        }

        ChatMessagesDO messageDO = new ChatMessagesDO();
        messageDO.setSessionId(sessionId);
        messageDO.setConversationId(conversationId);
        messageDO.setContent(message.getText());
        messageDO.setDeleted(0);
        
        // 使用时间戳序列确保消息顺序
        LocalDateTime createTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestampSequence), 
            java.time.ZoneId.systemDefault()
        );
        messageDO.setGmtCreate(createTime);
        messageDO.setGmtModified(createTime);
        messageDO.setCreator("system");
        messageDO.setModifier("system");

        // 根据Spring AI消息类型设置message_type字段
        // USER: 用户输入消息
        // ASSISTANT: AI助手回复消息  
        // SYSTEM: 系统提示消息
        // TOOL: 工具调用消息
        if (message instanceof UserMessage) {
            messageDO.setMessageType("USER");
        } else if (message instanceof AssistantMessage) {
            messageDO.setMessageType("ASSISTANT");
        } else if (message instanceof SystemMessage) {
            messageDO.setMessageType("SYSTEM");
        } else if (message instanceof ToolResponseMessage) {
            messageDO.setMessageType("TOOL");
        } else {
            messageDO.setMessageType("USER"); // 默认为用户消息
        }

        return messageDO;
    }
}