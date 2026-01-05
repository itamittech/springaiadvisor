package com.example.advisor.supportbot.controller;

import com.example.advisor.supportbot.model.dto.CustomerDTO;
import com.example.advisor.supportbot.model.enums.CustomerPlan;
import com.example.advisor.supportbot.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing customers.
 */
@RestController
@RequestMapping("/support/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Get all customers.
     * 
     * GET /support/customers
     */
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        List<CustomerDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * Get customer by ID.
     * 
     * GET /support/customers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable Long id) {
        CustomerDTO customer = customerService.getCustomer(id);
        return ResponseEntity.ok(customer);
    }

    /**
     * Create a new customer.
     * 
     * POST /support/customers
     */
    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(defaultValue = "FREE") CustomerPlan plan,
            @RequestParam(required = false) String companyName) {

        CustomerDTO customer = customerService.createCustomer(name, email, plan, companyName);
        return ResponseEntity.ok(customer);
    }

    /**
     * Upgrade customer plan.
     * 
     * PATCH /support/customers/{id}/plan
     */
    @PatchMapping("/{id}/plan")
    public ResponseEntity<CustomerDTO> upgradePlan(
            @PathVariable Long id,
            @RequestParam CustomerPlan plan) {

        CustomerDTO customer = customerService.upgradePlan(id, plan);
        return ResponseEntity.ok(customer);
    }

    /**
     * Get customers by plan.
     * 
     * GET /support/customers/plan/{plan}
     */
    @GetMapping("/plan/{plan}")
    public ResponseEntity<List<CustomerDTO>> getCustomersByPlan(@PathVariable CustomerPlan plan) {
        List<CustomerDTO> customers = customerService.getCustomersByPlan(plan);
        return ResponseEntity.ok(customers);
    }
}
