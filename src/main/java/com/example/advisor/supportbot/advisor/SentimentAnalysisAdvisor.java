package com.example.advisor.supportbot.advisor;

import com.example.advisor.supportbot.model.enums.SentimentType;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Advisor that analyzes customer message sentiment.
 * This helps identify frustrated customers who may need priority attention.
 * 
 * This advisor runs BEFORE the LLM call to analyze sentiment and log it.
 */
public class SentimentAnalysisAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String ADVISOR_NAME = "SentimentAnalysisAdvisor";

    private SentimentType lastDetectedSentiment = SentimentType.NEUTRAL;

    // Positive sentiment indicators
    private static final List<String> POSITIVE_WORDS = List.of(
            "thanks", "thank you", "great", "awesome", "excellent", "perfect",
            "love", "amazing", "helpful", "appreciate", "wonderful", "fantastic");

    // Frustrated sentiment indicators
    private static final List<String> FRUSTRATED_WORDS = List.of(
            "frustrated", "annoying", "disappointed", "unhappy", "waste of time",
            "not happy", "terrible", "awful", "ridiculous", "unacceptable");

    // Angry sentiment indicators
    private static final List<String> ANGRY_WORDS = List.of(
            "angry", "furious", "outraged", "hate", "worst", "horrible",
            "disgusting", "fed up", "sick of", "never again", "demand");

    @Override
    public String getName() {
        return ADVISOR_NAME;
    }

    @Override
    public int getOrder() {
        return 20; // Run after customer context advisor
    }

    /**
     * Gets the last detected sentiment.
     */
    public SentimentType getLastSentiment() {
        return lastDetectedSentiment;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String userMessage = extractUserMessage(request).toLowerCase();

        // Analyze sentiment
        SentimentType sentiment = analyzeSentiment(userMessage);
        lastDetectedSentiment = sentiment;

        // Log sentiment
        logSentiment(sentiment);

        return chain.nextCall(request);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        String userMessage = extractUserMessage(request).toLowerCase();
        SentimentType sentiment = analyzeSentiment(userMessage);
        lastDetectedSentiment = sentiment;
        logSentiment(sentiment);

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
     * Analyzes the sentiment of the user message.
     */
    private SentimentType analyzeSentiment(String message) {
        // Check for angry sentiment first (highest priority)
        if (containsAny(message, ANGRY_WORDS)) {
            return SentimentType.ANGRY;
        }

        // Check for frustrated sentiment
        if (containsAny(message, FRUSTRATED_WORDS)) {
            return SentimentType.FRUSTRATED;
        }

        // Check for positive sentiment
        if (containsAny(message, POSITIVE_WORDS)) {
            return SentimentType.POSITIVE;
        }

        // Default to neutral
        return SentimentType.NEUTRAL;
    }

    /**
     * Logs sentiment for analytics purposes.
     */
    private void logSentiment(SentimentType sentiment) {
        String icon = sentiment.requiresAttention() ? "⚠️" : "📊";
        System.out.println(icon + " Sentiment Analysis: " + sentiment.getEmoji() + " " + sentiment.getDisplayName());
    }

    /**
     * Checks if text contains any of the given words.
     */
    private boolean containsAny(String text, List<String> words) {
        return words.stream().anyMatch(word -> text.contains(word.toLowerCase()));
    }
}
