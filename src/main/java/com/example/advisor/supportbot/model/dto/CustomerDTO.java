package com.example.advisor.supportbot.model.dto;

import com.example.advisor.supportbot.model.enums.CustomerPlan;

/**
 * DTO for customer data transfer.
 */
public record CustomerDTO(
        Long id,
        String name,
        String email,
        CustomerPlan plan,
        String companyName,
        boolean isPaid) {
    /**
     * Creates a CustomerDTO from entity data.
     */
    public static CustomerDTO from(Long id, String name, String email, CustomerPlan plan, String companyName) {
        return new CustomerDTO(id, name, email, plan, companyName, plan != CustomerPlan.FREE);
    }
}
