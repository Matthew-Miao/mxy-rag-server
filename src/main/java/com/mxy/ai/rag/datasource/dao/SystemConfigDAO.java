package com.mxy.ai.rag.datasource.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mxy.ai.rag.datasource.entity.SystemConfigDO;
import com.mxy.ai.rag.datasource.mapper.SystemConfigMapper;
import org.springframework.stereotype.Repository;

/**
 * 系统配置表DAO类
 * 提供系统配置数据的业务层操作
 */
@Repository
public class SystemConfigDAO extends ServiceImpl<SystemConfigMapper, SystemConfigDO> {
}