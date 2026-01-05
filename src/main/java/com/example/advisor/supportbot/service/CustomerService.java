package com.example.advisor.supportbot.service;

import com.example.advisor.supportbot.exception.CustomerNotFoundException;
import com.example.advisor.supportbot.model.dto.CustomerDTO;
import com.example.advisor.supportbot.model.entity.Customer;
import com.example.advisor.supportbot.model.enums.CustomerPlan;
import com.example.advisor.supportbot.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing customers.
 */
@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Gets a customer by ID.
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        return toDTO(customer);
    }

    /**
     * Gets a customer by email.
     */
    @Transactional(readOnly = true)
    public Optional<CustomerDTO> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .map(this::toDTO);
    }

    /**
     * Creates a new customer.
     */
    public CustomerDTO createCustomer(String name, String email, CustomerPlan plan, String companyName) {
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Customer with email " + email + " already exists");
        }

        Customer customer = new Customer(name, email, plan);
        customer.setCompanyName(companyName);

        Customer savedCustomer = customerRepository.save(customer);
        return toDTO(savedCustomer);
    }

    /**
     * Updates customer's last active timestamp.
     */
    public void updateLastActive(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        customer.setLastActiveAt(LocalDateTime.now());
        customerRepository.save(customer);
    }

    /**
     * Upgrades customer plan.
     */
    public CustomerDTO upgradePlan(Long customerId, CustomerPlan newPlan) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        customer.setPlan(newPlan);
        Customer updatedCustomer = customerRepository.save(customer);
        return toDTO(updatedCustomer);
    }

    /**
     * Gets all customers.
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets customers by plan.
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getCustomersByPlan(CustomerPlan plan) {
        return customerRepository.findByPlan(plan).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts a Customer entity to DTO.
     */
    private CustomerDTO toDTO(Customer customer) {
        return CustomerDTO.from(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPlan(),
                customer.getCompanyName());
    }
}
