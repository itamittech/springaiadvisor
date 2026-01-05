package com.example.advisor.supportbot.tool;

import com.example.advisor.supportbot.model.entity.Customer;
import com.example.advisor.supportbot.model.entity.Ticket;
import com.example.advisor.supportbot.model.enums.TicketPriority;
import com.example.advisor.supportbot.model.enums.TicketStatus;
import com.example.advisor.supportbot.repository.CustomerRepository;
import com.example.advisor.supportbot.repository.TicketRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 🛠️ TICKET TOOLS (Agentic AI)
 * 
 * @learning PATTERN: FUNCTION CALLING (AGENTS)
 *           Instead of observing the chat and guessing when to create a ticket
 *           (Advisor pattern),
 *           we give the LLM this "Tool" and let it *decide* when to call it.
 * 
 *           WHY IS THIS BETTER?
 *           1. **Reasoning**: The AI can decide if a refund is actually
 *           warranted by policy before creating the ticket.
 *           2. **Data Extraction**: The AI natively extracts parameters
 *           (reason, priority) without regex.
 */
@Component
public class TicketTools {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;

    public TicketTools(TicketRepository ticketRepository, CustomerRepository customerRepository) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
    }

    @Tool(description = "Create a new support ticket for a customer. Use this when the user asks for help that requires human intervention, like refunds or technical bugs.")
    public String createTicket(
            @ToolParam(description = "The ID of the customer (available in context)") Long customerId,
            @ToolParam(description = "Summary of the issue") String subject,
            @ToolParam(description = "Detailed description including specific error messages or requests") String description,
            @ToolParam(description = "Priority level: LOW, MEDIUM, HIGH, or CRITICAL") String priority) {

        System.out.println("🤖 Tool Call: createTicket for Customer " + customerId);

        return customerRepository.findById(customerId).map(customer -> {
            Ticket ticket = new Ticket();
            ticket.setCustomer(customer);
            ticket.setSubject(subject);
            ticket.setDescription(description);
            ticket.setStatus(TicketStatus.OPEN);
            try {
                ticket.setPriority(TicketPriority.valueOf(priority.toUpperCase()));
            } catch (Exception e) {
                ticket.setPriority(TicketPriority.MEDIUM);
            }
            ticket.setCategory("agent-created");
            ticket.setCreatedAt(LocalDateTime.now());

            Ticket saved = ticketRepository.save(ticket);
            return "✅ Ticket created successfully! Ticket ID: " + saved.getId();
        }).orElse("❌ Error: Customer not found with ID " + customerId);
    }
}
