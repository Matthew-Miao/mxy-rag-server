package com.mxy.ai.rag.datasource.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mxy.ai.rag.datasource.entity.OperationLogsDO;
import com.mxy.ai.rag.datasource.mapper.OperationLogsMapper;
import org.springframework.stereotype.Repository;

/**
 * 操作日志表DAO类
 * 提供操作日志数据的业务层操作
 */
@Repository
public class OperationLogsDAO extends ServiceImpl<OperationLogsMapper, OperationLogsDO> {
}