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
 * 📝 RESPONSE FORMATTING ADVISOR (Order: 1000)
 * 
 * @learning PATTERN: POST-PROCESSING & OBSERVABILITY
 *           This advisor runs *after* the LLM response to log metrics or format
 *           output.
 * 
 *           WHY IS THIS IMPORTANT?
 *           1. **Observability**: We can log token usage, latency, and response
 *           content for debugging.
 *           2. **Standardization**: Ensure the output format matches what the
 *           frontend expects (e.g., Markdown).
 * 
 *           IMPLEMENTATION DETAILS:
 *           - It has the highest Order (1000), meaning it wraps the *entire*
 *           inner chain.
 *           - It inspects the `ChatClientResponse` coming *back* from the
 *           chain.
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
