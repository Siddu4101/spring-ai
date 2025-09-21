package com.spring.ai.rest;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.ai.model.News;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/spring-ai")
class SpringAiController {

    private final ChatClient chatClient;

    public SpringAiController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }


    @GetMapping("/chat")
    public ResponseEntity<String> askQuestion(@RequestParam String q){

//        /*passing prompt instead of simple query*/
        Prompt prompt = new Prompt("What can you do for me ?");
        String content = chatClient.prompt(prompt).call().content();

        log.info("Response from using prompt obj {}", content);

        /*getting metadata about the request*/
        ChatResponseMetadata metadata = chatClient.prompt(q)
                .call()
                .chatResponse()
                .getMetadata();
        log.info("MetaDate: {} ", metadata);

        /*chatResponse*/
        String response = chatClient.prompt(q)
                .call()
                .chatResponse()
                .getResults() /*multi response*/
                .get(0)/*picking one*/
                .getOutput()
                .getText();

        log.info("The response from the model {}", response);

        /*using simple string*/
       return ResponseEntity.ok(chatClient.prompt(q).call().content());

    }

    @GetMapping("/news")
    public ResponseEntity<News> getNewNews(){

        /*setting default chat options this can be done at any level for chat client(while building chat client too) */
        /*at prompt level */
        /*higher the temp more the inconsistency */
        Prompt prompt = new Prompt("get a new topic and some short description about the topic you chosen and Respond ONLY with a single valid JSON object, not an array"
                , OllamaOptions.builder().temperature(0.8).build());

        News asNewContentCreator = chatClient.prompt(prompt)
                .system("as new content creator")
                .call()
                .entity(News.class);/*to map the response to a structure*/

        log.info("The new response with tweaked temp {}", asNewContentCreator);


        /*returning a response in a structured way*/
        return ResponseEntity.ok(
                chatClient
                        .prompt()
                        .user("get a new topic and some short description about the topic you chosen and Respond ONLY with a single valid JSON object, not an array")
                        .system("act as a senior journalist")/*to give more context and actor */
                        .call()
                        .entity(News.class)/*to map the response to a structure*/

        );

    }

    @GetMapping("/template-test")
    public ResponseEntity<String> getNewInfo(@RequestParam String topic){
        /*using prompt template to add user query */
        return ResponseEntity.ok(chatClient
                .prompt()
                .user(u-> u.text("Give me a brief information about the {topic}").param("topic", topic))
                .call()
                .content());
    }
}
