package com.mxy.ai.rag.datasource.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mxy.ai.rag.datasource.entity.ChatMessagesDO;
import com.mxy.ai.rag.datasource.mapper.ChatMessagesMapper;
import org.springframework.stereotype.Repository;

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
                .last("LIMIT " + maxMessages)
                .orderByAsc(ChatMessagesDO::getGmtCreate)
                .orderByAsc(ChatMessagesDO::getId)
                .list();
    }
}