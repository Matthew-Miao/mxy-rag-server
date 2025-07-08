package com.mxy.ai.rag.service.impl;

import com.mxy.ai.rag.datasource.dao.UsersDAO;
import com.mxy.ai.rag.datasource.entity.UsersDO;
import com.mxy.ai.rag.dto.UserChangePasswordDTO;
import com.mxy.ai.rag.dto.UserLoginDTO;
import com.mxy.ai.rag.dto.UserRegisterDTO;
import com.mxy.ai.rag.service.UserService;
import com.mxy.ai.rag.web.vo.UserVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户管理服务实现类
 * 提供用户注册、登录、密码修改等功能的具体实现
 *
 * @author Mxy
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private UsersDAO usersDAO;

    /**
     * 用户注册
     *
     * @param dto 用户注册请求参数
     * @return 注册成功的用户信息
     */
    @Override
    @Transactional
    public UserVO register(UserRegisterDTO dto) {
        Assert.hasText(dto.getUsername(), "用户名不能为空");
        Assert.hasText(dto.getPassword(), "密码不能为空");
        Assert.hasText(dto.getConfirmPassword(), "确认密码不能为空");
        
        logger.info("开始处理用户注册: username={}", dto.getUsername());
        
        // 验证密码和确认密码是否一致
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("密码和确认密码不一致");
        }
        
        // 检查用户名是否已存在
        if (isUsernameExists(dto.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        try {
            // 创建用户实体
            UsersDO usersDO = new UsersDO();
            usersDO.setUsername(dto.getUsername());
            usersDO.setPassword(encryptPassword(dto.getPassword()));
            usersDO.setUserId(UUID.randomUUID().toString());
            usersDO.setDeleted(0);
            usersDO.setGmtCreate(LocalDateTime.now());
            usersDO.setGmtModified(LocalDateTime.now());
            usersDO.setCreator("system");
            usersDO.setModifier("system");
            
            // 保存用户
            usersDAO.save(usersDO);
            
            logger.info("用户注册成功: username={}, userId={}", dto.getUsername(), usersDO.getUserId());
            
            // 转换为VO返回
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(usersDO, userVO);
            return userVO;
            
        } catch (Exception e) {
            logger.error("用户注册失败: {}", e.getMessage(), e);
            throw new RuntimeException("用户注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     *
     * @param dto 用户登录请求参数
     * @return 登录成功的用户信息
     */
    @Override
    public UserVO login(UserLoginDTO dto) {
        Assert.hasText(dto.getUsername(), "用户名不能为空");
        Assert.hasText(dto.getPassword(), "密码不能为空");
        
        logger.info("开始处理用户登录: username={}", dto.getUsername());
        
        try {
            // 根据用户名查询用户
            UsersDO usersDO = usersDAO.lambdaQuery()
                    .eq(UsersDO::getUsername, dto.getUsername())
                    .eq(UsersDO::getDeleted, 0)
                    .one();
            
            if (usersDO == null) {
                throw new RuntimeException("用户名或密码错误");
            }
            
            // 验证密码
            String encryptedPassword = encryptPassword(dto.getPassword());
            if (!encryptedPassword.equals(usersDO.getPassword())) {
                throw new RuntimeException("用户名或密码错误");
            }
            
            logger.info("用户登录成功: username={}, userId={}", dto.getUsername(), usersDO.getUserId());
            
            // 转换为VO返回
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(usersDO, userVO);
            return userVO;
            
        } catch (Exception e) {
            logger.error("用户登录失败: {}", e.getMessage(), e);
            throw new RuntimeException("用户登录失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     *
     * @param dto 修改密码请求参数
     */
    @Override
    @Transactional
    public void changePassword(UserChangePasswordDTO dto) {
        Assert.hasText(dto.getUserId(), "用户ID不能为空");
        Assert.hasText(dto.getOldPassword(), "原密码不能为空");
        Assert.hasText(dto.getNewPassword(), "新密码不能为空");
        Assert.hasText(dto.getConfirmNewPassword(), "确认新密码不能为空");
        
        logger.info("开始处理密码修改: userId={}", dto.getUserId());
        
        // 验证新密码和确认新密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new RuntimeException("新密码和确认新密码不一致");
        }
        
        try {
            // 根据用户ID查询用户
            UsersDO usersDO = usersDAO.getByUserId(dto.getUserId());
            if (usersDO == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 验证原密码
            String encryptedOldPassword = encryptPassword(dto.getOldPassword());
            if (!encryptedOldPassword.equals(usersDO.getPassword())) {
                throw new RuntimeException("原密码错误");
            }
            
            // 更新密码
            String encryptedNewPassword = encryptPassword(dto.getNewPassword());
            usersDAO.changePassword(dto, encryptedNewPassword);

            logger.info("密码修改成功: userId={}", dto.getUserId());
            
        } catch (Exception e) {
            logger.error("密码修改失败: {}", e.getMessage(), e);
            throw new RuntimeException("密码修改失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public UserVO getUserInfo(String userId) {
        Assert.hasText(userId, "用户ID不能为空");
        
        try {
            UsersDO usersDO = usersDAO.getByUserId(userId);
            if (usersDO == null) {
                throw new RuntimeException("用户不存在");
            }
            
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(usersDO, userVO);
            return userVO;
            
        } catch (Exception e) {
            logger.error("获取用户信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return true-已存在，false-不存在
     */
    @Override
    public boolean isUsernameExists(String username) {
        Assert.hasText(username, "用户名不能为空");
        
        UsersDO usersDO = usersDAO.isUsernameExists(username);
        return usersDO != null;
    }

    /**
     * 密码加密
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    private String encryptPassword(String password) {
        // 使用MD5加密密码（实际项目中建议使用更安全的加密方式，如BCrypt）
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }
}