package com.mxy.ai.rag.config.session;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executor;

/**
 * Web配置类
 * 配置Spring MVC相关设置，包括拦截器注册
 * 
 * @author Mxy
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Resource
    private UserAuthInterceptor userAuthInterceptor;
    
    @Resource(name = "ttlTaskExecutor")
    private ThreadPoolTaskExecutor ttlTaskExecutor;
    
    /**
     * 添加拦截器配置
     * 注册用户认证拦截器，拦截所有API请求进行用户身份验证
     * 
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userAuthInterceptor)
                // 拦截所有API请求
                .addPathPatterns("/api/**")
                // 排除不需要认证的路径
                .excludePathPatterns(
                    // 健康检查
                    "/actuator/**",
                    // Swagger文档
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    // 静态资源
                    "/static/**",
                    "/public/**",
                    "/favicon.ico",
                    // 错误页面
                    "/error/**"
                )
                // 设置拦截器顺序（数字越小优先级越高）
                .order(1);
    }
    
    /**
     * 配置CORS跨域支持
     * 允许前端页面跨域访问API接口
     * 
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许所有来源（开发环境）
                .allowedOriginPatterns("*")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许发送Cookie
                .allowCredentials(true)
                // 预检请求缓存时间（1小时）
                .maxAge(3600);
    }
    
    /**
     * 配置异步支持
     * 使用自定义的TTL任务执行器替代默认的SimpleAsyncTaskExecutor
     * 解决生产环境下的异步处理性能问题，并保持用户上下文传递
     * 
     * @param configurer 异步支持配置器
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 设置异步请求的任务执行器
        configurer.setTaskExecutor(ttlTaskExecutor);
        // 设置异步请求超时时间（30秒）
        configurer.setDefaultTimeout(30000);
    }
}