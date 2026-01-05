package com.example.advisor.supportbot.advisor;

import com.example.advisor.supportbot.model.entity.Customer;
import com.example.advisor.supportbot.model.entity.Ticket;
import com.example.advisor.supportbot.model.enums.TicketPriority;
import com.example.advisor.supportbot.model.enums.TicketStatus;
import com.example.advisor.supportbot.repository.CustomerRepository;
import com.example.advisor.supportbot.repository.TicketRepository;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

/**
 * 🎫 TICKET ESCALATION ADVISOR (Order: 30)
 * 
 * @learning PATTERN: SIDE-EFFECT / ACTION TRIGGER
 *           This advisor demonstrates how to trigger external actions (Database
 *           Writes) based on conversation triggers.
 * 
 *           WHY IS THIS IMPORTANT?
 *           1. **Automation**: Converts "talk" into "action" (creating a
 *           ticket).
 *           2. **Transparency**: The LLM needs to know that a ticket *was*
 *           created so it can tell the user.
 * 
 *           NOTE ON MODERN AI:
 *           Currently, this uses **Regex/Keyword Matching** (Rule-Based).
 *           In **Level 4 (Agents)**, we will refactor this to use **Function
 *           Calling (@Tool)**,
 *           where the LLM *decides* to call `createTicket()` itself.
 * 
 *           IMPLEMENTATION DETAILS:
 *           - Scans message for trigger words ("refund", "manager").
 *           - If triggered, calls `ticketRepository.save()`.
 *           - Injects a "System Message" into the prompt telling the LLM: "A
 *           ticket has been created... inform the user."
 */
public class TicketEscalationAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String ADVISOR_NAME = "TicketEscalationAdvisor";

    // Escalation trigger phrases
    private static final List<String> ESCALATION_PHRASES = List.of(
            "speak to human", "talk to human", "human agent", "real person",
            "speak to someone", "talk to someone", "speak with someone",
            "manager", "supervisor", "escalate", "cancel my subscription",
            "cancel account", "want a refund", "need a refund", "get my money back",
            "close my account", "delete my account", "legal action", "lawyer");

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;

    private Long currentCustomerId = null;
    private Ticket lastCreatedTicket = null;

    public TicketEscalationAdvisor(TicketRepository ticketRepository,
            CustomerRepository customerRepository) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Sets the customer ID for ticket creation.
     */
    public void setCustomerId(Long customerId) {
        this.currentCustomerId = customerId;
    }

    /**
     * Gets the last created ticket.
     */
    public Ticket getLastCreatedTicket() {
        return lastCreatedTicket;
    }

    @Override
    public String getName() {
        return ADVISOR_NAME;
    }

    @Override
    public int getOrder() {
        return 30; // Run after sentiment analysis
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String userMessage = extractUserMessage(request).toLowerCase();
        lastCreatedTicket = null;

        // Check for escalation triggers
        Optional<String> escalationTrigger = detectEscalationTrigger(userMessage);

        if (escalationTrigger.isPresent() && currentCustomerId != null) {
            lastCreatedTicket = createEscalationTicket(currentCustomerId, escalationTrigger.get(), userMessage);
        }

        return chain.nextCall(request);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        String userMessage = extractUserMessage(request).toLowerCase();
        lastCreatedTicket = null;

        Optional<String> escalationTrigger = detectEscalationTrigger(userMessage);
        if (escalationTrigger.isPresent() && currentCustomerId != null) {
            lastCreatedTicket = createEscalationTicket(currentCustomerId, escalationTrigger.get(), userMessage);
        }

        return chain.nextStream(request);
    }

    /**
     * Extracts the user message from the request.
     */
    private String extractUserMessage(ChatClientRequest request) {
        if (request.prompt() != null && request.prompt().getContents() != null) {
            return request.prompt().getContents();
        }
        return "";
    }

    /**
     * Detects if the message contains an escalation trigger.
     */
    private Optional<String> detectEscalationTrigger(String message) {
        return ESCALATION_PHRASES.stream()
                .filter(phrase -> message.contains(phrase.toLowerCase()))
                .findFirst();
    }

    /**
     * Creates a high-priority escalation ticket.
     */
    private Ticket createEscalationTicket(Long customerId, String trigger, String originalMessage) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            return null;
        }

        Customer customer = customerOpt.get();

        // Determine priority based on trigger
        TicketPriority priority = determinePriority(trigger);
        String category = categorizeEscalation(trigger);

        Ticket ticket = new Ticket();
        ticket.setCustomer(customer);
        ticket.setSubject("Escalation Request: " + trigger);
        ticket.setDescription("Customer requested escalation.\n\n" +
                "Trigger phrase: \"" + trigger + "\"\n\n" +
                "Original message: " + originalMessage);
        ticket.setPriority(priority);
        ticket.setCategory(category);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setEscalated(true);

        Ticket savedTicket = ticketRepository.save(ticket);

        System.out.println("🎫 Escalation Ticket Created - ID: " + savedTicket.getId() +
                ", Customer: " + customer.getName() +
                ", Priority: " + priority +
                ", Trigger: " + trigger);

        return savedTicket;
    }

    /**
     * Determines ticket priority based on escalation trigger.
     */
    private TicketPriority determinePriority(String trigger) {
        if (trigger.contains("legal") || trigger.contains("lawyer")) {
            return TicketPriority.CRITICAL;
        }
        if (trigger.contains("refund") || trigger.contains("cancel")) {
            return TicketPriority.HIGH;
        }
        if (trigger.contains("manager") || trigger.contains("supervisor")) {
            return TicketPriority.HIGH;
        }
        return TicketPriority.MEDIUM;
    }

    /**
     * Categorizes the escalation based on trigger.
     */
    private String categorizeEscalation(String trigger) {
        if (trigger.contains("refund") || trigger.contains("cancel") || trigger.contains("money")) {
            return "billing";
        }
        if (trigger.contains("legal") || trigger.contains("lawyer")) {
            return "legal";
        }
        return "support";
    }
}
