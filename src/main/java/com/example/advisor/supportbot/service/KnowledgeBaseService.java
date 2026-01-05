package com.example.advisor.supportbot.service;

import com.example.advisor.supportbot.exception.KnowledgeBaseException;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for interacting with the Support Bot's knowledge base.
 * Provides semantic search capabilities for RAG-based responses.
 */
@Service
public class KnowledgeBaseService {

    private final VectorStore vectorStore;

    public KnowledgeBaseService(@Qualifier("supportBotVectorStore") VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Searches the knowledge base for relevant articles.
     *
     * @param query The search query
     * @param topK  Number of results to return
     * @return List of relevant documents
     */
    public List<Document> searchArticles(String query, int topK) {
        try {
            return vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(query)
                            .topK(topK)
                            .build());
        } catch (Exception e) {
            throw new KnowledgeBaseException("Failed to search knowledge base: " + e.getMessage(), e);
        }
    }

    /**
     * Searches for articles in a specific category.
     *
     * @param query    The search query
     * @param category The category to filter by (faq, billing, troubleshooting)
     * @param topK     Number of results to return
     * @return List of relevant documents in the category
     */
    public List<Document> searchByCategory(String query, String category, int topK) {
        List<Document> results = searchArticles(query, topK * 2);

        return results.stream()
                .filter(doc -> category.equals(doc.getMetadata().get("category")))
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * Gets the context string from search results for injection into prompts.
     *
     * @param query The search query
     * @param topK  Number of results to use
     * @return Formatted context string
     */
    public String getContextForQuery(String query, int topK) {
        List<Document> documents = searchArticles(query, topK);

        if (documents.isEmpty()) {
            return "No relevant information found in the knowledge base.";
        }

        return documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    /**
     * Categorizes a query to determine which knowledge base section is most
     * relevant.
     *
     * @param query The user's query
     * @return The detected category (faq, billing, troubleshooting, general)
     */
    public String categorizeQuery(String query) {
        String lowerQuery = query.toLowerCase();

        // Billing-related keywords
        if (containsAny(lowerQuery, "bill", "price", "cost", "subscription", "payment",
                "refund", "cancel", "upgrade", "downgrade", "invoice", "charge", "plan")) {
            return "billing";
        }

        // Troubleshooting keywords
        if (containsAny(lowerQuery, "error", "not working", "problem", "issue", "bug",
                "crash", "slow", "loading", "fail", "help", "fix", "broken")) {
            return "troubleshooting";
        }

        // FAQ keywords
        if (containsAny(lowerQuery, "how to", "what is", "can i", "how do", "where",
                "feature", "capability", "support", "does it", "is there")) {
            return "faq";
        }

        return "general";
    }

    /**
     * Checks if the text contains any of the given keywords.
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
