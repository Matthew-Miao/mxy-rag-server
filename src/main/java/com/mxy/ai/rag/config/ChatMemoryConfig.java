package com.mxy.ai.rag.config;

import com.mxy.ai.rag.service.memory.CustomChatMemoryRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.Resource;

/**
 * 聊天记忆配置类
 * 配置Spring AI的聊天记忆功能，使用自定义的ChatMemoryRepository实现
 * 
 * @author Mxy
 */
@Configuration
public class ChatMemoryConfig {

    @Resource
    private CustomChatMemoryRepository customChatMemoryRepository;

    /**
     * 配置聊天记忆仓库
     * 使用自定义的CustomChatMemoryRepository作为主要的ChatMemoryRepository实现
     * 
     * @return ChatMemoryRepository实例
     */
    @Bean
    @Primary
    public ChatMemoryRepository chatMemoryRepository() {
        return customChatMemoryRepository;
    }

    @Bean
    @Primary
    public ChatMemory chatMemory() {
       return MessageWindowChatMemory.builder()
                .chatMemoryRepository(customChatMemoryRepository)
                .maxMessages(10).build();
    }
}