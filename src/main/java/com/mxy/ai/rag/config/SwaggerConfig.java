package com.mxy.ai.rag.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 * 用于配置API文档的基本信息和展示
 * 
 * @author Mxy
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置OpenAPI文档信息
     * 
     * @return OpenAPI配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MXY RAG Server API")
                        .version("1.0.0")
                        .description("MXY RAG服务器API文档，提供聊天会话管理、文档处理等功能")
                        .contact(new Contact()
                                .name("Mxy")
                                .email("mxy@example.com")
                                .url("https://github.com/mxy"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}