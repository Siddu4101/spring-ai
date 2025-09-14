package com.spring.ai.rest;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/spring-ai")
class SpringAiController {

    private final ChatClient chatClient;

    public SpringAiController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }


    @GetMapping("/chat")
    public ResponseEntity<String> askQuestion(@RequestParam String q){
       return ResponseEntity.ok(chatClient.prompt(q).call().content());
    }
}
