package com.mxy.ai.rag.service;

import com.mxy.ai.rag.dto.UserChangePasswordDTO;
import com.mxy.ai.rag.dto.UserLoginDTO;
import com.mxy.ai.rag.dto.UserRegisterDTO;
import com.mxy.ai.rag.web.vo.UserVO;

/**
 * 用户管理服务接口
 * 提供用户注册、登录、密码修改等功能
 *
 * @author Mxy
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param dto 用户注册请求参数
     * @return 注册成功的用户信息
     */
    UserVO register(UserRegisterDTO dto);

    /**
     * 用户登录
     *
     * @param dto 用户登录请求参数
     * @return 登录成功的用户信息
     */
    UserVO login(UserLoginDTO dto);

    /**
     * 修改密码
     *
     * @param dto 修改密码请求参数
     */
    void changePassword(UserChangePasswordDTO dto);

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserInfo(String userId);

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return true-已存在，false-不存在
     */
    boolean isUsernameExists(String username);
}