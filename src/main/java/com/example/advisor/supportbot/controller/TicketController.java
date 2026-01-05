package com.example.advisor.supportbot.controller;

import com.example.advisor.supportbot.model.dto.TicketDTO;
import com.example.advisor.supportbot.model.enums.TicketPriority;
import com.example.advisor.supportbot.model.enums.TicketStatus;
import com.example.advisor.supportbot.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing support tickets.
 */
@RestController
@RequestMapping("/support/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Create a new ticket.
     * 
     * POST /support/tickets
     */
    @PostMapping
    public ResponseEntity<TicketDTO> createTicket(
            @RequestParam Long customerId,
            @RequestParam String subject,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "MEDIUM") TicketPriority priority,
            @RequestParam(required = false) String category) {

        TicketDTO ticket = ticketService.createTicket(customerId, subject, description, priority, category);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Get ticket by ID.
     * 
     * GET /support/tickets/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketDTO> getTicket(@PathVariable Long id) {
        TicketDTO ticket = ticketService.getTicket(id);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Get all tickets for a customer.
     * 
     * GET /support/tickets/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TicketDTO>> getTicketsForCustomer(@PathVariable Long customerId) {
        List<TicketDTO> tickets = ticketService.getTicketsForCustomer(customerId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get active tickets for a customer.
     * 
     * GET /support/tickets/customer/{customerId}/active
     */
    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<List<TicketDTO>> getActiveTicketsForCustomer(@PathVariable Long customerId) {
        List<TicketDTO> tickets = ticketService.getActiveTicketsForCustomer(customerId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Update ticket status.
     * 
     * PATCH /support/tickets/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam TicketStatus status) {

        TicketDTO ticket = ticketService.updateStatus(id, status);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Update ticket priority.
     * 
     * PATCH /support/tickets/{id}/priority
     */
    @PatchMapping("/{id}/priority")
    public ResponseEntity<TicketDTO> updatePriority(
            @PathVariable Long id,
            @RequestParam TicketPriority priority) {

        TicketDTO ticket = ticketService.updatePriority(id, priority);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Close a ticket.
     * 
     * POST /support/tickets/{id}/close
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<TicketDTO> closeTicket(@PathVariable Long id) {
        TicketDTO ticket = ticketService.closeTicket(id);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Get all escalated tickets.
     * 
     * GET /support/tickets/escalated
     */
    @GetMapping("/escalated")
    public ResponseEntity<List<TicketDTO>> getEscalatedTickets() {
        List<TicketDTO> tickets = ticketService.getEscalatedTickets();
        return ResponseEntity.ok(tickets);
    }
}
