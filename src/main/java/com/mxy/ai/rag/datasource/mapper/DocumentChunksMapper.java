package com.mxy.ai.rag.datasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mxy.ai.rag.datasource.entity.DocumentChunksDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档分块表Mapper接口
 * 提供文档分块数据的基础CRUD操作
 */
@Mapper
public interface DocumentChunksMapper extends BaseMapper<DocumentChunksDO> {
}