package com.example.advisor.supportbot.repository;

import com.example.advisor.supportbot.model.entity.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ConversationSession entity operations.
 */
@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {

    /**
     * Find a session by its unique conversation ID.
     */
    Optional<ConversationSession> findByConversationId(String conversationId);

    /**
     * Find all sessions for a customer.
     */
    List<ConversationSession> findByCustomerId(Long customerId);

    /**
     * Find active sessions for a customer.
     */
    List<ConversationSession> findByCustomerIdAndActiveTrue(Long customerId);

    /**
     * Find the most recent active session for a customer.
     */
    Optional<ConversationSession> findFirstByCustomerIdAndActiveTrueOrderByLastMessageAtDesc(Long customerId);

    /**
     * Check if a conversation ID exists.
     */
    boolean existsByConversationId(String conversationId);
}
