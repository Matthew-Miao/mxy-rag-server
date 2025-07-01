package com.mxy.ai.rag.web.controller;

import com.mxy.ai.rag.service.ChatSessionService;
import com.mxy.ai.rag.dto.*;
import com.mxy.ai.rag.web.param.CreateSessionRequest;
import com.mxy.ai.rag.web.param.SessionQueryRequest;
import com.mxy.ai.rag.web.param.UpdateSessionTitleRequest;
import com.mxy.ai.rag.web.param.UpdateSessionDescriptionRequest;
import com.mxy.ai.rag.web.param.ArchiveSessionRequest;
import com.mxy.ai.rag.web.param.DeleteSessionRequest;
import com.mxy.ai.rag.web.param.RestoreSessionRequest;
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
    public ApiResult<SessionVO> createSession(
            @Parameter(description = "创建会话请求参数", required = true)
            @Valid @RequestBody CreateSessionRequest request) {
        try {
            logger.info("接收创建会话请求: userId={}, title={}", request.getUserId(), request.getTitle());
            
            // 转换为DTO
            CreateSessionDTO dto = new CreateSessionDTO();
            BeanUtils.copyProperties(request, dto);
            
            SessionVO sessionVO = chatSessionService.createSession(dto);
            return ApiResult.success("会话创建成功", sessionVO);
        } catch (Exception e) {
            logger.error("创建会话失败: {}", e.getMessage(), e);
            return ApiResult.error("创建会话失败: " + e.getMessage());
        }
    }

    /**
     * 根据会话ID获取会话详情
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID（用于权限验证）
     * @return 会话详情
     */
    @Operation(summary = "获取会话详情", description = "根据会话ID获取会话的详细信息")
    @GetMapping("/detail/{sessionId}")
    public ApiResult<SessionVO> getSessionById(
            @Parameter(description = "会话ID", required = true) @PathVariable Long sessionId,
            @Parameter(description = "用户ID", required = true) @RequestParam String userId) {
        try {
            logger.info("获取会话详情: sessionId={}, userId={}", sessionId, userId);
            SessionVO sessionVO = chatSessionService.getSessionById(sessionId, userId);
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
            logger.info("查询会话列表: userId={}, keyword={}, pageNum={}, pageSize={}", 
                    request.getUserId(), request.getKeyword(), request.getPageNum(), request.getPageSize());
            
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
            logger.info("更新会话标题: sessionId={}, userId={}, title={}", 
                    request.getSessionId(), request.getUserId(), request.getTitle());
            
            // 转换为DTO
            UpdateSessionTitleDTO dto = new UpdateSessionTitleDTO();
            BeanUtils.copyProperties(request, dto);
            
            SessionVO sessionVO = chatSessionService.updateSessionTitle(dto);
            return ApiResult.success("会话标题更新成功", sessionVO);
        } catch (Exception e) {
            logger.error("更新会话标题失败: {}", e.getMessage(), e);
            return ApiResult.error("更新会话标题失败: " + e.getMessage());
        }
    }

    /**
     * 更新会话描述
     * 
     * @param request 更新描述请求参数（包含sessionId、userId和description）
     * @return 更新后的会话信息
     */
    @Operation(summary = "更新会话描述", description = "更新指定会话的描述信息")
    @PostMapping("/update-description")
    public ApiResult<SessionVO> updateSessionDescription(
            @Parameter(description = "更新描述请求参数", required = true)
            @Valid @RequestBody UpdateSessionDescriptionRequest request) {
        try {
            logger.info("更新会话描述: sessionId={}, userId={}, description={}", 
                    request.getSessionId(), request.getUserId(), request.getDescription());
            
            // 转换为DTO
            UpdateSessionDescriptionDTO dto = new UpdateSessionDescriptionDTO();
            BeanUtils.copyProperties(request, dto);
            
            SessionVO sessionVO = chatSessionService.updateSessionDescription(dto);
            return ApiResult.success("会话描述更新成功", sessionVO);
        } catch (Exception e) {
            logger.error("更新会话描述失败: {}", e.getMessage(), e);
            return ApiResult.error("更新会话描述失败: " + e.getMessage());
        }
    }

    /**
     * 归档会话
     * 
     * @param request 归档会话请求参数（包含sessionId和userId）
     * @return 操作结果
     */
    @Operation(summary = "归档会话", description = "将指定会话设置为归档状态")
    @PostMapping("/archive")
    public ApiResult<Boolean> archiveSession(
            @Parameter(description = "归档请求参数", required = true)
            @Valid @RequestBody ArchiveSessionRequest request) {
        try {
            logger.info("归档会话: sessionId={}, userId={}", request.getSessionId(), request.getUserId());
            
            // 转换为DTO
            ArchiveSessionDTO dto = new ArchiveSessionDTO();
            BeanUtils.copyProperties(request, dto);
            
            Boolean result = chatSessionService.archiveSession(dto);
            return result ? ApiResult.success("会话归档成功", result) : ApiResult.error("会话归档失败");
        } catch (Exception e) {
            logger.error("归档会话失败: {}", e.getMessage(), e);
            return ApiResult.error("归档会话失败: " + e.getMessage());
        }
    }

    /**
     * 删除会话（逻辑删除）
     * 
     * @param request 删除会话请求参数（包含sessionId和userId）
     * @return 操作结果
     */
    @Operation(summary = "删除会话", description = "逻辑删除指定的会话")
    @PostMapping("/delete")
    public ApiResult<Boolean> deleteSession(
            @Parameter(description = "删除请求参数", required = true)
            @Valid @RequestBody DeleteSessionRequest request) {
        try {
            logger.info("删除会话: sessionId={}, userId={}", request.getSessionId(), request.getUserId());
            
            // 转换为DTO
            DeleteSessionDTO dto = new DeleteSessionDTO();
            BeanUtils.copyProperties(request, dto);
            
            Boolean result = chatSessionService.deleteSession(dto);
            return result ? ApiResult.success("会话删除成功", result) : ApiResult.error("会话删除失败");
        } catch (Exception e) {
            logger.error("删除会话失败: {}", e.getMessage(), e);
            return ApiResult.error("删除会话失败: " + e.getMessage());
        }
    }

    /**
     * 恢复已删除的会话
     * 
     * @param request 恢复会话请求参数（包含sessionId和userId）
     * @return 操作结果
     */
    @Operation(summary = "恢复会话", description = "恢复已删除的会话")
    @PostMapping("/restore")
    public ApiResult<Boolean> restoreSession(
            @Parameter(description = "恢复请求参数", required = true)
            @Valid @RequestBody RestoreSessionRequest request) {
        try {
            logger.info("恢复会话: sessionId={}, userId={}", request.getSessionId(), request.getUserId());
            
            // 转换为DTO
            RestoreSessionDTO dto = new RestoreSessionDTO();
            BeanUtils.copyProperties(request, dto);
            
            Boolean result = chatSessionService.restoreSession(dto);
            return result ? ApiResult.success("会话恢复成功", result) : ApiResult.error("会话恢复失败");
        } catch (Exception e) {
            logger.error("恢复会话失败: {}", e.getMessage(), e);
            return ApiResult.error("恢复会话失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的会话统计信息
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    @Operation(summary = "获取会话统计", description = "获取用户的会话统计信息，包括总数、活跃数、归档数等")
    @GetMapping("/statistics/user")
    public ApiResult<Object> getSessionStatistics(
            @Parameter(description = "用户ID", required = true) @RequestParam String userId) {
        try {
            logger.info("获取会话统计信息: userId={}", userId);
            Object statistics = chatSessionService.getSessionStatistics(userId);
            return ApiResult.success(statistics);
        } catch (Exception e) {
            logger.error("获取会话统计信息失败: {}", e.getMessage(), e);
            return ApiResult.error("获取会话统计信息失败: " + e.getMessage());
        }
    }
}