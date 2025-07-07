package com.mxy.ai.rag.service.memory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mxy.ai.rag.datasource.dao.ChatMessagesDAO;
import com.mxy.ai.rag.datasource.dao.ChatSessionsDAO;
import com.mxy.ai.rag.datasource.entity.ChatMessagesDO;
import com.mxy.ai.rag.datasource.entity.ChatSessionsDO;
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
    private ChatSessionsDAO chatSessionsDAO;

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
            List<ChatSessionsDO> sessions = chatSessionsDAO.findConversationIds();
            List<String> conversationIds = sessions.stream()
                    .map(ChatSessionsDO::getConversationId)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());

            logger.debug("找到{}个对话ID", conversationIds.size());
            return conversationIds;

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
     * 按照Spring AI标准实现：先删除现有消息，再保存新消息（事务性操作）
     *
     * @param conversationId 对话ID
     * @param messages       消息列表
     */
    @Override
    @Transactional
    public void saveAll(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");
        
        logger.debug("保存所有消息: conversationId={}, messageCount={}", conversationId, messages.size());

        try {
            // 确保会话存在
            ChatSessionsDO session = ensureSessionExists(conversationId);
            if (session == null) {
                logger.warn("对话会话不存在，创建新会话: conversationId={}", conversationId);
                throw new RuntimeException("对话会话不存在");
            }
            
            // 按照Spring AI标准：先删除现有消息
            deleteMessagesByConversationId(conversationId);
            
            // 保存新消息（带时间戳序列）
            saveMessagesWithTimestampSequence(session.getId(), conversationId, messages);

            // 更新会话信息
            updateSessionActivity(session.getId(), messages.size());

            logger.info("成功保存所有消息: conversationId={}, messageCount={}", conversationId, messages.size());

        } catch (Exception e) {
            logger.error("保存所有消息失败: conversationId={}", conversationId, e);
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

            // 软删除会话
            LambdaUpdateWrapper<ChatSessionsDO> sessionUpdateWrapper = new LambdaUpdateWrapper<>();
            sessionUpdateWrapper.eq(ChatSessionsDO::getConversationId, conversationId)
                    .set(ChatSessionsDO::getDeleted, 1)
                    .set(ChatSessionsDO::getGmtModified, LocalDateTime.now());
            chatSessionsDAO.update(null, sessionUpdateWrapper);

            logger.info("成功删除聊天记忆: conversationId={}", conversationId);

        } catch (Exception e) {
            logger.error("删除聊天记忆失败: conversationId={}", conversationId, e);
            throw e;
        }
    }

    /**
     * 根据对话ID获取会话信息
     */
    private ChatSessionsDO getSessionByConversationId(String conversationId) {
        LambdaQueryWrapper<ChatSessionsDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSessionsDO::getConversationId, conversationId)
                .eq(ChatSessionsDO::getDeleted, 0)
                .last("LIMIT 1");
        return chatSessionsDAO.getOne(queryWrapper);
    }

    /**
     * 根据对话ID获取消息列表
     * 按照Spring AI标准：按时间升序返回消息
     */
    private List<Message> getMessagesByConversationId(String conversationId, Integer maxMessages) {


        List<ChatMessagesDO> messageDOs = chatMessagesDAO.getMessagesByConversationId(conversationId, maxMessages);

        // 转换为Spring AI Message对象
        return messageDOs.stream()
                .map(chatMessagesDO -> {
                    Map<String, Object> metadata = new HashMap<>();;
                    metadata.put("id", chatMessagesDO.getId());
                    return convertToMessage(chatMessagesDO, metadata);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 确保会话存在，如果不存在则创建
     */
    private ChatSessionsDO ensureSessionExists(String conversationId) {
        return getSessionByConversationId(conversationId);
    }


    /**
     * 删除指定对话的所有消息（软删除）
     */
    private void deleteMessagesByConversationId(String conversationId) {
        LambdaUpdateWrapper<ChatMessagesDO> messageUpdateWrapper = new LambdaUpdateWrapper<>();
        messageUpdateWrapper.eq(ChatMessagesDO::getConversationId, conversationId)
                .set(ChatMessagesDO::getDeleted, 1)
                .set(ChatMessagesDO::getGmtModified, LocalDateTime.now());
        chatMessagesDAO.update(null, messageUpdateWrapper);
    }

    /**
     * 保存消息列表（带时间戳序列，确保消息顺序）
     */
    private void saveMessagesWithTimestampSequence(Long sessionId, String conversationId, List<Message> messages) {
        List<ChatMessagesDO> messageDOs = new ArrayList<>();
        long baseTimestamp = System.currentTimeMillis();

        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            ChatMessagesDO messageDO = convertToMessageDO(sessionId, conversationId, message, baseTimestamp + i);
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
     * 更新会话活动信息
     */
    private void updateSessionActivity(Long sessionId, int newMessageCount) {
        LambdaUpdateWrapper<ChatSessionsDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatSessionsDO::getId, sessionId)
                .set(ChatSessionsDO::getGmtModified, LocalDateTime.now());

        chatSessionsDAO.update(null, updateWrapper);
    }

    /**
     * 将ChatMessagesDO转换为Spring AI Message
     */
    private Message convertToMessage(ChatMessagesDO messageDO, Map<String, Object> metadata) {
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
            case "USER" -> UserMessage.builder().text(content).media(new ArrayList<>()).metadata(metadata).build();
            case "ASSISTANT" -> new AssistantMessage(content, metadata);
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

    /**
     * 将Spring AI Message转换为ChatMessagesDO（兼容旧方法）
     */
    private ChatMessagesDO convertToMessageDO(Long sessionId, String conversationId, Message message) {
        return convertToMessageDO(sessionId, conversationId, message, System.currentTimeMillis());
    }
}