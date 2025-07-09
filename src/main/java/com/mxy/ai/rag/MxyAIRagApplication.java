package com.mxy.ai.rag;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan("com.mxy.ai.rag.datasource.mapper")
@Slf4j
public class MxyAIRagApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(MxyAIRagApplication.class, args);
        String port = applicationContext.getEnvironment().getProperty("server.port");
        log.info("AI RAG Application started successfully! port:{}", port);
    }
}
