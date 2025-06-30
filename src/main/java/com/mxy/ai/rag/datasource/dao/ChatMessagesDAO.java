package com.mxy.ai.rag.datasource.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mxy.ai.rag.datasource.entity.ChatMessagesDO;
import com.mxy.ai.rag.datasource.mapper.ChatMessagesMapper;
import org.springframework.stereotype.Repository;

/**
 * 聊天消息表DAO类
 * 提供聊天消息数据的业务层操作
 */
@Repository
public class ChatMessagesDAO extends ServiceImpl<ChatMessagesMapper, ChatMessagesDO> {
}