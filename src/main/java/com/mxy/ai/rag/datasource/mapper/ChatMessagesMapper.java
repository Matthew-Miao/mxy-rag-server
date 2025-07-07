package com.mxy.ai.rag.datasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mxy.ai.rag.datasource.entity.ChatMessagesDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息表Mapper接口
 * 提供聊天消息数据的基础CRUD操作
 */
@Mapper
public interface ChatMessagesMapper extends BaseMapper<ChatMessagesDO> {

    /**
     * 根据conversationId查询消息列表
     * @param conversationId Spring AI对话ID
     * @return 消息列表
     */
    java.util.List<ChatMessagesDO> selectByConversationId(String conversationId);

    /**
     * 根据conversationId和时间范围查询消息
     * @param conversationId 对话ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息列表
     */
    List<ChatMessagesDO> selectByConversationIdAndTimeRange(
            String conversationId,
            java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime);
}