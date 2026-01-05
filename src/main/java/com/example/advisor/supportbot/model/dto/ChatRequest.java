package com.example.advisor.supportbot.model.dto;

import com.example.advisor.supportbot.model.enums.SentimentType;

/**
 * Request DTO for chat messages.
 */
public record ChatRequest(
        String message,
        String customerId,
        String sessionId) {
    /**
     * Creates a chat request with just a message (for anonymous users).
     */
    public static ChatRequest anonymous(String message) {
        return new ChatRequest(message, null, null);
    }

    /**
     * Creates a chat request for an identified customer.
     */
    public static ChatRequest forCustomer(String message, String customerId) {
        return new ChatRequest(message, customerId, null);
    }
}
