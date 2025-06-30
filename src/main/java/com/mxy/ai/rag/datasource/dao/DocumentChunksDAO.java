package com.mxy.ai.rag.datasource.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mxy.ai.rag.datasource.entity.DocumentChunksDO;
import com.mxy.ai.rag.datasource.mapper.DocumentChunksMapper;
import org.springframework.stereotype.Repository;

/**
 * 文档分块表DAO类
 * 提供文档分块数据的业务层操作
 */
@Repository
public class DocumentChunksDAO extends ServiceImpl<DocumentChunksMapper, DocumentChunksDO> {
}