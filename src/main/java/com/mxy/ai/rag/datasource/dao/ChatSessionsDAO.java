package com.mxy.ai.rag.datasource.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mxy.ai.rag.datasource.entity.ChatSessionsDO;
import com.mxy.ai.rag.datasource.mapper.ChatSessionsMapper;
import com.mxy.ai.rag.dto.SessionQueryDTO;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 聊天会话表DAO类
 * 提供聊天会话数据的业务层操作
 */
@Repository
public class ChatSessionsDAO extends ServiceImpl<ChatSessionsMapper, ChatSessionsDO> {
    public Page<ChatSessionsDO> getSessionList(SessionQueryDTO dto) {
        Page<ChatSessionsDO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        
        QueryWrapper<ChatSessionsDO> queryWrapper = new QueryWrapper<>();
        
        // 按用户ID过滤
        if (StringUtils.hasText(dto.getUserId())) {
            queryWrapper.eq("creator", dto.getUserId());
        }
        queryWrapper.eq("deleted", 0);
        
        // 按更新时间倒序排列
        queryWrapper.orderByDesc("gmt_modified");
        
        return page(page, queryWrapper);
    }
}