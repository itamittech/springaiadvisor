package com.example.advisor.supportbot.model.entity;

import com.example.advisor.supportbot.model.enums.SentimentType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a conversation session between a customer and the support bot.
 * Links chat memory to customers and optionally to tickets.
 */
@Entity
@Table(name = "conversation_sessions")
public class ConversationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false, unique = true)
    private String conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_sentiment")
    private SentimentType lastSentiment = SentimentType.NEUTRAL;

    @Column(name = "message_count")
    private int messageCount = 0;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "is_active")
    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        lastMessageAt = LocalDateTime.now();
    }

    // Constructors
    public ConversationSession() {
    }

    public ConversationSession(String conversationId, Customer customer) {
        this.conversationId = conversationId;
        this.customer = customer;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public SentimentType getLastSentiment() {
        return lastSentiment;
    }

    public void setLastSentiment(SentimentType lastSentiment) {
        this.lastSentiment = lastSentiment;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Records a new message in this session.
     */
    public void recordMessage(SentimentType sentiment) {
        this.messageCount++;
        this.lastMessageAt = LocalDateTime.now();
        this.lastSentiment = sentiment;
    }

    /**
     * Ends this conversation session.
     */
    public void endSession() {
        this.active = false;
        this.endedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "ConversationSession{" +
                "id=" + id +
                ", conversationId='" + conversationId + '\'' +
                ", messageCount=" + messageCount +
                ", active=" + active +
                '}';
    }
}
