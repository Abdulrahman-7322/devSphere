package com.shutu.interview.service;

import reactor.core.publisher.Flux;

public interface AIService {
    /**
     * Chat with system prompt (Persona)
     * @param system System prompt
     * @param user User message
     * @return LLM response
     */
    String chat(String system, String user);

    /**
     * Stream chat with system prompt
     * @param system System prompt
     * @param user User message
     * @return Flux of LLM response chunks
     */
    Flux<String> streamChat(String system, String user);

    /**
     * Simple chat with default persona
     * @param message User message
     * @return LLM response
     */
    String chat(String message);

    /**
     * Stream chat with default persona
     * @param message User message
     * @return Flux of LLM response chunks
     */
    Flux<String> streamChat(String message);
}
