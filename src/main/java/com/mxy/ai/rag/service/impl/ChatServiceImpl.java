package com.mxy.ai.rag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxy.ai.rag.datasource.dao.ChatMessagesDAO;
import com.mxy.ai.rag.datasource.dao.ChatSessionsDAO;
import com.mxy.ai.rag.datasource.entity.ChatMessagesDO;
import com.mxy.ai.rag.datasource.entity.ChatSessionsDO;
import com.mxy.ai.rag.dto.ChatAskDTO;
import com.mxy.ai.rag.dto.ChatFeedbackDTO;
import com.mxy.ai.rag.dto.ChatStreamDTO;
import com.mxy.ai.rag.service.ChatService;
import com.mxy.ai.rag.service.KnowledgeBaseService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 智能问答（阻塞式）
     * 基于知识库进行问答，返回完整的回答结果
     * 
     * @param dto 问答请求数据传输对象
     * @return 问答结果
     */
    @Override
    @Transactional
    public ChatMessageVO askQuestion(ChatAskDTO dto) {
        Assert.notNull(dto.getSessionId(), "会话ID不能为空");
        Assert.hasText(dto.getUserId(), "用户ID不能为空");
        Assert.hasText(dto.getQuestion(), "问题内容不能为空");
        
        logger.info("开始处理智能问答: sessionId={}, userId={}, question={}", 
                dto.getSessionId(), dto.getUserId(), dto.getQuestion());
        
        // 验证会话是否存在且属于当前用户
        validateSession(dto.getSessionId(), dto.getUserId());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 保存用户问题
            saveUserMessage(dto);
            
            // 调用知识库服务获取回答
            String answer;
            if (dto.getUseKnowledgeBase()) {
                answer = knowledgeBaseService.chatWithKnowledge(dto.getQuestion(), dto.getTopK());
            } else {
                // 如果不使用知识库，可以直接调用LLM（这里简化处理）
                answer = "抱歉，当前仅支持基于知识库的问答。";
            }
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            // 保存AI回答
            ChatMessagesDO assistantMessage = saveAssistantMessage(dto, answer, responseTime);
            
            // 更新会话信息
            updateSessionInfo(dto.getSessionId(), answer);
            
            // 转换为VO返回
            return convertToVO(assistantMessage);
            
        } catch (Exception e) {
            logger.error("智能问答处理失败: {}", e.getMessage(), e);
            throw new RuntimeException("智能问答处理失败: " + e.getMessage());
        }
    }

    /**
     * 流式智能问答
     * 基于知识库进行问答，以流的形式返回回答内容
     * 
     * @param dto 流式问答请求数据传输对象
     * @return 流式回答内容
     */
    @Override
    public Flux<String> askQuestionStream(ChatStreamDTO dto) {
        Assert.notNull(dto.getSessionId(), "会话ID不能为空");
        Assert.hasText(dto.getUserId(), "用户ID不能为空");
        Assert.hasText(dto.getQuestion(), "问题内容不能为空");
        
        logger.info("开始处理流式智能问答: sessionId={}, userId={}, question={}", 
                dto.getSessionId(), dto.getUserId(), dto.getQuestion());
        
        // 验证会话是否存在且属于当前用户
        validateSession(dto.getSessionId(), dto.getUserId());
        
        try {
            // 保存用户问题
            saveUserMessage(dto);
            
            // 调用知识库服务获取流式回答
            if (dto.getUseKnowledgeBase()) {
                return knowledgeBaseService.chatWithKnowledgeStream(dto.getQuestion(), dto.getTopK())
                        .doOnComplete(() -> {
                            // 流式回答完成后的处理（如保存完整回答到数据库）
                            logger.info("流式问答完成: sessionId={}", dto.getSessionId());
                        })
                        .doOnError(error -> {
                            logger.error("流式问答出错: {}", error.getMessage(), error);
                        });
            } else {
                return Flux.just("抱歉，当前仅支持基于知识库的问答。");
            }
            
        } catch (Exception e) {
            logger.error("流式智能问答处理失败: {}", e.getMessage(), e);
            return Flux.error(new RuntimeException("流式智能问答处理失败: " + e.getMessage()));
        }
    }

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
    @Override
    public PageResult<ChatMessageVO> getChatHistory(Long sessionId, String userId, Long pageNum, Long pageSize) {
        Assert.notNull(sessionId, "会话ID不能为空");
        Assert.hasText(userId, "用户ID不能为空");
        
        logger.info("获取对话历史: sessionId={}, userId={}, pageNum={}, pageSize={}", 
                sessionId, userId, pageNum, pageSize);
        
        // 验证会话是否存在且属于当前用户
        validateSession(sessionId, userId);
        
        // 构建分页查询
        Page<ChatMessagesDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ChatMessagesDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessagesDO::getSessionId, sessionId)
                .eq(ChatMessagesDO::getDeleted, 0)
                .orderByAsc(ChatMessagesDO::getGmtCreate);
        
        IPage<ChatMessagesDO> result = chatMessagesDAO.page(page, queryWrapper);
        
        // 转换为VO
        List<ChatMessageVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(voList, result.getTotal(), pageNum, pageSize);
    }

    /**
     * 用户反馈
     * 用户对AI回答进行评分和反馈
     * 
     * @param dto 反馈数据传输对象
     * @return 反馈处理结果
     */
    @Override
    @Transactional
    public Boolean submitFeedback(ChatFeedbackDTO dto) {
        Assert.notNull(dto.getMessageId(), "消息ID不能为空");
        Assert.hasText(dto.getUserId(), "用户ID不能为空");
        Assert.notNull(dto.getRating(), "评分不能为空");
        
        logger.info("提交用户反馈: messageId={}, userId={}, rating={}", 
                dto.getMessageId(), dto.getUserId(), dto.getRating());
        
        // 查询消息是否存在
        ChatMessagesDO message = chatMessagesDAO.getById(dto.getMessageId());
        if (message == null || message.getDeleted() == 1) {
            throw new RuntimeException("消息不存在或已删除");
        }
        
        // 验证消息所属会话的用户权限
        validateSession(message.getSessionId(), dto.getUserId());
        
        // 更新消息评分
        message.setRating(dto.getRating());
        message.setGmtModified(LocalDateTime.now());
        message.setModifier(dto.getUserId());
        
        // 如果有反馈内容，可以存储到metadata中
        if (dto.getFeedback() != null && !dto.getFeedback().trim().isEmpty()) {
            try {
                String metadata = message.getMetadata();
                if (metadata == null || metadata.trim().isEmpty()) {
                    metadata = "{\"feedback\":\"" + dto.getFeedback() + "\"}";
                } else {
                    // 简化处理，直接替换或添加feedback字段
                    metadata = metadata.replace("}", ",\"feedback\":\"" + dto.getFeedback() + "\"}");
                }
                message.setMetadata(metadata);
            } catch (Exception e) {
                logger.warn("保存反馈内容到metadata失败: {}", e.getMessage());
            }
        }
        
        return chatMessagesDAO.updateById(message);
    }

    /**
     * 获取会话的所有消息
     * 获取指定会话的完整对话记录（不分页）
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID（用于权限验证）
     * @return 消息列表
     */
    @Override
    public List<ChatMessageVO> getSessionMessages(Long sessionId, String userId) {
        Assert.notNull(sessionId, "会话ID不能为空");
        Assert.hasText(userId, "用户ID不能为空");
        
        logger.info("获取会话所有消息: sessionId={}, userId={}", sessionId, userId);
        
        // 验证会话是否存在且属于当前用户
        validateSession(sessionId, userId);
        
        LambdaQueryWrapper<ChatMessagesDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessagesDO::getSessionId, sessionId)
                .eq(ChatMessagesDO::getDeleted, 0)
                .orderByAsc(ChatMessagesDO::getGmtCreate);
        
        List<ChatMessagesDO> messages = chatMessagesDAO.list(queryWrapper);
        
        return messages.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 验证会话是否存在且属于当前用户
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    private void validateSession(Long sessionId, String userId) {
        ChatSessionsDO session = chatSessionsDAO.getById(sessionId);
        if (session == null || session.getDeleted() == 1) {
            throw new RuntimeException("会话不存在或已删除");
        }
        if (!userId.equals(session.getUserId())) {
            throw new RuntimeException("无权限访问该会话");
        }
    }

    /**
     * 保存用户消息
     *
     * @param dto 请求DTO
     */
    private void saveUserMessage(ChatAskDTO dto) {
        ChatMessagesDO userMessage = new ChatMessagesDO();
        userMessage.setSessionId(dto.getSessionId());
        userMessage.setRole("user");
        userMessage.setContent(dto.getQuestion());
        userMessage.setDeleted(0);
        userMessage.setGmtCreate(LocalDateTime.now());
        userMessage.setGmtModified(LocalDateTime.now());
        userMessage.setCreator(dto.getUserId());
        userMessage.setModifier(dto.getUserId());
        
        chatMessagesDAO.save(userMessage);
    }

    /**
     * 保存用户消息（流式版本）
     *
     * @param dto 请求DTO
     */
    private void saveUserMessage(ChatStreamDTO dto) {
        ChatMessagesDO userMessage = new ChatMessagesDO();
        userMessage.setSessionId(dto.getSessionId());
        userMessage.setRole("user");
        userMessage.setContent(dto.getQuestion());
        userMessage.setDeleted(0);
        userMessage.setGmtCreate(LocalDateTime.now());
        userMessage.setGmtModified(LocalDateTime.now());
        userMessage.setCreator(dto.getUserId());
        userMessage.setModifier(dto.getUserId());
        
        chatMessagesDAO.save(userMessage);
    }

    /**
     * 保存AI回答消息
     * 
     * @param dto 请求DTO
     * @param answer AI回答内容
     * @param responseTime 响应时间
     * @return 保存的消息实体
     */
    private ChatMessagesDO saveAssistantMessage(ChatAskDTO dto, String answer, long responseTime) {
        ChatMessagesDO assistantMessage = new ChatMessagesDO();
        assistantMessage.setSessionId(dto.getSessionId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(answer);
        assistantMessage.setResponseTime((int) responseTime);
        assistantMessage.setDeleted(0);
        assistantMessage.setGmtCreate(LocalDateTime.now());
        assistantMessage.setGmtModified(LocalDateTime.now());
        assistantMessage.setCreator(dto.getUserId());
        assistantMessage.setModifier(dto.getUserId());
        
        // 估算Token使用量（简化计算）
        int tokensUsed = (dto.getQuestion().length() + answer.length()) / 4;
        assistantMessage.setTokensUsed(tokensUsed);
        
        chatMessagesDAO.save(assistantMessage);
        return assistantMessage;
    }

    /**
     * 更新会话信息
     * 
     * @param sessionId 会话ID
     * @param lastMessage 最后一条消息内容
     */
    private void updateSessionInfo(Long sessionId, String lastMessage) {
        ChatSessionsDO session = chatSessionsDAO.getById(sessionId);
        if (session != null) {
            // 更新消息数量
            LambdaQueryWrapper<ChatMessagesDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ChatMessagesDO::getSessionId, sessionId)
                    .eq(ChatMessagesDO::getDeleted, 0);
            long messageCount = chatMessagesDAO.count(queryWrapper);
            
            session.setMessageCount((int) messageCount);
            session.setGmtModified(LocalDateTime.now());
            
            chatSessionsDAO.updateById(session);
        }
    }

    /**
     * 将DO转换为VO
     * 
     * @param messageDO 消息实体
     * @return 消息VO
     */
    private ChatMessageVO convertToVO(ChatMessagesDO messageDO) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(messageDO, vo);
        
        // 解析sources字段
        if (messageDO.getSources() != null && !messageDO.getSources().trim().isEmpty()) {
            try {
                List<ChatMessageVO.KnowledgeSource> sources = objectMapper.readValue(
                        messageDO.getSources(),
                        new TypeReference<>() {
                        }
                );
                vo.setSources(sources);
            } catch (JsonProcessingException e) {
                logger.warn("解析sources字段失败: {}", e.getMessage());
                vo.setSources(new ArrayList<>());
            }
        } else {
            vo.setSources(new ArrayList<>());
        }
        
        return vo;
    }
}