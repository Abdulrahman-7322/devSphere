package com.shutu.interview.controller;

import com.shutu.interview.service.AIService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return aiService.chat(message);
    }

    @GetMapping("/stream-chat")
    public Flux<String> streamChat(@RequestParam String message) {
        return aiService.streamChat(message);
    }
}
