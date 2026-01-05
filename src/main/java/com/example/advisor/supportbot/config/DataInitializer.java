package com.example.advisor.supportbot.config;

import com.example.advisor.supportbot.model.entity.Customer;
import com.example.advisor.supportbot.model.entity.Ticket;
import com.example.advisor.supportbot.model.enums.CustomerPlan;
import com.example.advisor.supportbot.model.enums.TicketPriority;
import com.example.advisor.supportbot.model.enums.TicketStatus;
import com.example.advisor.supportbot.repository.CustomerRepository;
import com.example.advisor.supportbot.repository.TicketRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Initializes sample data for the Support Bot demo.
 */
@Component
public class DataInitializer {

    private final CustomerRepository customerRepository;
    private final TicketRepository ticketRepository;

    public DataInitializer(CustomerRepository customerRepository,
            TicketRepository ticketRepository) {
        this.customerRepository = customerRepository;
        this.ticketRepository = ticketRepository;
    }

    @PostConstruct
    public void initData() {
        // Only initialize if database is empty
        if (customerRepository.count() > 0) {
            return;
        }

        System.out.println("📊 Initializing Support Bot sample data...");

        // Create sample customers
        Customer john = new Customer("John Smith", "john@acme.com", CustomerPlan.PREMIUM);
        john.setCompanyName("Acme Corp");
        john = customerRepository.save(john);

        Customer sarah = new Customer("Sarah Johnson", "sarah@techstart.io", CustomerPlan.ENTERPRISE);
        sarah.setCompanyName("TechStart");
        sarah = customerRepository.save(sarah);

        Customer mike = new Customer("Mike Brown", "mike.brown@gmail.com", CustomerPlan.FREE);
        mike = customerRepository.save(mike);

        Customer emily = new Customer("Emily Davis", "emily@designco.com", CustomerPlan.PREMIUM);
        emily.setCompanyName("Design Co");
        emily = customerRepository.save(emily);

        Customer alex = new Customer("Alex Wilson", "alex@startup.io", CustomerPlan.FREE);
        alex.setCompanyName("Startup Inc");
        customerRepository.save(alex);

        // Create sample tickets
        Ticket ticket1 = new Ticket();
        ticket1.setCustomer(john);
        ticket1.setSubject("Cannot access premium features");
        ticket1.setDescription("Upgraded yesterday but features not showing.");
        ticket1.setStatus(TicketStatus.OPEN);
        ticket1.setPriority(TicketPriority.HIGH);
        ticket1.setCategory("billing");
        ticketRepository.save(ticket1);

        Ticket ticket2 = new Ticket();
        ticket2.setCustomer(mike);
        ticket2.setSubject("Export question");
        ticket2.setDescription("How do I export to CSV?");
        ticket2.setStatus(TicketStatus.IN_PROGRESS);
        ticket2.setPriority(TicketPriority.MEDIUM);
        ticket2.setCategory("general");
        ticketRepository.save(ticket2);

        Ticket ticket3 = new Ticket();
        ticket3.setCustomer(sarah);
        ticket3.setSubject("SSO not working");
        ticket3.setDescription("Getting 403 error with SAML SSO.");
        ticket3.setStatus(TicketStatus.OPEN);
        ticket3.setPriority(TicketPriority.CRITICAL);
        ticket3.setCategory("technical");
        ticketRepository.save(ticket3);

        System.out.println("✅ Sample data initialized: " +
                customerRepository.count() + " customers, " +
                ticketRepository.count() + " tickets");
    }
}
