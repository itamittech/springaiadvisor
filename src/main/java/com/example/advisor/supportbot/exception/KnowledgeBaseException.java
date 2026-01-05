package com.example.advisor.supportbot.exception;

/**
 * Exception thrown when there's an issue with the knowledge base.
 */
public class KnowledgeBaseException extends RuntimeException {

    public KnowledgeBaseException(String message) {
        super(message);
    }

    public KnowledgeBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
