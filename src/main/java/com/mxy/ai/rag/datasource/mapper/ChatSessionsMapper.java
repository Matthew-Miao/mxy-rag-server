package com.mxy.ai.rag.datasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mxy.ai.rag.datasource.entity.ChatSessionsDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天会话表Mapper接口
 * 提供聊天会话数据的基础CRUD操作
 */
@Mapper
public interface ChatSessionsMapper extends BaseMapper<ChatSessionsDO> {
}