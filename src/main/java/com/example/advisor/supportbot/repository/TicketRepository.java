package com.example.advisor.supportbot.repository;

import com.example.advisor.supportbot.model.entity.Ticket;
import com.example.advisor.supportbot.model.enums.TicketPriority;
import com.example.advisor.supportbot.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Ticket entity operations.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Find all tickets for a specific customer.
     */
    List<Ticket> findByCustomerId(Long customerId);

    /**
     * Find all tickets with a specific status.
     */
    List<Ticket> findByStatus(TicketStatus status);

    /**
     * Find all tickets with a specific priority.
     */
    List<Ticket> findByPriority(TicketPriority priority);

    /**
     * Find all active (non-closed, non-resolved) tickets for a customer.
     */
    @Query("SELECT t FROM Ticket t WHERE t.customer.id = :customerId AND t.status NOT IN ('CLOSED', 'RESOLVED')")
    List<Ticket> findActiveTicketsByCustomerId(@Param("customerId") Long customerId);

    /**
     * Find escalated tickets that need attention.
     */
    List<Ticket> findByEscalatedTrueAndStatusNot(TicketStatus status);

    /**
     * Find ticket by conversation ID.
     */
    List<Ticket> findByConversationId(String conversationId);

    /**
     * Count open tickets for a customer.
     */
    long countByCustomerIdAndStatus(Long customerId, TicketStatus status);
}
