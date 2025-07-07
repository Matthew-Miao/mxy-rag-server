package com.mxy.ai.rag.service;

import com.mxy.ai.rag.dto.ChatAskDTO;
import com.mxy.ai.rag.dto.ChatFeedbackDTO;
import com.mxy.ai.rag.dto.ChatMessagePageRequestDTO;
import com.mxy.ai.rag.web.vo.ChatMessageVO;
import com.mxy.ai.rag.web.vo.PageResult;
import reactor.core.publisher.Flux;

/**
 * 智能对话服务接口
 * 提供智能问答、流式对话、对话历史查询等功能
 *
 * @author Mxy
 */
public interface ChatService {
    /**
     * 智能问答
     *
     * @param dto 问答请求参数
     * @return 问答结果
     */
    String askQuestion(ChatAskDTO dto);

    /**
     * 流式智能问答
     *
     * @param dto 流式问答请求参数
     * @return 流式问答结果
     */
    Flux<String> askQuestionStream(ChatAskDTO dto);

    /**
     * 获取对话历史
     *
     * @param chatMessagePageRequestDTO 查询请求参数
     * @return 对话历史列表
     */
    PageResult<ChatMessageVO> getChatHistory(ChatMessagePageRequestDTO chatMessagePageRequestDTO);

    /**
     * 用户反馈
     *
     * @param dto 用户反馈请求参数
     */
    void submitFeedback(ChatFeedbackDTO dto);
}
