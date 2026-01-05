package com.example.advisor.supportbot.model.enums;

/**
 * Represents the priority level of a support ticket.
 * Higher priority tickets are addressed more urgently.
 */
public enum TicketPriority {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4);

    private final String displayName;
    private final int urgencyLevel;

    TicketPriority(String displayName, int urgencyLevel) {
        this.displayName = displayName;
        this.urgencyLevel = urgencyLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getUrgencyLevel() {
        return urgencyLevel;
    }
}
