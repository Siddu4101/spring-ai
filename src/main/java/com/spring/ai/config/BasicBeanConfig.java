package com.spring.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class BasicBeanConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder){
        log.info("Creating a bean for chat client");
        return chatClientBuilder.build();
    }
}
