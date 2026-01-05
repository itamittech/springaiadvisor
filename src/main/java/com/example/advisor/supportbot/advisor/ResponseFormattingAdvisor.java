package com.example.advisor.supportbot.advisor;

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
 * Advisor that ensures responses are professionally formatted.
 * This advisor runs AFTER the LLM call to log the response.
 * 
 * Note: In Spring AI 1.1.2, we cannot modify the response after the LLM call,
 * so this advisor mainly logs the response for analytics.
 */
public class ResponseFormattingAdvisor implements CallAdvisor, StreamAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(ResponseFormattingAdvisor.class);
    private static final String ADVISOR_NAME = "ResponseFormattingAdvisor";

    @Override
    public String getName() {
        return ADVISOR_NAME;
    }

    @Override
    public int getOrder() {
        return 1000; // Run last in the chain
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // Get the response from the chain
        ChatClientResponse response = chain.nextCall(request);

        // Log the response for analytics
        logResponse(response);

        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        return chain.nextStream(request)
                .doOnNext(this::logResponse);
    }

    /**
     * Logs the response for analytics.
     */
    private void logResponse(ChatClientResponse response) {
        if (response.chatResponse() != null &&
                response.chatResponse().getResult() != null &&
                response.chatResponse().getResult().getOutput() != null) {

            String content = response.chatResponse().getResult().getOutput().getText();
            int length = content != null ? content.length() : 0;

            logger.info("📤 Response Formatted - Length: {} chars", length);
            System.out.println("📤 Response sent - " + length + " characters");
        }
    }
}
