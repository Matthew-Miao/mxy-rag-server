package com.mxy.ai.rag.datasource.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mxy.ai.rag.datasource.entity.ChatSessionsDO;
import com.mxy.ai.rag.datasource.mapper.ChatSessionsMapper;
import com.mxy.ai.rag.dto.SessionQueryDTO;
import org.springframework.stereotype.Repository;

/**
 * 聊天会话表DAO类
 * 提供聊天会话数据的业务层操作
 */
@Repository
public class ChatSessionsDAO extends ServiceImpl<ChatSessionsMapper, ChatSessionsDO> {
    public Page<ChatSessionsDO> getSessionList(SessionQueryDTO dto) {
        Page<ChatSessionsDO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        return page(page);
    }
}