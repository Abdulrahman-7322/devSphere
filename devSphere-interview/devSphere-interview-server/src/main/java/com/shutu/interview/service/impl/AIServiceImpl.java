package com.shutu.interview.service.impl;

import com.shutu.interview.service.AIService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AIServiceImpl implements AIService {

    private final ChatClient chatClient;

    public AIServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String chat(String system, String user) {
        return chatClient.prompt()
                .system(system)
                .user(user)
                .call()
                .content();
    }

    @Override
    public Flux<String> streamChat(String system, String user) {
        return chatClient.prompt()
                .system(system)
                .user(user)
                .stream()
                .content();
    }

    // Deprecated or Default implementations
    @Override
    public String chat(String message) {
        return chat("You are a helpful AI assistant.", message);
    }

    @Override
    public Flux<String> streamChat(String message) {
        return streamChat("You are a helpful AI assistant.", message);
    }
}
