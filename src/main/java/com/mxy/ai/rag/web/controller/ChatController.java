package com.mxy.ai.rag.web.controller;

import com.mxy.ai.rag.dto.ChatAskDTO;
import com.mxy.ai.rag.dto.ChatFeedbackDTO;
import com.mxy.ai.rag.dto.ChatStreamDTO;
import com.mxy.ai.rag.service.ChatService;
import com.mxy.ai.rag.web.param.ChatAskRequest;
import com.mxy.ai.rag.web.param.ChatFeedbackRequest;
import com.mxy.ai.rag.web.param.ChatStreamRequest;
import com.mxy.ai.rag.web.vo.ApiResult;
import com.mxy.ai.rag.web.vo.ChatMessageVO;
import com.mxy.ai.rag.web.vo.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.validation.Valid;
import java.util.List;

/**
 * 智能对话控制器
 * 提供智能问答、流式对话、对话历史查询等REST API接口
 * 
 * @author Mxy
 */
@Tag(name = "智能对话管理", description = "提供智能问答、流式对话、对话历史查询等功能")
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Resource
    private ChatService chatService;


    /**
     * 智能问答（阻塞式）
     * 基于知识库进行问答，返回完整的回答结果
     * 
     * @param request 问答请求参数
     * @return 问答结果
     */
    @Operation(summary = "智能问答", description = "基于知识库进行智能问答，返回完整的回答结果")
    @PostMapping("/ask")
    public ApiResult<ChatMessageVO> askQuestion(
            @Parameter(description = "问答请求参数", required = true)
            @Valid @RequestBody ChatAskRequest request) {
        try {
            logger.info("接收智能问答请求: sessionId={}, userId={}, question={}", 
                    request.getSessionId(), request.getUserId(), request.getQuestion());
            
            // 转换为DTO
            ChatAskDTO dto = new ChatAskDTO();
            BeanUtils.copyProperties(request, dto);
            
            ChatMessageVO result = chatService.askQuestion(dto);
            return ApiResult.success("问答成功", result);
        } catch (Exception e) {
            logger.error("智能问答失败: {}", e.getMessage(), e);
            return ApiResult.error("智能问答失败: " + e.getMessage());
        }
    }

    /**
     * 流式智能问答
     * 基于知识库进行问答，以流的形式返回回答内容
     * 
     * @param request 流式问答请求参数
     * @return 流式回答内容
     */
    @Operation(summary = "流式智能问答", description = "基于知识库进行智能问答，以流的形式返回回答内容")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> askQuestionStream(
            @Parameter(description = "流式问答请求参数", required = true)
            @Valid @RequestBody ChatStreamRequest request) {
        try {
            logger.info("接收流式智能问答请求: sessionId={}, userId={}, question={}", 
                    request.getSessionId(), request.getUserId(), request.getQuestion());
            
            // 转换为DTO
            ChatStreamDTO dto = new ChatStreamDTO();
            BeanUtils.copyProperties(request, dto);
            
            return chatService.askQuestionStream(dto);
        } catch (Exception e) {
            logger.error("流式智能问答失败: {}", e.getMessage(), e);
            return Flux.error(new RuntimeException("流式智能问答失败: " + e.getMessage()));
        }
    }

    /**
     * 获取对话历史
     * 分页查询指定会话的对话历史记录
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID（用于权限验证）
     * @param pageNum 页码（默认1）
     * @param pageSize 每页大小（默认20）
     * @return 分页的对话历史
     */
    @Operation(summary = "获取对话历史", description = "分页查询指定会话的对话历史记录")
    @GetMapping("/history/{sessionId}")
    public ApiResult<PageResult<ChatMessageVO>> getChatHistory(
            @Parameter(description = "会话ID", required = true) @PathVariable Long sessionId,
            @Parameter(description = "用户ID", required = true) @RequestParam String userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Long pageSize) {
        try {
            logger.info("获取对话历史: sessionId={}, userId={}, pageNum={}, pageSize={}", 
                    sessionId, userId, pageNum, pageSize);
            
            PageResult<ChatMessageVO> result = chatService.getChatHistory(sessionId, userId, pageNum, pageSize);
            return ApiResult.success(result);
        } catch (Exception e) {
            logger.error("获取对话历史失败: {}", e.getMessage(), e);
            return ApiResult.error("获取对话历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取会话的所有消息
     * 获取指定会话的完整对话记录（不分页）
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID（用于权限验证）
     * @return 消息列表
     */
    @Operation(summary = "获取会话所有消息", description = "获取指定会话的完整对话记录（不分页）")
    @GetMapping("/messages/{sessionId}")
    public ApiResult<List<ChatMessageVO>> getSessionMessages(
            @Parameter(description = "会话ID", required = true) @PathVariable Long sessionId,
            @Parameter(description = "用户ID", required = true) @RequestParam String userId) {
        try {
            logger.info("获取会话所有消息: sessionId={}, userId={}", sessionId, userId);
            
            List<ChatMessageVO> result = chatService.getSessionMessages(sessionId, userId);
            return ApiResult.success(result);
        } catch (Exception e) {
            logger.error("获取会话消息失败: {}", e.getMessage(), e);
            return ApiResult.error("获取会话消息失败: " + e.getMessage());
        }
    }

    /**
     * 用户反馈
     * 用户对AI回答进行评分和反馈
     * 
     * @param request 反馈请求参数
     * @return 反馈处理结果
     */
    @Operation(summary = "用户反馈", description = "用户对AI回答进行评分和反馈")
    @PostMapping("/feedback")
    public ApiResult<Boolean> submitFeedback(
            @Parameter(description = "反馈请求参数", required = true)
            @Valid @RequestBody ChatFeedbackRequest request) {
        try {
            logger.info("接收用户反馈: messageId={}, userId={}, rating={}", 
                    request.getMessageId(), request.getUserId(), request.getRating());
            
            // 转换为DTO
            ChatFeedbackDTO dto = new ChatFeedbackDTO();
            BeanUtils.copyProperties(request, dto);
            
            Boolean result = chatService.submitFeedback(dto);
            return result ? ApiResult.success("反馈提交成功", result) : ApiResult.error("反馈提交失败");
        } catch (Exception e) {
            logger.error("用户反馈失败: {}", e.getMessage(), e);
            return ApiResult.error("用户反馈失败: " + e.getMessage());
        }
    }
}