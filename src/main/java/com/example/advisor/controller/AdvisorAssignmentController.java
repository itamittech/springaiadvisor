package com.example.advisor.controller;

import com.example.advisor.advisor.CustomLoggingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AdvisorAssignmentController demonstrates various Spring AI Chat Advisors.
 */
@RestController
public class AdvisorAssignmentController {

        private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";

        private final ChatClient chatClient;
        private final ChatMemory inMemoryChatMemory;
        private final ChatMemory persistentChatMemory;
        private final InMemoryChatMemoryRepository sharedRepository;

        private final VectorStore vectorStore;
        private final SafeGuardAdvisor safeGuardAdvisor;
        private final CustomLoggingAdvisor customLoggingAdvisor;

        // For custom feature endpoint
        private final MessageChatMemoryAdvisor messageChatMemoryAdvisor;

        public AdvisorAssignmentController(ChatClient.Builder builder,
                        VectorStore vectorStore,
                        SafeGuardAdvisor safeGuardAdvisor,
                        CustomLoggingAdvisor customLoggingAdvisor,
                        MessageChatMemoryAdvisor messageChatMemoryAdvisor,
                        @Qualifier("inMemoryChatMemory") ChatMemory inMemoryChatMemory,
                        @Qualifier("persistentChatMemory") ChatMemory persistentChatMemory,
                        InMemoryChatMemoryRepository sharedRepository) {
                this.chatClient = builder.build();
                this.vectorStore = vectorStore;
                this.safeGuardAdvisor = safeGuardAdvisor;
                this.customLoggingAdvisor = customLoggingAdvisor;
                this.messageChatMemoryAdvisor = messageChatMemoryAdvisor;
                this.inMemoryChatMemory = inMemoryChatMemory;
                this.persistentChatMemory = persistentChatMemory;
                this.sharedRepository = sharedRepository;
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
                                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, userId))
                                .advisors(MessageChatMemoryAdvisor.builder(inMemoryChatMemory).build())
                                .call()
                                .content();
        }

        // Exercise 3: Persistent Memory (The "Restart" Test)
        @GetMapping("/advisor/chat/persistent")
        public String chatPersistent(@RequestParam String message) {
                return chatClient.prompt()
                                .user(message)
                                .advisors(MessageChatMemoryAdvisor.builder(persistentChatMemory).build())
                                .call()
                                .content();
        }

        // Exercise 4: The Sliding Window (Cost Optimization)
        @GetMapping("/advisor/chat/window")
        public String chatWindow(@RequestParam String message) {
                ChatMemory windowMemory = MessageWindowChatMemory.builder()
                                .chatMemoryRepository(sharedRepository)
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

        // Exercise 7: Logging (Using CustomLoggingAdvisor)
        @GetMapping("/advisor/chat/logging")
        public String chatLogging(@RequestParam String message) {
                return chatClient.prompt()
                                .user(message)
                                .advisors(customLoggingAdvisor)
                                .call()
                                .content();
        }

        // Exercise 8: Custom Feature (Combined Advisors)
        @GetMapping("/advisor/chat/custom-feature")
        public String chatCustomFeature(@RequestParam String message) {
                // 1. RAG Retrieve
                List<Document> documents = vectorStore.similaritySearch(
                                SearchRequest.builder().query(message).topK(2).build());
                String context = documents.stream()
                                .map(Document::getText)
                                .collect(Collectors.joining("\n\n"));

                // 2. Call with all advisors: CustomLogging + SafeGuard + Memory + RAG Context
                return chatClient.prompt()
                                .user(message)
                                // RAG Context
                                .system(s -> s.text(
                                                "You are a helpful assistant. Use the following context to answer the question:\n{context}")
                                                .param("context", context))
                                // Advisor Chain: CustomLogging -> SafeGuard -> Memory
                                .advisors(customLoggingAdvisor)
                                .advisors(safeGuardAdvisor)
                                .advisors(messageChatMemoryAdvisor)
                                .call()
                                .content();
        }
}
