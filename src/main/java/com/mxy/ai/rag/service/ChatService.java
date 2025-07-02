package com.mxy.ai.rag.service;

import com.mxy.ai.rag.dto.ChatAskDTO;
import com.mxy.ai.rag.dto.ChatStreamDTO;
import com.mxy.ai.rag.dto.ChatFeedbackDTO;
import com.mxy.ai.rag.web.vo.ChatMessageVO;
import com.mxy.ai.rag.web.vo.PageResult;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 智能对话服务接口
 * 提供智能问答、流式对话、对话历史查询等功能
 * 
 * @author Mxy
 */
public interface ChatService {

    /**
     * 智能问答（阻塞式）
     * 基于知识库进行问答，返回完整的回答结果
     * 
     * @param dto 问答请求数据传输对象
     * @return 问答结果
     */
    ChatMessageVO askQuestion(ChatAskDTO dto);

    /**
     * 流式智能问答
     * 基于知识库进行问答，以流的形式返回回答内容
     * 
     * @param dto 流式问答请求数据传输对象
     * @return 流式回答内容
     */
    Flux<String> askQuestionStream(ChatStreamDTO dto);

    /**
     * 获取对话历史
     * 分页查询指定会话的对话历史记录
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID（用于权限验证）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页的对话历史
     */
    PageResult<ChatMessageVO> getChatHistory(Long sessionId, String userId, Long pageNum, Long pageSize);

    /**
     * 用户反馈
     * 用户对AI回答进行评分和反馈
     * 
     * @param dto 反馈数据传输对象
     * @return 反馈处理结果
     */
    Boolean submitFeedback(ChatFeedbackDTO dto);

    /**
     * 获取会话的所有消息
     * 获取指定会话的完整对话记录（不分页）
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID（用于权限验证）
     * @return 消息列表
     */
    List<ChatMessageVO> getSessionMessages(Long sessionId, String userId);

}