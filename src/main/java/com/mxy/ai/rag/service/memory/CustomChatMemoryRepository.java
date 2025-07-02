package com.mxy.ai.rag.service.memory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mxy.ai.rag.datasource.dao.ChatMessagesDAO;
import com.mxy.ai.rag.datasource.dao.ChatSessionsDAO;
import com.mxy.ai.rag.datasource.entity.ChatMessagesDO;
import com.mxy.ai.rag.datasource.entity.ChatSessionsDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.sql.Timestamp;
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
            LambdaQueryWrapper<ChatSessionsDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ChatSessionsDO::getDeleted, 0)
                    .select(ChatSessionsDO::getConversationId)
                    .orderByDesc(ChatSessionsDO::getLastActivityTime);
            
            List<ChatSessionsDO> sessions = chatSessionsDAO.list(queryWrapper);
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
        logger.debug("根据对话ID查找消息: conversationId={}", conversationId);
        
        if (!StringUtils.hasText(conversationId)) {
            logger.warn("对话ID为空，返回空消息列表");
            return Collections.emptyList();
        }

        try {
            // 查询会话信息
            ChatSessionsDO session = getSessionByConversationId(conversationId);
            if (session == null) {
                logger.info("未找到对话会话: conversationId={}", conversationId);
                return Collections.emptyList();
            }

            // 查询消息历史
            List<Message> messages = getMessagesByConversationId(conversationId, session.getMaxContextMessages());
            
            logger.debug("成功查找消息: conversationId={}, messageCount={}", conversationId, messages.size());
            return messages;
            
        } catch (Exception e) {
            logger.error("查找消息失败: conversationId={}", conversationId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 保存所有消息到指定对话
     * 
     * @param conversationId 对话ID
     * @param messages 消息列表
     */
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        logger.debug("保存所有消息: conversationId={}, messageCount={}", conversationId, messages.size());
        
        if (!StringUtils.hasText(conversationId) || messages.isEmpty()) {
            logger.warn("对话ID或消息列表为空，跳过保存");
            return;
        }

        try {
            // 确保会话存在
            ChatSessionsDO session = ensureSessionExists(conversationId);
            
            // 先删除该对话的所有现有消息（软删除）
            LambdaUpdateWrapper<ChatMessagesDO> deleteWrapper = new LambdaUpdateWrapper<>();
            deleteWrapper.eq(ChatMessagesDO::getConversationId, conversationId)
                    .set(ChatMessagesDO::getDeleted, 1)
                    .set(ChatMessagesDO::getGmtModified, LocalDateTime.now());
            chatMessagesDAO.update(null, deleteWrapper);
            
            // 保存新消息
            saveMessages(session.getId(), conversationId, messages);
            
            // 更新会话信息
            updateSessionActivity(session.getId(), messages.size());
            
            logger.info("成功保存所有消息: conversationId={}, messageCount={}", conversationId, messages.size());
            
        } catch (Exception e) {
            logger.error("保存所有消息失败: conversationId={}", conversationId, e);
        }
    }

    /**
     * 根据对话ID删除聊天记忆
     * 
     * @param conversationId 对话ID
     */
    @Override
    public void deleteByConversationId(String conversationId) {
        logger.info("删除聊天记忆: conversationId={}", conversationId);
        
        if (!StringUtils.hasText(conversationId)) {
            logger.warn("对话ID为空，跳过删除");
            return;
        }

        try {
            // 软删除消息
            LambdaUpdateWrapper<ChatMessagesDO> messageUpdateWrapper = new LambdaUpdateWrapper<>();
            messageUpdateWrapper.eq(ChatMessagesDO::getConversationId, conversationId)
                    .set(ChatMessagesDO::getDeleted, 1)
                    .set(ChatMessagesDO::getGmtModified, LocalDateTime.now());
            chatMessagesDAO.update(null, messageUpdateWrapper);
            
            // 软删除会话
            LambdaUpdateWrapper<ChatSessionsDO> sessionUpdateWrapper = new LambdaUpdateWrapper<>();
            sessionUpdateWrapper.eq(ChatSessionsDO::getConversationId, conversationId)
                    .set(ChatSessionsDO::getDeleted, 1)
                    .set(ChatSessionsDO::getGmtModified, LocalDateTime.now());
            chatSessionsDAO.update(null, sessionUpdateWrapper);
            
            logger.info("成功删除聊天记忆: conversationId={}", conversationId);
            
        } catch (Exception e) {
            logger.error("删除聊天记忆失败: conversationId={}", conversationId, e);
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
     */
    private List<Message> getMessagesByConversationId(String conversationId, Integer maxMessages) {
        LambdaQueryWrapper<ChatMessagesDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessagesDO::getConversationId, conversationId)
                .eq(ChatMessagesDO::getDeleted, 0)
                .orderByDesc(ChatMessagesDO::getTimestamp)
                .orderByDesc(ChatMessagesDO::getId);
        
        if (maxMessages != null && maxMessages > 0) {
            queryWrapper.last("LIMIT " + maxMessages);
        }
        
        List<ChatMessagesDO> messageDOs = chatMessagesDAO.list(queryWrapper);
        
        // 转换为Spring AI Message对象并按时间正序排列
        return messageDOs.stream()
                .sorted(Comparator.comparing(ChatMessagesDO::getTimestamp))
                .map(this::convertToMessage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 确保会话存在，如果不存在则创建
     */
    private ChatSessionsDO ensureSessionExists(String conversationId) {
        ChatSessionsDO session = getSessionByConversationId(conversationId);
        
        if (session == null) {
            // 创建新会话
            session = new ChatSessionsDO();
            session.setUserId("system"); // 默认用户ID，实际使用时应该从上下文获取
            session.setConversationId(conversationId);
            session.setTitle("AI对话会话");
            session.setDescription("Spring AI自动创建的对话会话");
            session.setMessageCount(0);
            session.setTotalTokens(0);
            session.setStatus("active");
            session.setMaxContextMessages(20);
            session.setContextStrategy("sliding_window");
            session.setMemoryRetentionHours(168); // 7天
            session.setLastActivityTime(LocalDateTime.now());
            session.setDeleted(0);
            session.setGmtCreate(LocalDateTime.now());
            session.setGmtModified(LocalDateTime.now());
            session.setCreator("system");
            session.setModifier("system");
            
            chatSessionsDAO.save(session);
            logger.info("创建新会话: conversationId={}, sessionId={}", conversationId, session.getId());
        }
        
        return session;
    }



    /**
     * 保存消息列表
     */
    private void saveMessages(Long sessionId, String conversationId, List<Message> messages) {
        List<ChatMessagesDO> messageDOs = new ArrayList<>();
        
        for (Message message : messages) {
            ChatMessagesDO messageDO = convertToMessageDO(sessionId, conversationId, message);
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
                .setSql("message_count = message_count + " + newMessageCount)
                .set(ChatSessionsDO::getLastActivityTime, LocalDateTime.now())
                .set(ChatSessionsDO::getGmtModified, LocalDateTime.now());
        
        chatSessionsDAO.update(null, updateWrapper);
    }

    /**
     * 将ChatMessagesDO转换为Spring AI Message
     */
    private Message convertToMessage(ChatMessagesDO messageDO) {
        if (messageDO == null || !StringUtils.hasText(messageDO.getContent())) {
            return null;
        }
        
        String role = messageDO.getRole();
        String content = messageDO.getContent();

        return switch (role.toLowerCase()) {
            case "user" -> new UserMessage(content);
            case "assistant" -> new AssistantMessage(content);
            case "system" -> new SystemMessage(content);
            default -> {
                logger.warn("未知的消息角色: {}", role);
                yield new UserMessage(content); // 默认作为用户消息
            }
        };
    }

    /**
     * 将Spring AI Message转换为ChatMessagesDO
     */
    private ChatMessagesDO convertToMessageDO(Long sessionId, String conversationId, Message message) {
        if (message == null) {
            return null;
        }
        
        ChatMessagesDO messageDO = new ChatMessagesDO();
        messageDO.setSessionId(sessionId);
        messageDO.setConversationId(conversationId);
        messageDO.setContent(message.getText());
        messageDO.setContextWeight(BigDecimal.ONE);
        messageDO.setTimestamp(new Timestamp(System.currentTimeMillis()));
        messageDO.setDeleted(0);
        messageDO.setGmtCreate(LocalDateTime.now());
        messageDO.setGmtModified(LocalDateTime.now());
        messageDO.setCreator("system");
        messageDO.setModifier("system");
        
        // 根据消息类型设置角色和消息类型
        if (message instanceof UserMessage) {
            messageDO.setRole("user");
            messageDO.setMessageType("USER");
        } else if (message instanceof AssistantMessage) {
            messageDO.setRole("assistant");
            messageDO.setMessageType("ASSISTANT");
        } else if (message instanceof SystemMessage) {
            messageDO.setRole("system");
            messageDO.setMessageType("SYSTEM");
        } else {
            messageDO.setRole("user");
            messageDO.setMessageType("USER");
        }
        
        return messageDO;
    }
}