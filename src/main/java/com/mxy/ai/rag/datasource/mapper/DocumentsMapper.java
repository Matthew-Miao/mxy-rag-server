package com.mxy.ai.rag.datasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mxy.ai.rag.datasource.entity.DocumentsDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档管理表Mapper接口
 * 提供文档数据的基础CRUD操作
 */
@Mapper
public interface DocumentsMapper extends BaseMapper<DocumentsDO> {
}