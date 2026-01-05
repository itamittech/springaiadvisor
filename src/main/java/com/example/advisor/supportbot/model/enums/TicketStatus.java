package com.example.advisor.supportbot.model.enums;

/**
 * Represents the lifecycle status of a support ticket.
 */
public enum TicketStatus {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    WAITING_CUSTOMER("Waiting on Customer"),
    RESOLVED("Resolved"),
    CLOSED("Closed");

    private final String displayName;

    TicketStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
