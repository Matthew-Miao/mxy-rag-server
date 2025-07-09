package com.mxy.ai.rag.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mxy.ai.rag.datasource.dao.ChatSessionsDAO;
import com.mxy.ai.rag.datasource.entity.ChatSessionsDO;
import com.mxy.ai.rag.dto.CreateSessionDTO;
import com.mxy.ai.rag.dto.DeleteSessionDTO;
import com.mxy.ai.rag.dto.SessionQueryDTO;
import com.mxy.ai.rag.dto.UpdateSessionTitleDTO;
import com.mxy.ai.rag.service.ChatSessionService;
import com.mxy.ai.rag.util.UserContextUtil;
import com.mxy.ai.rag.web.vo.PageResult;
import com.mxy.ai.rag.web.vo.SessionVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天会话管理服务实现类
 *
 * @author Mxy
 */
@Service
@Slf4j
public class ChatSessionServiceImpl implements ChatSessionService {
    @Resource
    private ChatSessionsDAO chatSessionsDAO;


    @Override
    public Long createSession(CreateSessionDTO dto) {
        log.info("创建会话: {}", dto);
        ChatSessionsDO sessionsDO = ChatSessionsDO.builder()
                .title(dto.getTitle())
                .creator(UserContextUtil.getCurrentUserId())
                .modifier(UserContextUtil.getCurrentUserId())
                .build();
        chatSessionsDAO.save(sessionsDO);
        return sessionsDO.getId();
    }

    @Override
    public SessionVO getSessionById(Long sessionId) {
        ChatSessionsDO chatSessionsDO = chatSessionsDAO.getById(sessionId);
        SessionVO sessionVO = new SessionVO();
        BeanUtils.copyProperties(chatSessionsDO, sessionVO);
        return sessionVO;
    }

    @Override
    public PageResult<SessionVO> getSessionList(SessionQueryDTO dto) {
        // 设置当前用户ID
        dto.setUserId(UserContextUtil.getCurrentUserId());
        
        Page<ChatSessionsDO> page = chatSessionsDAO.getSessionList(dto);
        List<ChatSessionsDO> records = page.getRecords();
        List<SessionVO> sessionVOS = records.stream().map(record -> {
            SessionVO sessionVO = new SessionVO();
            BeanUtils.copyProperties(record, sessionVO);
            return sessionVO;
        }).toList();
        PageResult<SessionVO> result = new PageResult<>(sessionVOS, page.getTotal(), page.getCurrent(), page.getSize());
        log.info("查询会话列表: {}", result);
        return result;
    }

    @Override
    public void updateSessionTitle(UpdateSessionTitleDTO dto) {
        ChatSessionsDO chatSessionsDO = chatSessionsDAO.getById(dto.getSessionId());
        if (chatSessionsDO == null) {
            log.error("会话不存在: {}", dto.getSessionId());
            throw new RuntimeException("会话不存在");
        }
        ChatSessionsDO update = ChatSessionsDO.builder()
                .id(dto.getSessionId())
                .title(dto.getTitle())
                .modifier(UserContextUtil.getCurrentUserId())
                .build();
        chatSessionsDAO.updateById(update);
    }

    @Override
    public void deleteSession(DeleteSessionDTO dto) {
        log.info("删除会话: sessionId={}, userId={}", dto.getSessionId(), dto.getUserId());
        
        // 检查会话是否存在
        ChatSessionsDO chatSessionsDO = chatSessionsDAO.getById(dto.getSessionId());
        if (chatSessionsDO == null) {
            log.error("会话不存在: {}", dto.getSessionId());
            throw new RuntimeException("会话不存在");
        }
        
        // 检查会话是否属于当前用户
        String currentUserId = UserContextUtil.getCurrentUserId();
        if (!currentUserId.equals(chatSessionsDO.getCreator())) {
            log.error("无权限删除会话: sessionId={}, currentUserId={}, creator={}", 
                    dto.getSessionId(), currentUserId, chatSessionsDO.getCreator());
            throw new RuntimeException("无权限删除该会话");
        }
        
        // 软删除会话
        ChatSessionsDO update = ChatSessionsDO.builder()
                .id(dto.getSessionId())
                .deleted(1)
                .modifier(currentUserId)
                .build();
        chatSessionsDAO.updateById(update);
        
        log.info("会话删除成功: sessionId={}", dto.getSessionId());
    }
}
