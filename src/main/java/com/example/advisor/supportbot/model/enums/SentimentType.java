package com.example.advisor.supportbot.model.enums;

/**
 * Represents the detected sentiment from customer messages.
 * Used by the SentimentAnalysisAdvisor for customer experience tracking.
 */
public enum SentimentType {
    POSITIVE("Positive", "😊"),
    NEUTRAL("Neutral", "😐"),
    FRUSTRATED("Frustrated", "😤"),
    ANGRY("Angry", "😠");

    private final String displayName;
    private final String emoji;

    SentimentType(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    /**
     * Returns true if this sentiment requires priority attention.
     */
    public boolean requiresAttention() {
        return this == FRUSTRATED || this == ANGRY;
    }
}
