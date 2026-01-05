---
description: Create a Spring AI Maven Project with Persistent Memory and Chat Advisors
---

This workflow condenses the learnings from implementing Spring AI Advisors, specifically focusing on setting up a Maven project with OpenAI, Vector Stores, and Persistent Chat Memory (JDBC/H2).

1. **Initialize Project Structure**
   - Ensure the standard Maven directory structure exists:
     - `src/main/java/com/example/ai` (adjust package as needed)
     - `src/main/resources`
     - `src/test/java/com/example/ai`
   - Create standard sub-packages: `config`, `controller`, `model`, `repository`, `service`, `util`.

2. **Create `pom.xml` with Correct Dependencies**
   - **Crucial**: Use the `spring-ai-bom` in `<dependencyManagement>`.
   - **Core**: `spring-boot-starter-web`, `spring-ai-starter-model-openai`.
   - **Utils**: `spring-dotenv` (me.paulschwarz).
   - **Persistence (Critical Learning)**:
     - Use `spring-boot-starter-data-jdbc` and `h2` (or postgres).
     - **For Chat Memory**: Use `spring-ai-starter-model-chat-memory-repository-jdbc`. *Do not* use `spring-ai-jdbc` or `spring-ai-jdbc-store` which may be deprecated or incorrect for this purpose.
   - **Vector Store**: `spring-ai-vector-store`.

   ```xml
   <dependency>
       <groupId>org.springframework.ai</groupId>
       <artifactId>spring-ai-starter-model-chat-memory-repository-jdbc</artifactId>
   </dependency>
   ```

3. **Configure `application.properties`**
   - Enable H2 Console: `spring.h2.console.enabled=true`
   - Set OpenAI Key: `spring.ai.openai.api-key=${OPENAI_API_KEY}`
   - Configure JDBC: `spring.datasource.url=jdbc:h2:mem:testdb`, `spring.datasource.driverClassName=org.h2.Driver`.

4. **Create `AssistantConfig`**
   - Define beans for `ChatMemory` and `VectorStore`.
   - **Best Practice**: Use `MessageWindowChatMemory.builder()` to wrap repositories.

5. **Implement Chat Controller with Advisors**
   - Inject `ChatClient.Builder` and `JdbcChatMemoryRepository` (auto-configured).
   - Use `MessageChatMemoryAdvisor` for memory management.
   - **Persistence Pattern**:
     ```java
     // In Constructor
     this.persistentMemory = MessageWindowChatMemory.builder()
             .chatMemoryRepository(jdbcRepository) // Injected bean
             .maxMessages(100)
             .build();
     
     // In Endpoint
     chatClient.prompt()
             .user(msg)
             .advisors(MessageChatMemoryAdvisor.builder(persistentMemory).build())
             .call();
     ```

6. **Create `.env` and `.gitignore`**
   - Add `OPENAI_API_KEY=sk-...` to `.env`.
   - Add `.env` to `.gitignore` immediately to prevent secrets leakage.
