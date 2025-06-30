package com.mxy.ai.rag.datasource.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mxy.ai.rag.datasource.entity.DocumentsDO;
import com.mxy.ai.rag.datasource.mapper.DocumentsMapper;
import org.springframework.stereotype.Repository;

/**
 * 文档管理表DAO类
 * 提供文档数据的业务层操作
 */
@Repository
public class DocumentsDAO extends ServiceImpl<DocumentsMapper, DocumentsDO> {
}