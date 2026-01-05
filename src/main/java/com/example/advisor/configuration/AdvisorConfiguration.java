package com.example.advisor.configuration;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;

@Configuration
public class AdvisorConfiguration {

    @Value("classpath:rag-docs/mars_colonization_guide.txt")
    private Resource ragDocument;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // Load documents
        if (ragDocument.exists()) {
            TextReader textReader = new TextReader(ragDocument);
            List<Document> documents = textReader.get();
            TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
            List<Document> splitDocuments = tokenTextSplitter.apply(documents);
            vectorStore.add(splitDocuments);
        }

        return vectorStore;
    }

    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(100)
                .build();
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    @Bean
    public SafeGuardAdvisor safeGuardAdvisor() {
        List<String> sensitiveWords = List.of("dark", "scary", "fart", "violence", "kill");
        return new SafeGuardAdvisor(sensitiveWords);
    }

    @Bean
    @Qualifier("inMemoryChatMemory")
    public ChatMemory inMemoryChatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(100)
                .build();
    }

    @Bean
    @Qualifier("persistentChatMemory")
    public ChatMemory persistentChatMemory(JdbcChatMemoryRepository jdbcRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcRepository)
                .maxMessages(100)
                .build();
    }

    // Shared repository for sliding window exercise
    @Bean
    public InMemoryChatMemoryRepository sharedInMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }
}
