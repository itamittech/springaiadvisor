package com.example.advisor.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

/**
 * CustomLoggingAdvisor - A proper Spring AI CallAdvisor implementation.
 * 
 * This advisor intercepts ChatClient calls to:
 * 1. Log the incoming request
 * 2. Measure execution time
 * 3. Log token usage (Prompt, Completion, Total)
 */
public class CustomLoggingAdvisor implements CallAdvisor, StreamAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(CustomLoggingAdvisor.class);

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long startTime = System.currentTimeMillis();

        logger.info("CustomLoggingAdvisor - Request received");
        logger.info("Prompt: {}", request.prompt().getContents());

        // Proceed with the chain
        ChatClientResponse response = chain.nextCall(request);

        long duration = System.currentTimeMillis() - startTime;

        // Log response and token usage
        logger.info("CustomLoggingAdvisor - Response received");
        logger.info("Response: {}", response.chatResponse().getResult().getOutput().getText());
        logger.info("Prompt Tokens: {}", response.chatResponse().getMetadata().getUsage().getPromptTokens());
        logger.info("Completion Tokens: {}", response.chatResponse().getMetadata().getUsage().getCompletionTokens());
        logger.info("Total Tokens: {}", response.chatResponse().getMetadata().getUsage().getTotalTokens());
        logger.info("Duration: {}ms", duration);

        // Also print to console for immediate visibility
        System.out.println("Custom Advisor Log - Tokens: [Prompt: " +
                response.chatResponse().getMetadata().getUsage().getPromptTokens() +
                ", Gen: " + response.chatResponse().getMetadata().getUsage().getCompletionTokens() +
                ", Total: " + response.chatResponse().getMetadata().getUsage().getTotalTokens() +
                "], Duration: " + duration + "ms");

        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        logger.info("CustomLoggingAdvisor - Streaming request received");
        return chain.nextStream(request);
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public int getOrder() {
        return 0; // High priority
    }
}
