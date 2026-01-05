package com.example.advisor.controller;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.document.Document;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdvisorAssignmentController {

        private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";

        private final ChatClient chatClient;
        private final ChatMemory inMemoryChatMemory;
        private final ChatMemory jdbcChatMemory;
        private final InMemoryChatMemoryRepository sharedRepository = new InMemoryChatMemoryRepository();

        private final VectorStore vectorStore;
        private final org.springframework.ai.chat.client.advisor.SafeGuardAdvisor safeGuardAdvisor;
        private final org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor simpleLoggerAdvisor;

        public AdvisorAssignmentController(ChatClient.Builder builder, JdbcChatMemoryRepository jdbcRepository,
                        VectorStore vectorStore,
                        org.springframework.ai.chat.client.advisor.SafeGuardAdvisor safeGuardAdvisor,
                        org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor simpleLoggerAdvisor) {
                this.chatClient = builder.build();
                this.vectorStore = vectorStore;
                this.safeGuardAdvisor = safeGuardAdvisor;
                this.simpleLoggerAdvisor = simpleLoggerAdvisor;

                // Exercise 1 & 2: In-Memory via MessageWindowChatMemory (wrapping repository)
                this.inMemoryChatMemory = MessageWindowChatMemory.builder()
                                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                                .maxMessages(100) // "In-memory bot" - effectively large memory
                                .build();

                // Exercise 3: Persistent Memory (The "Restart" Test)
                // We assume JdbcChatMemoryRepository is auto-configured by the starter and
                // injected here.
                this.jdbcChatMemory = MessageWindowChatMemory.builder()
                                .chatMemoryRepository(jdbcRepository)
                                .maxMessages(100)
                                .build();
        }

        // Exercise 1: The "Elephant" Bot (In-Memory)
        @GetMapping("/advisor/chat/memory")
        public String chatMemory(@RequestParam String message) {
                return chatClient.prompt()
                                .user(message)
                                .advisors(MessageChatMemoryAdvisor.builder(inMemoryChatMemory).build())
                                .call()
                                .content();
        }

        // Exercise 2: The Multi-User Sandbox
        @GetMapping("/advisor/chat/user")
        public String chatUser(@RequestParam String message, @RequestHeader("userId") String userId) {
                return chatClient.prompt()
                                .user(message)
                                .advisors(a -> a
                                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, userId))
                                .advisors(MessageChatMemoryAdvisor.builder(inMemoryChatMemory).build())
                                .call()
                                .content();
        }

        // Exercise 3: Persistent Memory (The "Restart" Test)
        @GetMapping("/advisor/chat/persistent")
        public String chatPersistent(@RequestParam String message) {
                return chatClient.prompt()
                                .user(message)
                                .advisors(MessageChatMemoryAdvisor.builder(jdbcChatMemory).build())
                                .call()
                                .content();
        }

        // Exercise 4: The Sliding Window (Cost Optimization)
        @GetMapping("/advisor/chat/window")
        public String chatWindow(@RequestParam String message) {
                // Exercise 4: Use a small window
                ChatMemory windowMemory = MessageWindowChatMemory.builder()
                                .chatMemoryRepository(sharedRepository) // Shared logic
                                .maxMessages(6) // 3 exchanges
                                .build();

                return chatClient.prompt()
                                .user(message)
                                .advisors(MessageChatMemoryAdvisor.builder(windowMemory).build())
                                .call()
                                .content();
        }

        // Exercise 5: RAG (Question Answering)
        @GetMapping("/advisor/chat/rag")
        public String chatRag(@RequestParam String message) {
                // Manual RAG Implementation
                List<Document> documents = vectorStore.similaritySearch(
                                SearchRequest.builder().query(message).topK(2).build());

                String context = documents.stream()
                                .map(Document::getText)
                                .collect(Collectors.joining("\n\n"));

                return chatClient.prompt()
                                .user(message)
                                .system(s -> s.text(
                                                "You are a helpful assistant. Use the following context to answer the question:\n{context}")
                                                .param("context", context))
                                .call()
                                .content();
        }

        // Exercise 6: Content Safety
        @GetMapping("/advisor/chat/safety")
        public String chatSafety(@RequestParam String message) {
                return chatClient.prompt()
                                .user(message)
                                .advisors(safeGuardAdvisor)
                                .call()
                                .content();
        }

        // Exercise 7: Logging
        @GetMapping("/advisor/chat/logging")
        public String chatLogging(@RequestParam String message) {
                // Use SimpleLoggerAdvisor but also append a UI-visible message
                String response = chatClient.prompt()
                                .user(message)
                                .advisors(simpleLoggerAdvisor)
                                .call()
                                .content();

                return response + "\n\n[System Log: Request and Response have been logged to the console using SimpleLoggerAdvisor]";
        }
}
