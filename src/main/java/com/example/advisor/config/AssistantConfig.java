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
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import java.util.List;

@Configuration
public class AssistantConfig {

    @Value("classpath:rag-docs/mars_colonization_guide.txt")
    private Resource ragDocument;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .build();
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // Load and ingest the document
        if (ragDocument.exists()) {
            TextReader reader = new TextReader(ragDocument);
            List<Document> documents = reader.get();
            List<Document> splitDocuments = new TokenTextSplitter().apply(documents);
            vectorStore.add(splitDocuments);
        }

        // Add previous sample data as well if needed, or replace it.
        // For this task, we prioritize Mars data.

        return vectorStore;
    }

    @Bean
    public SafeGuardAdvisor safeGuardAdvisor() {
        List<String> sensitiveWords = List.of("dark", "scary", "fart", "violence", "kill");
        return new SafeGuardAdvisor(sensitiveWords);
    }

    @Bean
    public SimpleLoggerAdvisor simpleLoggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

}
