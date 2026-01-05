package com.example.advisor.supportbot.exception;

/**
 * Exception thrown when a ticket is not found.
 */
public class TicketNotFoundException extends RuntimeException {

    private final Long ticketId;

    public TicketNotFoundException(Long id) {
        super("Ticket not found with id: " + id);
        this.ticketId = id;
    }

    public Long getTicketId() {
        return ticketId;
    }
}
