package com.mxy.ai.rag.datasource.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mxy.ai.rag.datasource.entity.UsersDO;
import com.mxy.ai.rag.datasource.mapper.UsersMapper;
import org.springframework.stereotype.Repository;

/**
 * 用户表DAO类
 * 提供用户数据的业务层操作
 */
@Repository
public class UsersDAO extends ServiceImpl<UsersMapper, UsersDO> {
}