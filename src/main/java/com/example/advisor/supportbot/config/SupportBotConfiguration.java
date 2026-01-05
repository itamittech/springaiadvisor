package com.example.advisor.supportbot.config;

import com.example.advisor.supportbot.advisor.CustomerContextAdvisor;
import com.example.advisor.supportbot.advisor.ResponseFormattingAdvisor;
import com.example.advisor.supportbot.advisor.SentimentAnalysisAdvisor;
import com.example.advisor.supportbot.advisor.SupportSafetyAdvisor;
import com.example.advisor.supportbot.advisor.TicketEscalationAdvisor;
import com.example.advisor.supportbot.repository.CustomerRepository;
import com.example.advisor.supportbot.repository.TicketRepository;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Main configuration for the Support Bot.
 * Sets up advisors, memory, and other Spring AI components.
 */
@Configuration
public class SupportBotConfiguration {

    /**
     * Creates a chat memory specifically for support bot conversations.
     * Uses sliding window of 20 messages to optimize costs while maintaining
     * context.
     */
    @Bean
    @Qualifier("supportBotChatMemory")
    public ChatMemory supportBotChatMemory(JdbcChatMemoryRepository jdbcRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcRepository)
                .maxMessages(20) // Keep last 20 messages for cost optimization
                .build();
    }

    /**
     * Creates a MessageChatMemoryAdvisor for the support bot.
     */
    @Bean
    @Qualifier("supportBotMemoryAdvisor")
    public MessageChatMemoryAdvisor supportBotMemoryAdvisor(
            @Qualifier("supportBotChatMemory") ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    /**
     * Safety advisor for blocking inappropriate content.
     */
    @Bean
    public SupportSafetyAdvisor supportSafetyAdvisor() {
        return new SupportSafetyAdvisor();
    }

    /**
     * Customer context advisor for personalizing responses.
     */
    @Bean
    public CustomerContextAdvisor customerContextAdvisor(CustomerRepository customerRepository) {
        return new CustomerContextAdvisor(customerRepository);
    }

    /**
     * Sentiment analysis advisor for tracking customer mood.
     */
    @Bean
    public SentimentAnalysisAdvisor sentimentAnalysisAdvisor() {
        return new SentimentAnalysisAdvisor();
    }

    /**
     * Ticket escalation advisor for auto-creating tickets.
     */
    @Bean
    public TicketEscalationAdvisor ticketEscalationAdvisor(
            TicketRepository ticketRepository,
            CustomerRepository customerRepository) {
        return new TicketEscalationAdvisor(ticketRepository, customerRepository);
    }

    /**
     * Response formatting advisor for professional output.
     */
    @Bean
    public ResponseFormattingAdvisor responseFormattingAdvisor() {
        return new ResponseFormattingAdvisor();
    }
}
