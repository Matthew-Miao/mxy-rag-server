package com.mxy.ai.rag.datasource.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mxy.ai.rag.datasource.entity.ChatMessagesDO;
import com.mxy.ai.rag.datasource.entity.ChatSessionsDO;
import com.mxy.ai.rag.datasource.mapper.ChatMessagesMapper;
import com.mxy.ai.rag.dto.ChatFeedbackDTO;
import com.mxy.ai.rag.dto.ChatMessagePageRequestDTO;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息表DAO类
 * 提供聊天消息数据的业务层操作
 */
@Repository
public class ChatMessagesDAO extends ServiceImpl<ChatMessagesMapper, ChatMessagesDO> {

    /**
     * 根据会话ID获取聊天消息列表
     *
     * @param conversationId 会话ID
     * @param maxMessages    最大消息数
     * @return 聊天消息列表
     */
    public List<ChatMessagesDO> getMessagesByConversationId(String conversationId, Integer maxMessages) {
        return lambdaQuery().eq(ChatMessagesDO::getConversationId, conversationId)
                .eq(ChatMessagesDO::getDeleted, 0)
                .orderByAsc(ChatMessagesDO::getId)
                .last("LIMIT " + maxMessages)
                .list();
    }

    /**
     * 获取会话ID列表
     *
     * @return 会话ID列表
     */
    public List<String> findConversationIds() {
        return  this.baseMapper.findConversationIds();
    }

    public void deleteMessagesByConversationId(String conversationId) {
        lambdaUpdate().eq(ChatMessagesDO::getConversationId, conversationId)
                .set(ChatMessagesDO::getDeleted, 1)
                .set(ChatMessagesDO::getGmtModified, LocalDateTime.now())
                .update();
    }

    /**
     * 根据消息ID列表批量删除消息（软删除）
     *
     * @param messageIds 消息ID列表
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

    /**
     * 更新消息
     *
     * @param sessionId      会话ID
     * @param conversationId 对话id
     * @param currentUserId  当前用户ID
     */
    public void updateMessage(Long sessionId, String conversationId, String currentUserId) {
        lambdaUpdate().eq(ChatMessagesDO::getConversationId, conversationId)
                .eq(ChatMessagesDO::getDeleted, 0)
                .set(ChatMessagesDO::getSessionId, sessionId)
                .set(ChatMessagesDO::getGmtModified, LocalDateTime.now())
                .set(ChatMessagesDO::getModifier, currentUserId)
                .update();
    }

    /**
     * 获取聊天历史
     *
     * @param chatMessagePageRequestDTO 聊天消息分页请求参数
     * @return 聊天历史列表
     */
    public Page<ChatMessagesDO> getChatHistory(ChatMessagePageRequestDTO chatMessagePageRequestDTO) {
        return lambdaQuery().eq(ChatMessagesDO::getSessionId, chatMessagePageRequestDTO.getSessionId())
                .eq(ChatMessagesDO::getDeleted, 0)
                .orderByDesc(ChatMessagesDO::getId)
                .page(new Page<>(chatMessagePageRequestDTO.getPageNum(), chatMessagePageRequestDTO.getPageSize()));
    }

    /**
     * 提交反馈
     *
     * @param dto 反馈参数
     */
    public void submitFeedback(ChatFeedbackDTO dto) {
        lambdaUpdate().eq(ChatMessagesDO::getId, dto.getMessageId())
                .set(ChatMessagesDO::getRating, dto.getRating())
                .update();
    }

    /**
     * 根据会话ID获取最近的消息列表
     *
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @return 最近的消息列表
     */
    public List<ChatMessagesDO> getRecentMessagesBySessionId(Long sessionId, int limit) {
        return lambdaQuery().eq(ChatMessagesDO::getSessionId, sessionId)
                .eq(ChatMessagesDO::getDeleted, 0)
                .orderByDesc(ChatMessagesDO::getId)
                .last("LIMIT " + limit)
                .list();
    }
}