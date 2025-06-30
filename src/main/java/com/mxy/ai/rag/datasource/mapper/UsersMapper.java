package com.mxy.ai.rag.datasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mxy.ai.rag.datasource.entity.UsersDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表Mapper接口
 * 提供用户数据的基础CRUD操作
 */
@Mapper
public interface UsersMapper extends BaseMapper<UsersDO> {
}