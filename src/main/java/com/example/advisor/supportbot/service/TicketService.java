package com.example.advisor.supportbot.service;

import com.example.advisor.supportbot.exception.CustomerNotFoundException;
import com.example.advisor.supportbot.exception.TicketNotFoundException;
import com.example.advisor.supportbot.model.dto.TicketDTO;
import com.example.advisor.supportbot.model.entity.Customer;
import com.example.advisor.supportbot.model.entity.Ticket;
import com.example.advisor.supportbot.model.enums.TicketPriority;
import com.example.advisor.supportbot.model.enums.TicketStatus;
import com.example.advisor.supportbot.repository.CustomerRepository;
import com.example.advisor.supportbot.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing support tickets.
 */
@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;

    public TicketService(TicketRepository ticketRepository, CustomerRepository customerRepository) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Creates a new support ticket.
     */
    public TicketDTO createTicket(Long customerId, String subject, String description,
            TicketPriority priority, String category) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        Ticket ticket = new Ticket();
        ticket.setCustomer(customer);
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setPriority(priority);
        ticket.setCategory(category);
        ticket.setStatus(TicketStatus.OPEN);

        Ticket savedTicket = ticketRepository.save(ticket);

        return toDTO(savedTicket);
    }

    /**
     * Gets a ticket by ID.
     */
    @Transactional(readOnly = true)
    public TicketDTO getTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        return toDTO(ticket);
    }

    /**
     * Gets all tickets for a customer.
     */
    @Transactional(readOnly = true)
    public List<TicketDTO> getTicketsForCustomer(Long customerId) {
        return ticketRepository.findByCustomerId(customerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets active (non-closed) tickets for a customer.
     */
    @Transactional(readOnly = true)
    public List<TicketDTO> getActiveTicketsForCustomer(Long customerId) {
        return ticketRepository.findActiveTicketsByCustomerId(customerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates ticket status.
     */
    public TicketDTO updateStatus(Long ticketId, TicketStatus newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        ticket.setStatus(newStatus);
        Ticket updatedTicket = ticketRepository.save(ticket);

        return toDTO(updatedTicket);
    }

    /**
     * Updates ticket priority.
     */
    public TicketDTO updatePriority(Long ticketId, TicketPriority newPriority) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        ticket.setPriority(newPriority);
        Ticket updatedTicket = ticketRepository.save(ticket);

        return toDTO(updatedTicket);
    }

    /**
     * Closes a ticket.
     */
    public TicketDTO closeTicket(Long ticketId) {
        return updateStatus(ticketId, TicketStatus.CLOSED);
    }

    /**
     * Gets all escalated tickets that need attention.
     */
    @Transactional(readOnly = true)
    public List<TicketDTO> getEscalatedTickets() {
        return ticketRepository.findByEscalatedTrueAndStatusNot(TicketStatus.CLOSED).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts a Ticket entity to DTO.
     */
    private TicketDTO toDTO(Ticket ticket) {
        return new TicketDTO(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory(),
                ticket.isEscalated(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }
}
