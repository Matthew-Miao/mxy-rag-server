package com.mxy.ai.rag.datasource.dao;

import com.mxy.ai.rag.datasource.entity.SystemConfigDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SystemConfigDAO单元测试类
 * 测试系统配置数据访问层的基本功能
 * --postgresql.host=192.168.1.188 --mysql.host=192.168.1.181 --mysql.username=rongyi --mysql.password=rongyi123$qwer
 */
@SpringBootTest
@Slf4j
public class SystemConfigDAOTest {

    @Resource
    private SystemConfigDAO systemConfigDAO;

    /**
     * 测试插入系统配置数据
     * 验证数据插入功能是否正常
     */
    @Test
    public void testInsertSystemConfig() {
        // 创建测试数据
        SystemConfigDO config = new SystemConfigDO();
        config.setConfigKey("system.name");
        config.setConfigValue("MXY RAG Server");
        config.setConfigType("string");
        config.setDescription("系统名称");
        config.setIsSystem(0);
        config.setIsEncrypted(0);
        config.setDeleted(0);
        config.setGmtCreate(LocalDateTime.now());
        config.setGmtModified(LocalDateTime.now());
        config.setCreator("test_user");
        config.setModifier("test_user");

        // 执行插入操作
        boolean result = systemConfigDAO.save(config);

        // 验证插入结果
        assertTrue(result, "插入系统配置应该成功");
        assertNotNull(config.getId(), "插入后应该生成主键ID");
        assertTrue(config.getId() > 0, "主键ID应该大于0");

        // 验证插入的数据
        SystemConfigDO savedConfig = systemConfigDAO.getById(config.getId());
        assertNotNull(savedConfig, "应该能够查询到插入的数据");
        assertEquals("system.name", savedConfig.getConfigKey());
        assertEquals("MXY RAG Server", savedConfig.getConfigValue());
        assertEquals("string", savedConfig.getConfigType());
        assertEquals("系统名称", savedConfig.getDescription());
    }

    /**
     * 测试查询系统配置列表
     * 验证数据查询功能是否正常
     */
    @Test
    public void testQuerySystemConfigList() {
        // 查询所有配置
        List<SystemConfigDO> allConfigs = systemConfigDAO.list();
        assertNotNull(allConfigs, "查询结果不应该为null");
        log.info("所有配置列表：{}", allConfigs);
    }

}