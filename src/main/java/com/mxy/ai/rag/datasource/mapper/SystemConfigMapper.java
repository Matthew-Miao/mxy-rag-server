package com.mxy.ai.rag.datasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mxy.ai.rag.datasource.entity.SystemConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置表Mapper接口
 * 提供系统配置数据的基础CRUD操作
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfigDO> {
}