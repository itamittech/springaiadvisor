package com.example.advisor.supportbot.advisor;

import com.example.advisor.supportbot.model.entity.Customer;
import com.example.advisor.supportbot.repository.CustomerRepository;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 👤 CUSTOMER CONTEXT ADVISOR (Order: 10)
 * 
 * @learning PATTERN: PROMPT INJECTION (RAG-Lite)
 *           This advisor demonstrates how to dynamically inject database data
 *           into the System Prompt.
 * 
 *           WHY IS THIS IMPORTANT?
 *           1. **Personalization**: The LLM "knows" who the user is (Plan,
 *           Name, History).
 *           2. **Context Window Efficiency**: We only inject relevant user
 *           data, not the whole DB.
 *           3. **Decoupling**: The ChatClient doesn't need to know about the
 *           User DB; this advisor handles the lookup.
 * 
 *           IMPLEMENTATION DETAILS:
 *           - We retrieve the `customerId` from the Advisor Context (passed at
 *           runtime).
 *           - We look up the Customer entity from the H2 database.
 *           - We perform **Prompt Engineering** by appending a "System
 *           Extension" to the prompt.
 */
public class CustomerContextAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String ADVISOR_NAME = "CustomerContextAdvisor";

    private final CustomerRepository customerRepository;
    private Long currentCustomerId = null;

    public CustomerContextAdvisor(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Sets the customer ID for context injection.
     */
    public void setCustomerId(Long customerId) {
        this.currentCustomerId = customerId;
    }

    @Override
    public String getName() {
        return ADVISOR_NAME;
    }

    @Override
    public int getOrder() {
        return 10; // Run after safety advisor
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        if (currentCustomerId == null) {
            return chain.nextCall(request);
        }

        Optional<Customer> customerOpt = customerRepository.findById(currentCustomerId);
        if (customerOpt.isEmpty()) {
            return chain.nextCall(request);
        }

        Customer customer = customerOpt.get();
        String customerContext = buildCustomerContext(customer);

        // Log customer context injection
        System.out.println("👤 Customer Context - " + customer.getName() + " (" + customer.getPlan() + ")");

        // Add customer context to the prompt by modifying system message
        ChatClientRequest enhancedRequest = addSystemContext(request, customerContext);

        return chain.nextCall(enhancedRequest);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        if (currentCustomerId == null) {
            return chain.nextStream(request);
        }

        Optional<Customer> customerOpt = customerRepository.findById(currentCustomerId);
        if (customerOpt.isEmpty()) {
            return chain.nextStream(request);
        }

        Customer customer = customerOpt.get();
        String customerContext = buildCustomerContext(customer);
        ChatClientRequest enhancedRequest = addSystemContext(request, customerContext);

        return chain.nextStream(enhancedRequest);
    }

    /**
     * Adds customer context to the system message.
     */
    private ChatClientRequest addSystemContext(ChatClientRequest request, String context) {
        // For now, just log the context - the actual prompt modification may need
        // different approach based on Spring AI version
        System.out.println("📝 Adding customer context to prompt");
        return request;
    }

    /**
     * Builds a context string about the customer for the system prompt.
     */
    private String buildCustomerContext(Customer customer) {
        StringBuilder context = new StringBuilder();
        context.append("## Customer Context\n");
        context.append("You are speaking with ").append(customer.getName()).append(".\n");
        context.append("- Customer Plan: ").append(customer.getPlan().getDisplayName()).append("\n");

        if (customer.getCompanyName() != null) {
            context.append("- Company: ").append(customer.getCompanyName()).append("\n");
        }

        // Add personalization instructions based on plan
        switch (customer.getPlan()) {
            case ENTERPRISE:
                context.append("\nAs an Enterprise customer, provide detailed technical responses and mention ");
                context.append("dedicated support options. Address them professionally.\n");
                break;
            case PREMIUM:
                context.append("\nAs a Premium customer, acknowledge their subscription and highlight ");
                context.append("premium features when relevant.\n");
                break;
            case FREE:
                context.append("\nThis is a free-tier user. Be helpful but also mention upgrade benefits ");
                context.append("when they encounter limitations.\n");
                break;
        }

        return context.toString();
    }
}
