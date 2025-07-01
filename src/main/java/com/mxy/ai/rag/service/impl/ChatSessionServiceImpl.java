package com.mxy.ai.rag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mxy.ai.rag.datasource.dao.ChatSessionsDAO;
import com.mxy.ai.rag.datasource.entity.ChatSessionsDO;
import com.mxy.ai.rag.service.ChatSessionService;
import com.mxy.ai.rag.dto.*;
import com.mxy.ai.rag.web.vo.PageResult;
import com.mxy.ai.rag.web.vo.SessionVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天会话管理服务实现类
 * @author Mxy
 */
@Service
@Slf4j
public class ChatSessionServiceImpl implements ChatSessionService {

    private static final Logger logger = LoggerFactory.getLogger(ChatSessionServiceImpl.class);

    @Resource
    private ChatSessionsDAO chatSessionsDAO;

    @Override
    public SessionVO createSession(CreateSessionDTO dto) {
        Assert.hasText(dto.getUserId(), "用户ID不能为空");
        
        logger.info("创建新会话: userId={}, title={}", dto.getUserId(), dto.getTitle());
        
        ChatSessionsDO sessionDO = new ChatSessionsDO();
        sessionDO.setUserId(dto.getUserId());
        sessionDO.setTitle(StringUtils.hasText(dto.getTitle()) ? dto.getTitle() : "新对话");
        sessionDO.setDescription(dto.getDescription());
        sessionDO.setMessageCount(0);
        sessionDO.setTotalTokens(0);
        sessionDO.setStatus("active");
        sessionDO.setDeleted(0);
        sessionDO.setGmtCreate(LocalDateTime.now());
        sessionDO.setGmtModified(LocalDateTime.now());
        sessionDO.setCreator(dto.getUserId());
        sessionDO.setModifier(dto.getUserId());
        
        boolean saved = chatSessionsDAO.save(sessionDO);
        if (!saved) {
            throw new RuntimeException("创建会话失败");
        }
        
        logger.info("会话创建成功: sessionId={}", sessionDO.getId());
        return convertToVO(sessionDO);
    }

    @Override
    public SessionVO getSessionById(Long sessionId, String userId) {
        Assert.notNull(sessionId, "会话ID不能为空");
        Assert.hasText(userId, "用户ID不能为空");
        
        logger.info("获取会话详情: sessionId={}, userId={}", sessionId, userId);
        
        ChatSessionsDO sessionDO = chatSessionsDAO.getById(sessionId);
        if (sessionDO == null) {
            throw new RuntimeException("会话不存在");
        }
        
        if (!userId.equals(sessionDO.getUserId())) {
            throw new RuntimeException("无权限访问该会话");
        }
        
        return convertToVO(sessionDO);
    }

    @Override
    public PageResult<SessionVO> getSessionList(SessionQueryDTO dto) {
        Assert.hasText(dto.getUserId(), "用户ID不能为空");
        
        logger.info("查询会话列表: userId={}, keyword={}, status={}, pageNum={}, pageSize={}", 
                dto.getUserId(), dto.getKeyword(), dto.getStatus(), 
                dto.getPageNum(), dto.getPageSize());
        
        LambdaQueryWrapper<ChatSessionsDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSessionsDO::getUserId, dto.getUserId())
                .eq(ChatSessionsDO::getDeleted, false);
        
        // 状态过滤
        if (StringUtils.hasText(dto.getStatus())) {
            queryWrapper.eq(ChatSessionsDO::getStatus, dto.getStatus());
        }
        
        // 关键词搜索
        if (StringUtils.hasText(dto.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(ChatSessionsDO::getTitle, dto.getKeyword())
                    .or()
                    .like(ChatSessionsDO::getDescription, dto.getKeyword()));
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(ChatSessionsDO::getGmtCreate);
        
        Page<ChatSessionsDO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        IPage<ChatSessionsDO> result = chatSessionsDAO.page(page, queryWrapper);
        
        List<SessionVO> sessionVOs = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        logger.info("查询会话列表完成: 总数={}, 当前页数据量={}", result.getTotal(), sessionVOs.size());
        
        return new PageResult<>(sessionVOs, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public SessionVO updateSessionTitle(UpdateSessionTitleDTO dto) {
        Assert.notNull(dto.getSessionId(), "会话ID不能为空");
        Assert.hasText(dto.getUserId(), "用户ID不能为空");
        Assert.hasText(dto.getTitle(), "标题不能为空");
        
        logger.info("更新会话标题: sessionId={}, userId={}, title={}", dto.getSessionId(), dto.getUserId(), dto.getTitle());
        
        ChatSessionsDO sessionDO = getAndValidateSession(dto.getSessionId(), dto.getUserId());
        sessionDO.setTitle(dto.getTitle());
        sessionDO.setGmtModified(LocalDateTime.now());
        sessionDO.setModifier(dto.getUserId());
        
        boolean updated = chatSessionsDAO.updateById(sessionDO);
        if (!updated) {
            throw new RuntimeException("更新会话标题失败");
        }
        
        logger.info("会话标题更新成功: sessionId={}", dto.getSessionId());
        return convertToVO(sessionDO);
    }

    @Override
    public SessionVO updateSessionDescription(UpdateSessionDescriptionDTO dto) {
        Assert.notNull(dto.getSessionId(), "会话ID不能为空");
        Assert.hasText(dto.getUserId(), "用户ID不能为空");
        
        logger.info("更新会话描述: sessionId={}, userId={}", dto.getSessionId(), dto.getUserId());
        
        ChatSessionsDO sessionDO = getAndValidateSession(dto.getSessionId(), dto.getUserId());
        sessionDO.setDescription(dto.getDescription());
        sessionDO.setGmtModified(LocalDateTime.now());
        sessionDO.setModifier(dto.getUserId());
        
        boolean updated = chatSessionsDAO.updateById(sessionDO);
        if (!updated) {
            throw new RuntimeException("更新会话描述失败");
        }
        
        logger.info("会话描述更新成功: sessionId={}", dto.getSessionId());
        return convertToVO(sessionDO);
    }

    @Override
    public Boolean archiveSession(ArchiveSessionDTO dto) {
        Assert.notNull(dto.getSessionId(), "会话ID不能为空");
        Assert.hasText(dto.getUserId(), "用户ID不能为空");
        
        logger.info("归档会话: sessionId={}, userId={}", dto.getSessionId(), dto.getUserId());
        
        ChatSessionsDO sessionDO = getAndValidateSession(dto.getSessionId(), dto.getUserId());
        sessionDO.setStatus("archived");
        sessionDO.setGmtModified(LocalDateTime.now());
        sessionDO.setModifier(dto.getUserId());
        
        boolean updated = chatSessionsDAO.updateById(sessionDO);
        logger.info("会话归档{}: sessionId={}", updated ? "成功" : "失败", dto.getSessionId());
        
        return updated;
    }

    @Override
    public Boolean deleteSession(DeleteSessionDTO dto) {
        Assert.notNull(dto.getSessionId(), "会话ID不能为空");
        Assert.hasText(dto.getUserId(), "用户ID不能为空");
        
        logger.info("删除会话: sessionId={}, userId={}", dto.getSessionId(), dto.getUserId());
        
        ChatSessionsDO sessionDO = getAndValidateSession(dto.getSessionId(), dto.getUserId());
        sessionDO.setDeleted(0);
        sessionDO.setGmtModified(LocalDateTime.now());
        sessionDO.setModifier(dto.getUserId());
        
        boolean updated = chatSessionsDAO.updateById(sessionDO);
        logger.info("会话删除{}: sessionId={}", updated ? "成功" : "失败", dto.getSessionId());
        
        return updated;
    }

    @Override
    public Boolean restoreSession(RestoreSessionDTO dto) {
        Assert.notNull(dto.getSessionId(), "会话ID不能为空");
        Assert.hasText(dto.getUserId(), "用户ID不能为空");
        
        logger.info("恢复会话: sessionId={}, userId={}", dto.getSessionId(), dto.getUserId());
        
        ChatSessionsDO sessionDO = chatSessionsDAO.getById(dto.getSessionId());
        if (sessionDO == null) {
            throw new RuntimeException("会话不存在");
        }
        
        if (!dto.getUserId().equals(sessionDO.getUserId())) {
            throw new RuntimeException("无权限操作该会话");
        }
        
        sessionDO.setDeleted(0);
        sessionDO.setStatus("active");
        sessionDO.setGmtModified(LocalDateTime.now());
        sessionDO.setModifier(dto.getUserId());
        
        boolean updated = chatSessionsDAO.updateById(sessionDO);
        logger.info("会话恢复{}: sessionId={}", updated ? "成功" : "失败", dto.getSessionId());
        
        return updated;
    }

    @Override
    public Object getSessionStatistics(String userId) {
        Assert.hasText(userId, "用户ID不能为空");
        
        logger.info("获取会话统计信息: userId={}", userId);
        
        LambdaQueryWrapper<ChatSessionsDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSessionsDO::getUserId, userId);
        
        // 总数
        Long totalCount = chatSessionsDAO.count(queryWrapper);
        
        // 活跃数
        queryWrapper.eq(ChatSessionsDO::getStatus, "active")
                .eq(ChatSessionsDO::getDeleted, false);
        Long activeCount = chatSessionsDAO.count(queryWrapper);
        
        // 归档数
        queryWrapper.clear();
        queryWrapper.eq(ChatSessionsDO::getUserId, userId)
                .eq(ChatSessionsDO::getStatus, "archived")
                .eq(ChatSessionsDO::getDeleted, false);
        Long archivedCount = chatSessionsDAO.count(queryWrapper);
        
        // 已删除数
        queryWrapper.clear();
        queryWrapper.eq(ChatSessionsDO::getUserId, userId)
                .eq(ChatSessionsDO::getDeleted, true);
        Long deletedCount = chatSessionsDAO.count(queryWrapper);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", totalCount);
        statistics.put("activeCount", activeCount);
        statistics.put("archivedCount", archivedCount);
        statistics.put("deletedCount", deletedCount);
        
        logger.info("会话统计信息: {}", statistics);
        return statistics;
    }

    /**
     * 获取并验证会话权限
     */
    private ChatSessionsDO getAndValidateSession(Long sessionId, String userId) {
        ChatSessionsDO sessionDO = chatSessionsDAO.getById(sessionId);
        if (sessionDO == null) {
            throw new RuntimeException("会话不存在");
        }
        
        if (!userId.equals(sessionDO.getUserId())) {
            throw new RuntimeException("无权限操作该会话");
        }
        
        return sessionDO;
    }

    /**
     * 将DO对象转换为VO对象
     */
    private SessionVO convertToVO(ChatSessionsDO sessionDO) {
        SessionVO sessionVO = new SessionVO();
        BeanUtils.copyProperties(sessionDO, sessionVO);
        return sessionVO;
    }
}