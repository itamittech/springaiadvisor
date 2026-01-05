package com.example.advisor.supportbot.exception;

/**
 * Exception thrown when a customer is not found.
 */
public class CustomerNotFoundException extends RuntimeException {

    private final String identifier;

    public CustomerNotFoundException(Long id) {
        super("Customer not found with id: " + id);
        this.identifier = String.valueOf(id);
    }

    public CustomerNotFoundException(String email) {
        super("Customer not found with email: " + email);
        this.identifier = email;
    }

    public String getIdentifier() {
        return identifier;
    }
}
