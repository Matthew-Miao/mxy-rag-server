package com.mxy.ai.rag.service;

import com.mxy.ai.rag.dto.CreateSessionDTO;
import com.mxy.ai.rag.dto.DeleteSessionDTO;
import com.mxy.ai.rag.dto.SessionQueryDTO;
import com.mxy.ai.rag.dto.UpdateSessionTitleDTO;
import com.mxy.ai.rag.web.vo.PageResult;
import com.mxy.ai.rag.web.vo.SessionVO;

/**
 * 
 *    
 * @author mixaoxiaoyu
 * @date 2025-07-07 16:11
 */

public interface ChatSessionService {
    /**
     * 创建会话
     *
     * @param dto
     * @return
     */
    Long createSession(CreateSessionDTO dto);

    /**
     * 获取会话详情
     *
     * @param sessionId
     * @return
     */
    SessionVO getSessionById(Long sessionId);

    /**
     * 更新会话标题
     *
     * @param dto
     * @return
     */
    PageResult<SessionVO> getSessionList(SessionQueryDTO dto);

    /**
     * 更新会话标题
     *
     * @param dto
     * @return
     */
    void updateSessionTitle(UpdateSessionTitleDTO dto);

    /**
     * 删除会话
     *
     * @param dto 删除会话请求参数
     */
    void deleteSession(DeleteSessionDTO dto);
}
