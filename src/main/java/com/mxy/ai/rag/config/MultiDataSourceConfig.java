package com.mxy.ai.rag.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 业务数据库配置
 */
@Configuration
@Slf4j
public class MultiDataSourceConfig {
    @Bean(name = "pgVectorDataSourceProperties")
    @ConfigurationProperties("spring.datasource.pgvector")
    public DataSourceProperties pgVectorDataSourceProperties() {
        return new DataSourceProperties();
    }
    /**
     * 初始化向量数据库 - 使用HikariDataSource确保属性正确绑定
     */
    @Bean("pgVectorDataSource")
    public DataSource vectorDataSource(@Qualifier("pgVectorDataSourceProperties") DataSourceProperties dataSourceProperties) {
        log.info("初始化向量数据库");
        return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
    public JdbcTemplate pgVectorJdbcTemplate(@Qualifier("pgVectorDataSource") DataSource dataSource) {
        log.info("初始化JdbcTemplate");
        return new JdbcTemplate(dataSource);
    }

    @Primary
    @Bean(name = "mysqlDataSourceProperties")
    @ConfigurationProperties("spring.datasource.mysql")
    public DataSourceProperties mysqlDataSourceProperties() {
        return new DataSourceProperties();
    }


    /**
     * 业务数据源 - 使用HikariDataSource确保属性正确绑定
     */
    @Bean("mysqlDataSource")
    @Primary
    public DataSource masterDataSource(@Qualifier("mysqlDataSourceProperties")
                                           DataSourceProperties dataSourceProperties) {
        log.info("初始化主数据源");
        return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }


    /**
     * SqlSessionFactory配置
     */
    @Bean("sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("mysqlDataSource") DataSource mysqlDataSource) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(mysqlDataSource);
        // 设置mapper文件位置
        sessionFactory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*Mapper*.xml"));
        // 设置MyBatis Plus插件
        sessionFactory.setPlugins(mybatisPlusInterceptor());
        log.info("初始化SqlSessionFactory");
        return sessionFactory.getObject();
    }

    /**
     * 事务管理器
     */
    @Bean("transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(@Qualifier("mysqlDataSource") DataSource mysqlDataSource) {
        log.info("初始化事务管理器");
        return new DataSourceTransactionManager(mysqlDataSource);
    }

    /**
     * MyBatis Plus插件配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setMaxLimit(1000L); // 设置最大分页限制
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        log.info("初始化MyBatis Plus拦截器");
        return interceptor;
    }
}
