package com.example.advisor.supportbot.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Safety advisor that filters inappropriate content and prevents
 * disclosure of sensitive internal information.
 * 
 * This advisor runs BEFORE the LLM call to filter user input,
 * and can block requests containing prohibited content.
 */
public class SupportSafetyAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String ADVISOR_NAME = "SupportSafetyAdvisor";

    // Words that should trigger content filtering
    private static final List<String> BLOCKED_WORDS = List.of(
            "competitor", "hack", "exploit", "lawsuit", "internal",
            "confidential", "admin access");

    // Phrases indicating abusive behavior
    private static final List<String> ABUSIVE_PHRASES = List.of(
            "you idiot", "stupid bot", "worthless", "useless piece");

    private static final String BLOCKED_RESPONSE = "I'm here to help with TaskFlow-related questions. " +
            "If you have a specific issue or question about our product, " +
            "I'd be happy to assist you. For sensitive matters, please " +
            "contact our support team directly at support@taskflow.com.";

    private static final String ABUSIVE_RESPONSE = "I understand you may be frustrated, and I genuinely want to help. "
            +
            "However, I'm not able to continue if the conversation becomes disrespectful. " +
            "Let's try again - what specific issue can I help you with today?";

    @Override
    public String getName() {
        return ADVISOR_NAME;
    }

    @Override
    public int getOrder() {
        return 0; // Run first in the chain
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String userMessage = extractUserMessage(request).toLowerCase();

        // Check for abusive language
        if (containsAbusiveLanguage(userMessage)) {
            return createBlockedResponse(ABUSIVE_RESPONSE);
        }

        // Check for blocked content
        if (containsBlockedContent(userMessage)) {
            return createBlockedResponse(BLOCKED_RESPONSE);
        }

        // Continue with the chain
        return chain.nextCall(request);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        String userMessage = extractUserMessage(request).toLowerCase();

        if (containsAbusiveLanguage(userMessage) || containsBlockedContent(userMessage)) {
            String response = containsAbusiveLanguage(userMessage) ? ABUSIVE_RESPONSE : BLOCKED_RESPONSE;
            return Flux.just(createBlockedResponse(response));
        }

        return chain.nextStream(request);
    }

    /**
     * Extracts the user message from the request.
     */
    private String extractUserMessage(ChatClientRequest request) {
        if (request.prompt() != null && request.prompt().getContents() != null) {
            return request.prompt().getContents();
        }
        return "";
    }

    /**
     * Checks if the message contains abusive language.
     */
    private boolean containsAbusiveLanguage(String message) {
        return ABUSIVE_PHRASES.stream()
                .anyMatch(phrase -> message.contains(phrase.toLowerCase()));
    }

    /**
     * Checks if the message contains blocked content.
     */
    private boolean containsBlockedContent(String message) {
        return BLOCKED_WORDS.stream()
                .anyMatch(word -> message.contains(word.toLowerCase()));
    }

    /**
     * Creates a blocked response without calling the LLM.
     */
    private ChatClientResponse createBlockedResponse(String message) {
        Generation generation = new Generation(new org.springframework.ai.chat.messages.AssistantMessage(message));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        return ChatClientResponse.builder()
                .chatResponse(chatResponse)
                .build();
    }
}
