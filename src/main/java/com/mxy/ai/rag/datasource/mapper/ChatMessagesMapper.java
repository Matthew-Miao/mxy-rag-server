package com.mxy.ai.rag.datasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mxy.ai.rag.datasource.entity.ChatMessagesDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 聊天消息表Mapper接口
 * 提供聊天消息数据的基础CRUD操作
 */
@Mapper
public interface ChatMessagesMapper extends BaseMapper<ChatMessagesDO> {


    List<String> findConversationIds();
}