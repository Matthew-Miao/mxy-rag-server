package com.mxy.ai.rag.datasource.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mxy.ai.rag.datasource.entity.UsersDO;
import com.mxy.ai.rag.datasource.mapper.UsersMapper;
import com.mxy.ai.rag.dto.UserChangePasswordDTO;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户表DAO类
 * 提供用户数据的业务层操作
 */
@Repository
public class UsersDAO extends ServiceImpl<UsersMapper, UsersDO> {

    public UsersDO getByUserId(String userId) {
        return lambdaQuery().eq(UsersDO::getUserId, userId).last("limit 1").one();
    }

    /**
     * 修改密码
     *
     * @param dto                   修改密码请求参数
     * @param encryptedNewPassword  新密码的加密值
     */
    public void changePassword(UserChangePasswordDTO dto, String encryptedNewPassword) {
        lambdaUpdate()
                .eq(UsersDO::getUserId, dto.getUserId())
                .set(UsersDO::getPassword, encryptedNewPassword)
                .set(UsersDO::getGmtModified, LocalDateTime.now())
                .set(UsersDO::getModifier, dto.getUserId())
                .update();
    }

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 用户信息
     */
    public UsersDO isUsernameExists(String username) {
       return lambdaQuery()
                .eq(UsersDO::getUsername, username)
                .eq(UsersDO::getDeleted, 0)
                .last("limit 1")
                .one();
    }
}