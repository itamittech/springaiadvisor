package com.example.advisor.supportbot.model.enums;

/**
 * Represents the subscription plan level for a customer.
 * Different plans may receive different levels of support.
 */
public enum CustomerPlan {
    FREE("Free", 0),
    PREMIUM("Premium", 1),
    ENTERPRISE("Enterprise", 2);

    private final String displayName;
    private final int priorityLevel;

    CustomerPlan(String displayName, int priorityLevel) {
        this.displayName = displayName;
        this.priorityLevel = priorityLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPriorityLevel() {
        return priorityLevel;
    }
}
