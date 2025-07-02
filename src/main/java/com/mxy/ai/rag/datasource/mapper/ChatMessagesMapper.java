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
     * @param conversationId Spring AI对话ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息列表
     */
    java.util.List<ChatMessagesDO> selectByConversationIdAndTimeRange(
            String conversationId, 
            java.sql.Timestamp startTime, 
            java.sql.Timestamp endTime);

    /**
     * 根据语义哈希查询相似消息
     * @param semanticHash 语义哈希值
     * @param limit 限制数量
     * @return 相似消息列表
     */
    java.util.List<ChatMessagesDO> selectBySimilarSemanticHash(String semanticHash, int limit);

    /**
     * 更新消息的相关性分数
     * @param messageId 消息ID
     * @param relevanceScore 相关性分数
     * @return 更新行数
     */
    int updateRelevanceScore(Long messageId, java.math.BigDecimal relevanceScore);

    /**
     * 批量删除过期消息
     * @param sessionIds 会话ID列表
     * @return 删除行数
     */
    int batchDeleteBySessionIds(java.util.List<Long> sessionIds);

    /**
     * 根据上下文权重排序查询消息
     * @param conversationId Spring AI对话ID
     * @param limit 限制数量
     * @return 按权重排序的消息列表
     */
    java.util.List<ChatMessagesDO> selectByConversationIdOrderByContextWeight(String conversationId, int limit);
}