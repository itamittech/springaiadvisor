package com.example.advisor.supportbot.model.dto;

import com.example.advisor.supportbot.model.enums.SentimentType;
import java.time.LocalDateTime;

/**
 * Response DTO for chat messages.
 */
public record ChatResponse(
        String message,
        String sessionId,
        SentimentType sentiment,
        boolean ticketCreated,
        Long ticketId,
        LocalDateTime timestamp) {
    /**
     * Creates a simple chat response.
     */
    public static ChatResponse simple(String message, String sessionId) {
        return new ChatResponse(message, sessionId, SentimentType.NEUTRAL, false, null, LocalDateTime.now());
    }

    /**
     * Creates a chat response with sentiment.
     */
    public static ChatResponse withSentiment(String message, String sessionId, SentimentType sentiment) {
        return new ChatResponse(message, sessionId, sentiment, false, null, LocalDateTime.now());
    }

    /**
     * Creates a chat response when a ticket was escalated.
     */
    public static ChatResponse withTicket(String message, String sessionId, SentimentType sentiment, Long ticketId) {
        return new ChatResponse(message, sessionId, sentiment, true, ticketId, LocalDateTime.now());
    }
}
