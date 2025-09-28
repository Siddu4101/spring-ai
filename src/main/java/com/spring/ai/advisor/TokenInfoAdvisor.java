package com.spring.ai.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

@Slf4j
public class TokenInfoAdvisor implements CallAdvisor {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
       log.info("The input prompt used is {}", chatClientRequest.prompt());
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        log.info("The number of input tokens consumed for the input prompt {}", chatClientResponse
                .chatResponse().getMetadata().getUsage().getPromptTokens());
        log.info("The number of tokens spent on the output {}", chatClientResponse.chatResponse().getMetadata().getUsage().getCompletionTokens());
        log.info("Total token spent for request and repose is {}", chatClientResponse.chatResponse().getMetadata().getUsage().getTotalTokens());
        return chatClientResponse;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public int getOrder() {
        /*higher the order more the priority in the running multiple advisors*/
        return 0;
    }
}
