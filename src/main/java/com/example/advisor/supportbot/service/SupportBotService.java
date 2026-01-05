package com.example.advisor.supportbot.service;

import com.example.advisor.supportbot.advisor.*;
import com.example.advisor.supportbot.model.dto.ChatRequest;
import com.example.advisor.supportbot.model.dto.ChatResponse;
import com.example.advisor.supportbot.tool.TicketTools;
import com.example.advisor.supportbot.model.enums.SentimentType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Main service for the Customer Support Bot.
 * Orchestrates the advisor chain and handles chat interactions.
 */
@Service
public class SupportBotService {

    private static final String SYSTEM_PROMPT = """
            You are a friendly and professional customer support agent for TaskFlow,
            a project management and team collaboration platform.

            Your responsibilities:
            1. Answer questions about TaskFlow features, pricing, and usage
            2. Help troubleshoot common issues
            3. Guide users through processes step-by-step
            4. Escalate to human support when necessary

            Guidelines:
            - Be concise but thorough
            - Use markdown formatting for clarity (bullet points, bold, code blocks)
            - Always maintain a helpful and positive tone
            - If you don't know something, admit it and offer alternatives
            - Never make up information about features or pricing
            """;

    private final ChatClient chatClient;
    private final KnowledgeBaseService knowledgeBaseService;

    // Advisors
    private final SupportSafetyAdvisor safetyAdvisor;
    private final CustomerContextAdvisor customerContextAdvisor;
    private final SentimentAnalysisAdvisor sentimentAdvisor;
    private final ResponseFormattingAdvisor formattingAdvisor;
    private final MessageChatMemoryAdvisor memoryAdvisor;

    // Tools (Agentic AI)
    private final TicketTools ticketTools;

    public SupportBotService(
            ChatClient.Builder chatClientBuilder,
            KnowledgeBaseService knowledgeBaseService,
            SupportSafetyAdvisor safetyAdvisor,
            CustomerContextAdvisor customerContextAdvisor,
            SentimentAnalysisAdvisor sentimentAdvisor,
            TicketTools ticketTools,
            ResponseFormattingAdvisor formattingAdvisor,
            @Qualifier("supportBotMemoryAdvisor") MessageChatMemoryAdvisor memoryAdvisor) {

        this.chatClient = chatClientBuilder.build();
        this.knowledgeBaseService = knowledgeBaseService;
        this.safetyAdvisor = safetyAdvisor;
        this.customerContextAdvisor = customerContextAdvisor;
        this.sentimentAdvisor = sentimentAdvisor;
        this.ticketTools = ticketTools;
        this.formattingAdvisor = formattingAdvisor;
        this.memoryAdvisor = memoryAdvisor;
    }

    /**
     * Handles a chat message from a customer.
     * 
     * @param request The chat request containing message and customer info
     * @return ChatResponse with AI response and metadata
     */
    public ChatResponse chat(ChatRequest request) {
        // Get RAG context from knowledge base
        String context = knowledgeBaseService.getContextForQuery(request.message(), 3);
        String category = knowledgeBaseService.categorizeQuery(request.message());

        // Build enhanced system prompt with RAG context
        String enhancedSystemPrompt = SYSTEM_PROMPT + "\n\n## Knowledge Base Context\n" +
                "Category: " + category + "\n\n" + context;

        // Set customer context for advisors
        Long customerId = parseCustomerId(request.customerId());
        if (customerId != null) {
            customerContextAdvisor.setCustomerId(customerId);
        }

        // Determine conversation ID for memory
        String conversationId = request.sessionId() != null ? request.sessionId()
                : (request.customerId() != null ? "customer-" + request.customerId() : "anonymous");

        // Execute chat with full advisor chain + TOOLS
        String content = chatClient.prompt()
                .system(enhancedSystemPrompt)
                .user(request.message())
                // Advisor chain
                .advisors(safetyAdvisor) // 1. Safety check
                .advisors(customerContextAdvisor) // 2. Customer personalization
                .advisors(sentimentAdvisor) // 3. Sentiment analysis
                .advisors(memoryAdvisor) // 4. Chat memory
                .advisors(formattingAdvisor) // 5. Response formatting
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                // LEVEL 4 AGENTIC UPGRADE: Tools
                .tools(ticketTools)
                .call()
                .content();

        // Get sentiment
        SentimentType sentiment = sentimentAdvisor.getLastSentiment();

        // For tools, we don't manually check "lastCreatedTicket" via advisor anymore.
        // The LLM tool execution happens internally.
        // If we wanted to return ticket ID, we'd need to capture tool events or parsing
        // logs.
        // For this tutorial, we will rely on the AI's text response confirming the
        // ticket creation.

        return new ChatResponse(
                content,
                conversationId,
                sentiment,
                false, // Ticket created flag strictly via Advisor is deprecated
                null,
                java.time.LocalDateTime.now());
    }

    /**
     * Simple chat without customer context (for demo/testing).
     */
    public String simpleChat(String message) {
        return chat(ChatRequest.anonymous(message)).message();
    }

    /**
     * Chat with customer context.
     */
    public ChatResponse chatWithCustomer(String message, Long customerId) {
        return chat(new ChatRequest(message, String.valueOf(customerId), null));
    }

    /**
     * Parses customer ID from string.
     */
    private Long parseCustomerId(String customerIdStr) {
        if (customerIdStr == null || customerIdStr.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(customerIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
