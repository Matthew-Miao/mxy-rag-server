package com.mxy.ai.rag.service;

import com.mxy.ai.rag.dto.*;
import com.mxy.ai.rag.web.vo.PageResult;
import com.mxy.ai.rag.web.vo.SessionVO;

/**
 * 聊天会话管理服务接口
 * 提供会话的创建、查询、更新、删除等功能
 */
public interface ChatSessionService {

    /**
     * 创建新的聊天会话
     * @param dto 创建会话数据传输对象
     * @return 创建的会话信息
     */
    SessionVO createSession(CreateSessionDTO dto);

    /**
     * 根据会话ID获取会话详情
     * @param sessionId 会话ID
     * @param userId 用户ID（用于权限验证）
     * @return 会话详情
     */
    SessionVO getSessionById(Long sessionId, String userId);

    /**
     * 分页查询用户的会话列表
     * @param dto 查询数据传输对象
     * @return 分页查询结果
     */
    PageResult<SessionVO> getSessionList(SessionQueryDTO dto);

    /**
     * 更新会话标题
     * @param dto 更新会话标题数据传输对象
     * @return 更新后的会话信息
     */
    SessionVO updateSessionTitle(UpdateSessionTitleDTO dto);

    /**
     * 更新会话描述
     * @param dto 更新会话描述数据传输对象
     * @return 更新后的会话信息
     */
    SessionVO updateSessionDescription(UpdateSessionDescriptionDTO dto);

    /**
     * 归档会话（将状态设置为archived）
     * @param dto 归档会话数据传输对象
     * @return 是否操作成功
     */
    Boolean archiveSession(ArchiveSessionDTO dto);

    /**
     * 删除会话（逻辑删除）
     * @param dto 删除会话数据传输对象
     * @return 是否操作成功
     */
    Boolean deleteSession(DeleteSessionDTO dto);

    /**
     * 恢复已删除的会话
     * @param dto 恢复会话数据传输对象
     * @return 是否操作成功
     */
    Boolean restoreSession(RestoreSessionDTO dto);

    /**
     * 获取用户的会话统计信息
     * @param userId 用户ID
     * @return 统计信息（总数、活跃数、归档数等）
     */
    Object getSessionStatistics(String userId);

    /**
     * 根据conversationId获取会话详情
     * @param conversationId Spring AI对话ID
     * @return 会话详情
     */
    SessionVO getSessionByConversationId(String conversationId);

    /**
     * 更新会话记忆配置
     * @param sessionId 会话ID
     * @param maxContextMessages 最大上下文消息数
     * @param contextStrategy 上下文策略
     * @param memoryRetentionHours 记忆保留时间（小时）
     * @return 是否更新成功
     */
    Boolean updateSessionMemoryConfig(Long sessionId, Integer maxContextMessages, 
                                    String contextStrategy, Integer memoryRetentionHours);
}