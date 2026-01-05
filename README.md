# Spring AI Advisor Application

A comprehensive Spring AI demonstration showcasing memory management, RAG, content safety, and custom advisors.

## Quick Start

```bash
export OPENAI_API_KEY=sk-...
mvn spring-boot:run
```

Access UI: `http://localhost:8080/`

## Features

| Feature | Endpoint | Description |
|---|---|---|
| In-Memory | `/advisor/chat/memory` | Conversation in memory, resets on restart |
| Multi-User | `/advisor/chat/user` | Separate histories via `userId` header |
| Persistent | `/advisor/chat/persistent` | H2 database persistence |
| Sliding Window | `/advisor/chat/window` | Keeps last 6 messages for cost optimization |
| RAG | `/advisor/chat/rag` | Uses Mars Colonization Guide |
| Safety | `/advisor/chat/safety` | Filters sensitive words |
| Logging | `/advisor/chat/logging` | Token usage logging via `CustomLoggingAdvisor` |
| Custom Feature | `/advisor/chat/custom-feature` | Combined: RAG + Memory + Safety + Logging |

## Custom Advisors

### CustomLoggingAdvisor
Implements `CallAdvisor` interface to log token usage and execution time:
```java
public class CustomLoggingAdvisor implements CallAdvisor, StreamAdvisor {
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // Log request, execute chain, log response + tokens
    }
}
```

### PromptEnhancerAdvisor
Uses Spring AOP `@Aspect` to modify prompts before processing:
```java
@Aspect @Component
public class PromptEnhancerAdvisor {
    @Around("execution(* ...chatCustomFeature(..))")
    public Object enhancePrompt(ProceedingJoinPoint joinPoint, String message) {
        // Append formatting instructions to user prompt
    }
}
```

## Screenshots

### In-Memory Chat
![In-Memory](docs/screenshots/tab_memory_1767603966631.png)

### Multi-User Sandbox
![Multi-User](docs/screenshots/tab_user_1767604498759.png)

### Persistent Memory
![Persistent](docs/screenshots/tab_persistent_1767604619314.png)

### Sliding Window
![Sliding Window](docs/screenshots/tab_window_1767604850260.png)

### RAG (Mars Guide)
![RAG](docs/screenshots/tab_rag_1767605419853.png)

### Custom Feature (Advisor Chain)
![Custom Feature](docs/screenshots/custom_feature_demo_final_1767603638997.png)

## Architecture

```
AdvisorConfiguration.java    - Bean definitions for all advisors
AdvisorAssignmentController  - REST endpoints
CustomLoggingAdvisor         - CallAdvisor implementation for logging
PromptEnhancerAdvisor        - Spring AOP aspect for prompt modification
```

## Dependencies

- `spring-ai-starter-model-openai` - OpenAI integration
- `spring-ai-starter-model-chat-memory-repository-jdbc` - H2 persistence
- `spring-ai-starter-vector-store-simple` - Vector store for RAG
- `spring-boot-starter-aop` - AOP support for PromptEnhancer
