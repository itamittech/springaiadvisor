package com.example.advisor.supportbot.service;

import com.example.advisor.supportbot.advisor.*;
import com.example.advisor.supportbot.model.dto.ChatRequest;
import com.example.advisor.supportbot.model.dto.ChatResponse;
import com.example.advisor.supportbot.tool.TicketTools;
import com.example.advisor.supportbot.model.enums.SentimentType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Main service for the Customer Support Bot.
 * Orchestrates the advisor chain and handles chat interactions.
 */
@Service
public class SupportBotService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SupportBotService.class);

    private final ChatClient chatClient;
    private final KnowledgeBaseService knowledgeBaseService;

    @Value("classpath:/prompts/system.st")
    private Resource systemPromptResource;

    // Advisors
    private final SupportSafetyAdvisor safetyAdvisor;
    private final CustomerContextAdvisor customerContextAdvisor;
    private final SentimentAnalysisAdvisor sentimentAdvisor;
    private final ResponseFormattingAdvisor formattingAdvisor;
    private final MessageChatMemoryAdvisor memoryAdvisor;
    private final org.springframework.ai.chat.memory.ChatMemory chatMemory; // Direct access for history retrieval

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
            @Qualifier("supportBotMemoryAdvisor") MessageChatMemoryAdvisor memoryAdvisor,
            @Qualifier("supportBotChatMemory") org.springframework.ai.chat.memory.ChatMemory chatMemory) {

        this.chatClient = chatClientBuilder.build();
        this.knowledgeBaseService = knowledgeBaseService;
        this.safetyAdvisor = safetyAdvisor;
        this.customerContextAdvisor = customerContextAdvisor;
        this.sentimentAdvisor = sentimentAdvisor;
        this.ticketTools = ticketTools;
        this.formattingAdvisor = formattingAdvisor;
        this.memoryAdvisor = memoryAdvisor;
        this.chatMemory = chatMemory;
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
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPromptResource);
        String enhancedSystemPrompt = systemPromptTemplate.createMessage(java.util.Map.of(
                "category", category,
                "context", context)).getText();

        // Set customer context for advisors
        Long customerId = parseCustomerId(request.customerId());
        if (customerId != null) {
            customerContextAdvisor.setCustomerId(customerId);
        }

        // Determine conversation ID for memory
        // Determine conversation ID for memory
        // PRIORITIZE Customer ID for persistence across sessions
        String conversationId = (request.customerId() != null && !request.customerId().isEmpty())
                ? "customer-" + request.customerId()
                : (request.sessionId() != null ? request.sessionId() : "anonymous");

        logger.info("Chat Request - CustomerID: {}, SessionID: {}, Generated ConversationID: {}",
                request.customerId(), request.sessionId(), conversationId);

        // Execute chat with full advisor chain + TOOLS
        String content = chatClient.prompt()
                .system(enhancedSystemPrompt)
                .user(request.message())
                // Ensure ID is set BEFORE advisors run
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                // Advisor chain
                .advisors(safetyAdvisor) // 1. Safety check
                .advisors(customerContextAdvisor) // 2. Customer personalization
                .advisors(sentimentAdvisor) // 3. Sentiment analysis
                .advisors(memoryAdvisor) // 4. Chat memory
                .advisors(formattingAdvisor) // 5. Response formatting
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
     * STREAMING chat handler (Level 4.5).
     * Returns a Flux<Map<String, String>> for real-time token streaming.
     */
    public reactor.core.publisher.Flux<java.util.Map<String, String>> streamChat(ChatRequest request) {
        // Get RAG context from knowledge base
        String context = knowledgeBaseService.getContextForQuery(request.message(), 3);
        String category = knowledgeBaseService.categorizeQuery(request.message());

        // Build enhanced system prompt
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPromptResource);
        String enhancedSystemPrompt = systemPromptTemplate.createMessage(java.util.Map.of(
                "category", category,
                "context", context)).getText();

        // Set context
        Long customerId = parseCustomerId(request.customerId());
        if (customerId != null) {
            customerContextAdvisor.setCustomerId(customerId);
        }
        String conversationId = (request.customerId() != null && !request.customerId().isEmpty())
                ? "customer-" + request.customerId()
                : (request.sessionId() != null ? request.sessionId() : "anonymous");

        // 1. Analyze Sentiment immediately (Stateless)
        SentimentType sentiment = sentimentAdvisor.analyzeSentiment(request.message());

        // 2. Create Sentiment Event Flux
        reactor.core.publisher.Flux<java.util.Map<String, String>> sentimentFlux = reactor.core.publisher.Flux
                .just(java.util.Collections.singletonMap("sentiment", sentiment.name()));

        // 3. Create Chat Stream Flux
        reactor.core.publisher.Flux<java.util.Map<String, String>> chatFlux = chatClient.prompt()
                .system(enhancedSystemPrompt)
                .user(request.message())
                // Ensure ID is set BEFORE advisors run
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                .advisors(safetyAdvisor)
                .advisors(customerContextAdvisor)
                .advisors(memoryAdvisor)
                .tools(ticketTools) // Enable Tools!
                .stream()
                .content()
                .map(content -> java.util.Collections.singletonMap("content", content));

        // 4. Concat: Sentiment first, then content
        return reactor.core.publisher.Flux.concat(sentimentFlux, chatFlux);
    }

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
     * Retrieves chat history for a given customer or session.
     */
    public java.util.List<org.springframework.ai.chat.messages.Message> getChatHistory(String customerId,
            String sessionId) {
        String conversationId = (customerId != null && !customerId.isEmpty())
                ? "customer-" + customerId
                : (sessionId != null ? sessionId : "anonymous");

        java.util.List<org.springframework.ai.chat.messages.Message> history = chatMemory.get(conversationId);
        logger.info("Get History - CustomerID: {}, SessionID: {}, conversationId: {}, History Size: {}",
                customerId, sessionId, conversationId, history.size());

        return history; // Retrieve messages
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
