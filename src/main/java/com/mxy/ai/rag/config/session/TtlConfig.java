package com.mxy.ai.rag.config.session;

import com.alibaba.ttl.threadpool.TtlExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * TTL（TransmittableThreadLocal）配置类
 * 配置支持TTL的线程池，确保异步任务中能够正确传递用户上下文
 * 
 * @author Mxy
 */
@Configuration
public class TtlConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(TtlConfig.class);
    
    /**
     * 配置支持TTL的异步任务执行器
     * 使用TTL装饰器包装线程池，确保异步任务中能够获取到主线程的用户上下文
     * 
     * @return 支持TTL的任务执行器
     */
    @Bean("ttlTaskExecutor")
    public ThreadPoolTaskExecutor ttlTaskExecutor() {
        logger.info("初始化支持TTL的线程池执行器");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(20);
        // 队列容量
        executor.setQueueCapacity(100);
        // 线程名前缀
        executor.setThreadNamePrefix("ttl-async-");
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        // 使用TTL装饰器包装线程池，支持上下文传递
        return executor;
    }
    
    /**
     * 配置支持TTL的调度任务执行器
     * 用于定时任务等场景
     * 
     * @return 支持TTL的调度执行器
     */
    @Bean("ttlScheduledExecutor")
    public Executor ttlScheduledExecutor() {
        logger.info("初始化支持TTL的调度线程池执行器");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ttl-scheduled-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        return TtlExecutors.getTtlExecutor(executor.getThreadPoolExecutor());
    }
}