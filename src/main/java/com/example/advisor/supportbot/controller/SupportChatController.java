package com.example.advisor.supportbot.controller;

import com.example.advisor.supportbot.model.dto.ChatRequest;
import com.example.advisor.supportbot.model.dto.ChatResponse;
import com.example.advisor.supportbot.service.SupportBotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Support Bot chat interactions.
 */
@RestController
@RequestMapping("/support")
public class SupportChatController {

    private final SupportBotService supportBotService;

    public SupportChatController(SupportBotService supportBotService) {
        this.supportBotService = supportBotService;
    }

    /**
     * Main chat endpoint.
     * 
     * POST /support/chat
     * Body: { "message": "...", "customerId": "1", "sessionId": "..." }
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = supportBotService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Streaming chat endpoint (Level 4.5).
     * Uses Server-Sent Events (SSE) to push tokens.
     */
    @GetMapping(value = "/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public reactor.core.publisher.Flux<String> streamChat(
            @RequestParam String message,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String sessionId) {

        ChatRequest request = new ChatRequest(message, customerId, sessionId);
        return supportBotService.streamChat(request);
    }

    /**
     * Simple chat endpoint (GET for easy testing).
     * 
     * GET /support/chat?message=...
     */
    @GetMapping("/chat")
    public ResponseEntity<ChatResponse> simpleChat(
            @RequestParam String message,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String sessionId) {

        ChatRequest request = new ChatRequest(message, customerId, sessionId);
        ChatResponse response = supportBotService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Chat with customer context.
     * 
     * GET /support/chat/customer/{customerId}?message=...
     */
    @GetMapping("/chat/customer/{customerId}")
    public ResponseEntity<ChatResponse> chatWithCustomer(
            @PathVariable Long customerId,
            @RequestParam String message) {

        ChatResponse response = supportBotService.chatWithCustomer(message, customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Support Bot is running!");
    }
}
