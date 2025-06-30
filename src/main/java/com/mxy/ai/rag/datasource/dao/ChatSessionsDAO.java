package com.mxy.ai.rag.datasource.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mxy.ai.rag.datasource.entity.ChatSessionsDO;
import com.mxy.ai.rag.datasource.mapper.ChatSessionsMapper;
import org.springframework.stereotype.Repository;

/**
 * 聊天会话表DAO类
 * 提供聊天会话数据的业务层操作
 */
@Repository
public class ChatSessionsDAO extends ServiceImpl<ChatSessionsMapper, ChatSessionsDO> {
}