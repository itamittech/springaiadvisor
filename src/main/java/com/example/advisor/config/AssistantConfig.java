package com.example.advisor.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.embedding.EmbeddingModel;
import java.util.List;

@Configuration
public class AssistantConfig {
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .build();
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // Populate with some sample data for the QA advisor demo
        vectorStore.add(List.of(
                new Document("Spring AI is a framework for building AI applications with Spring Boot."),
                new Document("Advisors in Spring AI are used to process chat requests and responses."),
                new Document(
                        "The QuestionAnswerAdvisor provides RAG capabilities by adding context from a VectorStore."),
                new Document("VoltRetail is a leading electronics and apparel retailer.")));

        return vectorStore;
    }

}
