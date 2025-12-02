package com.shutu.interview.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;

@Component
public class AudioStreamHandler extends BinaryWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Audio stream connection established: " + session.getId());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        ByteBuffer payload = message.getPayload();
        // Mock ASR: In a real scenario, we would send this to FunASR via WebSocket or gRPC
        // Here we just simulate a transcription after receiving enough data or a specific signal
        
        // For demonstration, we assume every chunk is processed and returns a mock text
        // In production, this would be asynchronous
        
        String mockTranscription = "{\"type\": \"transcription\", \"text\": \"This is a simulated ASR result.\"}";
        session.sendMessage(new org.springframework.web.socket.TextMessage(mockTranscription));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Audio stream connection closed: " + session.getId());
    }
}
