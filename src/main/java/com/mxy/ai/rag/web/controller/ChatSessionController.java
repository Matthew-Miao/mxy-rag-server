package com.mxy.ai.rag.web.controller;

import com.mxy.ai.rag.dto.*;
import com.mxy.ai.rag.service.ChatSessionService;
import com.mxy.ai.rag.web.param.CreateSessionRequest;
import com.mxy.ai.rag.web.param.SessionQueryRequest;
import com.mxy.ai.rag.web.param.UpdateSessionTitleRequest;
import com.mxy.ai.rag.util.UserContextUtil;
import org.springframework.beans.BeanUtils;
import com.mxy.ai.rag.web.vo.ApiResult;
import com.mxy.ai.rag.web.vo.PageResult;
import com.mxy.ai.rag.web.vo.SessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 聊天会话管理控制器
 * 提供会话的创建、查询、更新、删除等REST API接口
 *
 * @author Mxy
 */
@Tag(name = "聊天会话管理", description = "提供聊天会话的创建、查询、更新、删除等功能")
@RestController
@RequestMapping("/api/v1/chat/sessions")
public class ChatSessionController {

    private static final Logger logger = LoggerFactory.getLogger(ChatSessionController.class);

    @Resource
    private ChatSessionService chatSessionService;

    /**
     * 创建新的聊天会话
     *
     * @param request 创建会话请求参数
     * @return 创建的会话信息
     */
    @Operation(summary = "创建聊天会话", description = "创建一个新的聊天会话")
    @PostMapping("/create")
    public ApiResult<Void> createSession(
            @Parameter(description = "创建会话请求参数", required = true)
            @Valid @RequestBody CreateSessionRequest request) {
        try {
            // 从用户上下文获取当前用户ID
            String currentUserId = UserContextUtil.getCurrentUserId();
            logger.info("接收创建会话请求: userId={}, title={}", currentUserId, request.getTitle());

            // 转换为DTO
            CreateSessionDTO dto = new CreateSessionDTO();
            BeanUtils.copyProperties(request, dto);
            chatSessionService.createSession(dto);
            return ApiResult.success();
        } catch (Exception e) {
            logger.error("创建会话失败: {}", e.getMessage(), e);
            return ApiResult.error("创建会话失败: " + e.getMessage());
        }
    }

    /**
     * 根据会话ID获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话详情
     */
    @Operation(summary = "获取会话详情", description = "根据会话ID获取会话的详细信息")
    @GetMapping("/detail/{sessionId}")
    public ApiResult<SessionVO> getSessionById(
            @Parameter(description = "会话ID", required = true) @PathVariable Long sessionId) {
        try {
            // 从用户上下文获取当前用户ID
            String currentUserId = UserContextUtil.getCurrentUserId();
            logger.info("获取会话详情: sessionId={}, userId={}", sessionId, currentUserId);
            SessionVO sessionVO = chatSessionService.getSessionById(sessionId);
            return ApiResult.success(sessionVO);
        } catch (Exception e) {
            logger.error("获取会话详情失败: {}", e.getMessage(), e);
            return ApiResult.error("获取会话详情失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询用户的会话列表
     *
     * @param request 查询请求参数
     * @return 分页查询结果
     */
    @Operation(summary = "查询会话列表", description = "分页查询用户的会话列表，支持关键词搜索和状态过滤")
    @PostMapping("/list")
    public ApiResult<PageResult<SessionVO>> getSessionList(
            @Parameter(description = "查询请求参数", required = true)
            @Valid @RequestBody SessionQueryRequest request) {
        try {
            // 从用户上下文获取当前用户ID
            String currentUserId = UserContextUtil.getCurrentUserId();
            logger.info("查询会话列表:pageNum={}, pageSize={}", request.getPageNum(), request.getPageSize());

            // 转换为DTO
            SessionQueryDTO dto = new SessionQueryDTO();
            BeanUtils.copyProperties(request, dto);
            PageResult<SessionVO> result = chatSessionService.getSessionList(dto);
            return ApiResult.success(result);
        } catch (Exception e) {
            logger.error("查询会话列表失败: {}", e.getMessage(), e);
            return ApiResult.error("查询会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新会话标题
     *
     * @param request 更新标题请求参数（包含sessionId、userId和title）
     * @return 更新后的会话信息
     */
    @Operation(summary = "更新会话标题", description = "更新指定会话的标题")
    @PostMapping("/update-title")
    public ApiResult<SessionVO> updateSessionTitle(
            @Parameter(description = "更新标题请求参数", required = true)
            @Valid @RequestBody UpdateSessionTitleRequest request) {
        try {
            // 从用户上下文获取当前用户ID
            String currentUserId = UserContextUtil.getCurrentUserId();
            logger.info("更新会话标题: sessionId={}, userId={}, title={}",
                    request.getSessionId(), currentUserId, request.getTitle());

            // 转换为DTO
            UpdateSessionTitleDTO dto = new UpdateSessionTitleDTO();
            BeanUtils.copyProperties(request, dto);

            chatSessionService.updateSessionTitle(dto);
            return ApiResult.success();
        } catch (Exception e) {
            logger.error("更新会话标题失败: {}", e.getMessage(), e);
            return ApiResult.error("更新会话标题失败: " + e.getMessage());
        }
    }
}