package com.example.advisor.supportbot.tool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.example.advisor.supportbot.model.entity.Customer;
import com.example.advisor.supportbot.model.entity.Ticket;
import com.example.advisor.supportbot.repository.CustomerRepository;
import com.example.advisor.supportbot.repository.TicketRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit Test for TicketTools.
 * Verifies validation logic and ticket creation without DB connection.
 */
public class TicketToolsTest {

    @Test
    public void testCreateTicket_Success() {
        // Setup Mocks
        TicketRepository ticketRepository = mock(TicketRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        TicketTools tools = new TicketTools(ticketRepository, customerRepository);

        Customer mockCustomer = new Customer();
        mockCustomer.setId(1L);

        Ticket mockSavedTicket = new Ticket();
        mockSavedTicket.setId(100L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(mockSavedTicket);

        // Execute
        String result = tools.createTicket(1L, "Broken Laptop", "Screen cracked", "HIGH");

        // Assert
        Assertions.assertTrue(result.contains("Ticket ID: 100"));
        Mockito.verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    public void testCreateTicket_InvalidPriority() {
        TicketRepository ticketRepository = mock(TicketRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        TicketTools tools = new TicketTools(ticketRepository, customerRepository);

        String result = tools.createTicket(1L, "Subject", "Desc", "INVALID_PRIORITY");

        Assertions.assertTrue(result.contains("Error: Invalid priority level"));
    }

    @Test
    public void testCreateTicket_MissingSubject() {
        TicketRepository ticketRepository = mock(TicketRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        TicketTools tools = new TicketTools(ticketRepository, customerRepository);

        String result = tools.createTicket(1L, "", "Desc", "HIGH");

        Assertions.assertTrue(result.contains("Error: Ticket subject cannot be empty"));
    }
}
