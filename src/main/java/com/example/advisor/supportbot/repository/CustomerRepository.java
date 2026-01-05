package com.example.advisor.supportbot.repository;

import com.example.advisor.supportbot.model.entity.Customer;
import com.example.advisor.supportbot.model.enums.CustomerPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Customer entity operations.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find a customer by email address.
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find all customers with a specific subscription plan.
     */
    List<Customer> findByPlan(CustomerPlan plan);

    /**
     * Find all customers by company name.
     */
    List<Customer> findByCompanyName(String companyName);

    /**
     * Check if a customer exists with the given email.
     */
    boolean existsByEmail(String email);
}
