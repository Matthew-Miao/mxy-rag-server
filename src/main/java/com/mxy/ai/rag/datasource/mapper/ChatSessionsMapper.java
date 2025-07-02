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

    /**
     * 根据conversationId查询会话
     * @param conversationId Spring AI对话ID
     * @return 会话信息
     */
    ChatSessionsDO selectByConversationId(String conversationId);

    /**
     * 更新会话的最后活动时间
     * @param sessionId 会话ID
     * @param lastActivityTime 最后活动时间
     * @return 更新行数
     */
    int updateLastActivityTime(Long sessionId, java.time.LocalDateTime lastActivityTime);

    /**
     * 查询过期的会话（超过记忆保留时间）
     * @return 过期会话列表
     */
    java.util.List<ChatSessionsDO> selectExpiredSessions();

    /**
     * 批量更新会话状态
     * @param sessionIds 会话ID列表
     * @param status 新状态
     * @return 更新行数
     */
    int batchUpdateStatus(java.util.List<Long> sessionIds, String status);
}