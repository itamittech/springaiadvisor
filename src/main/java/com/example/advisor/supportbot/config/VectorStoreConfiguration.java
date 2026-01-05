package com.example.advisor.supportbot.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the Support Bot's Knowledge Base Vector Store.
 * Loads product documentation into a vector store for RAG-based responses.
 */
@Configuration
public class VectorStoreConfiguration {

    @Value("classpath:supportbot-docs/product_faq.txt")
    private Resource productFaqResource;

    @Value("classpath:supportbot-docs/billing_policy.txt")
    private Resource billingPolicyResource;

    @Value("classpath:supportbot-docs/troubleshooting.txt")
    private Resource troubleshootingResource;

    /**
     * Creates a dedicated VectorStore for the Support Bot knowledge base.
     * This is separate from the main tutorial vector store.
     */
    @Bean
    @Qualifier("supportBotVectorStore")
    public VectorStore supportBotVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        List<Document> allDocuments = new ArrayList<>();

        // Load all knowledge base documents
        allDocuments.addAll(loadDocumentWithCategory(productFaqResource, "faq"));
        allDocuments.addAll(loadDocumentWithCategory(billingPolicyResource, "billing"));
        allDocuments.addAll(loadDocumentWithCategory(troubleshootingResource, "troubleshooting"));

        // Split documents for optimal retrieval
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> splitDocuments = splitter.apply(allDocuments);

        vectorStore.add(splitDocuments);

        System.out.println("✅ Support Bot Knowledge Base loaded: " + splitDocuments.size() + " document chunks");

        return vectorStore;
    }

    /**
     * Loads a document and adds category metadata.
     */
    private List<Document> loadDocumentWithCategory(Resource resource, String category) {
        if (!resource.exists()) {
            System.out.println("⚠️ Knowledge base document not found: " + resource.getFilename());
            return List.of();
        }

        TextReader reader = new TextReader(resource);
        List<Document> documents = reader.get();

        // Add category metadata to each document
        documents.forEach(doc -> doc.getMetadata().put("category", category));

        return documents;
    }
}
