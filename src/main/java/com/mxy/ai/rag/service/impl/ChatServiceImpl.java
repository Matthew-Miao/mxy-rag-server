package com.mxy.ai.rag.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxy.ai.rag.datasource.dao.ChatMessagesDAO;
import com.mxy.ai.rag.datasource.dao.ChatSessionsDAO;
import com.mxy.ai.rag.datasource.entity.ChatMessagesDO;
import com.mxy.ai.rag.datasource.entity.ChatSessionsDO;
import com.mxy.ai.rag.dto.ChatAskDTO;
import com.mxy.ai.rag.dto.ChatFeedbackDTO;
import com.mxy.ai.rag.dto.ChatMessagePageRequestDTO;
import com.mxy.ai.rag.service.ChatService;
import com.mxy.ai.rag.service.KnowledgeBaseService;
import com.mxy.ai.rag.util.UserContextUtil;
import com.mxy.ai.rag.web.vo.ChatMessageVO;
import com.mxy.ai.rag.web.vo.PageResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

/**
 * 智能对话服务实现类
 * 提供智能问答、流式对话、对话历史查询等功能的具体实现
 *
 * @author Mxy
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    @Resource
    private ChatMessagesDAO chatMessagesDAO;

    @Resource
    private ChatSessionsDAO chatSessionsDAO;

    @Resource
    private KnowledgeBaseService knowledgeBaseService;


    /**
     * 智能问答（阻塞式）
     * 基于知识库进行问答，返回完整的回答结果
     *
     * @param dto 问答请求数据传输对象
     * @return 问答结果
     */
    @Override
    @Transactional
    public String askQuestion(ChatAskDTO dto) {
        Assert.notNull(dto.getSessionId(), "会话ID不能为空");
        Assert.hasText(dto.getQuestion(), "问题内容不能为空");
        String currentUserId = UserContextUtil.getCurrentUserId();
        logger.info("开始处理智能问答: sessionId={}, userId={}, question={}",
                dto.getSessionId(), currentUserId, dto.getQuestion());

        // 验证会话是否存在且属于当前用户

        long startTime = System.currentTimeMillis();
        try {
            String conversationId = dto.getSessionId().toString();
            // 调用知识库服务获取回答
            String answer = knowledgeBaseService.chatWithKnowledge(dto.getQuestion(), conversationId, dto.getTopK());
            long responseTime = System.currentTimeMillis() - startTime;
            logger.info("知识库对话完成，查询: '{}'，耗时: {}ms", dto.getQuestion(), responseTime);

            chatMessagesDAO.updateMessage(dto.getSessionId(), conversationId, currentUserId);

            // 转换为VO返回
            return answer;

        } catch (Exception e) {
            logger.error("智能问答处理失败: {}", e.getMessage(), e);
            throw new RuntimeException("智能问答处理失败: " + e.getMessage());
        }
    }

    /**
     * 智能问答（流式）
     * 基于知识库进行问答，以流形式返回回答内容
     *
     * @param dto 流式问答请求数据传输对象
     * @return 流式问答结果
     */
    @Override
    public Flux<String> askQuestionStream(ChatAskDTO dto) {
        Assert.notNull(dto.getSessionId(), "会话ID不能为空");
        Assert.hasText(dto.getQuestion(), "问题内容不能为空");
        String currentUserId = UserContextUtil.getCurrentUserId();
        logger.info("开始处理流式智能问答: sessionId={}, userId={}, question={}",
                dto.getSessionId(), currentUserId, dto.getQuestion());

        // 验证会话是否存在且属于当前用户
        long startTime = System.currentTimeMillis();
        try {
            String conversationId = dto.getSessionId().toString();

            // 调用知识库服务获取流式回答
            Flux<String> answerStream = knowledgeBaseService.chatWithKnowledgeStream(dto.getQuestion(), conversationId, dto.getTopK());

            // 在流完成时记录日志和更新消息
            return answerStream
                    .doOnComplete(() -> {
                        long responseTime = System.currentTimeMillis() - startTime;
                        logger.info("流式知识库对话完成，查询: '{}', 耗时: {}ms", dto.getQuestion(), responseTime);

                        // 异步更新消息记录
                        try {
                            chatMessagesDAO.updateMessage(dto.getSessionId(), conversationId, currentUserId);
                        } catch (Exception e) {
                            logger.error("更新消息记录失败: {}", e.getMessage(), e);
                        }
                    })
                    .doOnError(error -> {
                        logger.error("流式智能问答处理失败: {}", error.getMessage(), error);
                    });

        } catch (Exception e) {
            logger.error("流式智能问答处理失败: {}", e.getMessage(), e);
            return Flux.just("智能问答处理失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<ChatMessageVO> getChatHistory(ChatMessagePageRequestDTO chatMessagePageRequestDTO) {
        Page<ChatMessagesDO> page = chatMessagesDAO.getChatHistory(chatMessagePageRequestDTO);
        List<ChatMessagesDO> records = page.getRecords();
        List<ChatMessageVO> chatMessageVOS = records.stream().map(record -> {
            ChatMessageVO chatMessageVO = new ChatMessageVO();
            BeanUtils.copyProperties(record, chatMessageVO);
            return chatMessageVO;
        }).toList();

       return new PageResult<>(chatMessageVOS, page.getTotal(), page.getCurrent(), page.getSize());

    }

    @Override
    public void submitFeedback(ChatFeedbackDTO dto) {
        ChatMessagesDO chatMessagesDO = chatMessagesDAO.getById(dto.getMessageId());
        if (chatMessagesDO == null) {
            throw new RuntimeException("消息ID不存在");
        }
        chatMessagesDAO.submitFeedback(dto);
    }

    /**
     * 根据聊天记录自动生成会话标题
     *
     * @param sessionId 会话ID
     * @return 生成的标题
     */
    @Override
    public String generateSessionTitle(Long sessionId) {
        try {
            ChatSessionsDO chatSessionsDO = chatSessionsDAO.getById(sessionId);
            if (chatSessionsDO == null) {
                throw new RuntimeException("会话ID不存在");
            }
            // 如果标题不是"新对话"，则直接返回 说明 已经设置过标题
            if (!"新对话".equals(chatSessionsDO.getTitle())) {
                return chatSessionsDO.getTitle();
            }

            // 获取会话的最近几条消息
            List<ChatMessagesDO> recentMessages = chatMessagesDAO.getRecentMessagesBySessionId(sessionId, 2);
            
            if (recentMessages.isEmpty()) {
                return "新对话";
            }
            
            // 构建对话内容用于生成标题
            StringBuilder conversationContent = new StringBuilder();
            for (ChatMessagesDO message : recentMessages) {
                if ("USER".equals(message.getMessageType())) {
                    conversationContent.append("用户: ").append(message.getContent()).append("\n");
                } else if ("ASSISTANT".equals(message.getMessageType())) {
                    conversationContent.append("助手: ").append(message.getContent()).append("\n");
                }
            }
            
            // 调用AI生成标题
            String prompt = "请根据以下对话内容，生成一个简洁、准确的会话标题（不超过20个字符，不要包含引号）：\n\n" + conversationContent;
            
            String generatedTitle = knowledgeBaseService.generateSessionTitle(conversationContent);
            
            // 清理生成的标题，移除可能的引号和多余空格
            generatedTitle = generatedTitle.trim().replaceAll("[\"']", "");
            
            // 限制标题长度
            if (generatedTitle.length() > 30) {
                generatedTitle = generatedTitle.substring(0, 30);
            }
            
            logger.info("为会话 {} 生成标题: {}", sessionId, generatedTitle);
            return generatedTitle;
            
        } catch (Exception e) {
            logger.error("生成会话标题失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return "新对话";
        }
    }
}