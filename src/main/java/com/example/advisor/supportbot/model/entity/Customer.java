package com.example.advisor.supportbot.model.entity;

import com.example.advisor.supportbot.model.enums.CustomerPlan;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a customer in the support system.
 * Customers can have different subscription plans affecting their support
 * priority.
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerPlan plan = CustomerPlan.FREE;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActiveAt = LocalDateTime.now();
    }

    // Constructors
    public Customer() {
    }

    public Customer(String name, String email, CustomerPlan plan) {
        this.name = name;
        this.email = email;
        this.plan = plan;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public CustomerPlan getPlan() {
        return plan;
    }

    public void setPlan(CustomerPlan plan) {
        this.plan = plan;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    /**
     * Returns true if this customer is on a paid plan.
     */
    public boolean isPaidCustomer() {
        return plan != CustomerPlan.FREE;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", plan=" + plan +
                '}';
    }
}
