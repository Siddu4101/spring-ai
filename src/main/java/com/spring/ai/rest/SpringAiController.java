package com.spring.ai.rest;

import com.spring.ai.advisor.TokenInfoAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.ai.model.News;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/spring-ai")
class SpringAiController {


    private final ChatClient chatClient;

    @Value("classpath:/prompt/user-message.st")
    private Resource userResource;

    @Value("classpath:/prompt/system-message.st")
    private Resource systemResource;

    SpringAiController(ChatClient chatClient) {
        this.chatClient = chatClient;
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
        /*using consumer to create prompt */
        String responseViaConsumerPrompt = chatClient
                .prompt()
                .user(u -> u.text("Give me a brief information about the {topic}").param("topic", topic))
                .call()
                .content();
        log.info("Response via consumer prompt {}", responseViaConsumerPrompt);



        /*using prompt template to create a prompt*/
        /*1.create a template*/
        PromptTemplate userTemplate = PromptTemplate.builder().template("Tell me more about the {topic}? and why it is used?").build();

        /*2.render it to add the params*/
        String renderedPrompt = userTemplate.render(Map.of("topic", topic));

        /*3.create prompt out of it*/
        Prompt userPrompt = new Prompt(renderedPrompt);

        /*4.use this prompt for the request*/
        String content = chatClient.prompt(userPrompt).call().content();
        log.info("response via PromptTemplate(userTemplate){} ", content);


        /*5. we can add the System templates too*/
        SystemPromptTemplate systemTemplate = SystemPromptTemplate.builder().template("Act as a all known agentic AI who can answer all the question").build();

        /*6. render this */
        String renderSystemPrompt = systemTemplate.render();

        String responseWithUserAndSystemTemplate = chatClient.prompt(userPrompt)
                .system(renderSystemPrompt)
                .call()
                .content();
        log.info("Response from system + user prompt template {}", responseWithUserAndSystemTemplate);


        /*Using fluent api*/

        String responseViaFluentApiTemplate = chatClient.prompt()
                .user(u -> u.text("Tell me more about the {topic}? and why it is used?").param("topic", topic))
                .system(s -> s.text("consider you are experienced person in the {topic} field and then answer this question").param("topic", topic))
                .call()
                .content();
        log.info("The response from the fluent api templating is {}", responseViaFluentApiTemplate);

        /*Using resource from the file*/
        String responseViaResourceTemplate = chatClient.prompt()
                .user(u-> u.text(userResource).param("topic", topic))
                .system(s -> s.text(systemResource).param("topic", topic))
                .call()
                .content();
        log.info("The response from the fluent api templating is {}", responseViaResourceTemplate);
        return ResponseEntity.ok(responseViaResourceTemplate);
    }

    @GetMapping("/advisor")
    public ResponseEntity<String> testAdvisor(@RequestParam String q){
        log.info("test advisor / interceptor");
        /*this advisor is mainly used asa interceptor between model request and response */
        /*we have one safeguard advisor which can handle bad words and simple log one iss to log the info before sending and receiving data from chat model */
        return ResponseEntity.ok(chatClient.prompt(q)
                .advisors(new SimpleLoggerAdvisor(),new SafeGuardAdvisor(List.of("sid", "game")))
                .call()
                .content());
    }

    @GetMapping("/custom-advisor")
    public ResponseEntity<String> testCustomAdvisor(@RequestParam String q){
        log.info("test custom advisor / interceptor");
        return ResponseEntity.ok(chatClient.prompt(q)
                        .system("as a great professor in java language")
                .advisors(new TokenInfoAdvisor())
                .call()
                .content());
    }

    @GetMapping("/streaming-response")
    public ResponseEntity<Flux<String>> streamingResponseForQuery(@RequestParam String q){
        log.info("streaming response test");
        return ResponseEntity.ok(chatClient.prompt(q)
                .system("as a great professor in java language")
                .stream()
                .content());
    }

}
