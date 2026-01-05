package com.example.advisor.supportbot.model.dto;

import com.example.advisor.supportbot.model.enums.TicketPriority;
import com.example.advisor.supportbot.model.enums.TicketStatus;
import java.time.LocalDateTime;

/**
 * DTO for support ticket data transfer.
 */
public record TicketDTO(
        Long id,
        String subject,
        String description,
        TicketStatus status,
        TicketPriority priority,
        String category,
        boolean escalated,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    /**
     * Creates a TicketDTO for a new ticket creation request.
     */
    public static TicketDTO createRequest(String subject, String description, TicketPriority priority,
            String category) {
        return new TicketDTO(null, subject, description, TicketStatus.OPEN, priority, category, false, null, null);
    }
}
