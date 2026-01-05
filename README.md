# Advisor Assignments

This project demonstrates Spring AI Advisors and Chat Memory capabilities through 4 exercises.

## Prerequisites
- Java 21
- Maven
- OpenAI API Key configured in `.env` or environment variables.

## Running the Application
```bash
./mvnw spring-boot:run
```

## using the UI
Open your browser and navigate to:
[http://localhost:8080](http://localhost:8080)

This provides a modern, premium web interface to interact with all 4 advisor exercises.

## Exercises

### Exercise 1: The "Elephant" Bot (In-Memory)
Stateless bot converted to one that remembers using `InMemoryChatMemory`.

**Test:**
```bash
# Set a fact
curl "http://localhost:8080/advisor/chat/memory?message=My%20favorite%20color%20is%20blue"

# Ask about the fact
curl "http://localhost:8080/advisor/chat/memory?message=What%20color%20do%20I%20like?"
```

### Exercise 2: The Multi-User Sandbox
Ensures User A's secrets don't leak to User B using `userId`.

**Test:**
```bash
# User A sets a secret
curl -H "userId: UserA" "http://localhost:8080/advisor/chat/user?message=My%20secret%20code%20is%201234"

# User B asks for the secret (should fail)
curl -H "userId: UserB" "http://localhost:8080/advisor/chat/user?message=What%20is%20my%20secret%20code?"

# User A asks for the secret (should succeed)
curl -H "userId: UserA" "http://localhost:8080/advisor/chat/user?message=What%20is%20my%20secret%20code?"
```

### Exercise 3: Persistent Memory (The "Restart" Test)
Keeps history even if the Spring Boot app crashes using `JdbcChatMemoryRepository` (H2 Database).

**Features:**
- Uses `H2` in-memory database (simulating persistence).
- Uses `Spring AI`'s `JdbcChatMemoryRepository` to store conversation history.
- Demonstrates how to inject custom repositories into `MessageWindowChatMemory`.

**Test:**
```bash
# Set a fact
curl "http://localhost:8080/advisor/chat/persistent?message=I%20am%20testing%20persistence"

# RESTART the application (Ctrl+C, then run again)

# Ask about the fact
curl "http://localhost:8080/advisor/chat/persistent?message=What%20did%20I%20say%20I%20was%20testing?"
```


### Exercise 4: The Sliding Window (Cost Optimization)
Limits history to the last 3 exchanges.

**Test:**
```bash
# Send 4 messages
curl "http://localhost:8080/advisor/chat/window?message=1"
curl "http://localhost:8080/advisor/chat/window?message=2"
curl "http://localhost:8080/advisor/chat/window?message=3"
curl "http://localhost:8080/advisor/chat/window?message=4"

# Ask what was the first message (should ideally be forgotten if window is small enough, though 3 exchanges is 6 messages usually)
curl "http://localhost:8080/advisor/chat/window?message=What%20was%20the%20first%20thing%20I%20said?"
```

## Technical Implementation Notes

### Critical Dependencies (Spring AI 1.1.2)
- **Chat Memory**: The correct starter for persistent JDBC memory is `spring-ai-starter-model-chat-memory-repository-jdbc`.
  - *Avoid*: `spring-ai-jdbc` or `spring-ai-jdbc-store` (these may cause resolution errors).
- **BOM**: Always use `spring-ai-bom` for version management.

### Coding Patterns
- **Persistence Wrapper**:
  To enable the "Sliding Window" effect on top of persistent storage, wrap the repository:
  ```java
  MessageWindowChatMemory.builder()
      .chatMemoryRepository(jdbcRepository) // Injected JdbcChatMemoryRepository bean
      .maxMessages(100)
      .build();
  ```
- **Bean Injection**:
  `JdbcChatMemoryRepository` is auto-configured by the starter. Inject it directly into your Controller/Service constructors rather than instantiating it manually with `JdbcClient`.
