package com.mxy.ai.rag.datasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mxy.ai.rag.datasource.entity.OperationLogsDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志表Mapper接口
 * 提供操作日志数据的基础CRUD操作
 */
@Mapper
public interface OperationLogsMapper extends BaseMapper<OperationLogsDO> {

}