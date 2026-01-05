---
description: How to implement a custom Spring AI Advisor
---

# Implementing Custom Spring AI Advisors

This workflow guides you through creating custom advisors for Spring AI 1.1.x projects.

## Prerequisites
- Spring AI 1.1.2+ with `spring-ai-starter-model-openai`
- Java 21+

## Option 1: CallAdvisor Interface (Recommended)

### Step 1: Create the Advisor Class
```java
package com.example.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

public class CustomAdvisor implements CallAdvisor {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // PRE-PROCESSING: Modify request before chain
        System.out.println("Request: " + request.prompt().getContents());
        
        // Execute the chain
        ChatClientResponse response = chain.nextCall(request);
        
        // POST-PROCESSING: Access response metadata
        var usage = response.chatResponse().getMetadata().getUsage();
        System.out.println("Tokens: " + usage.getTotalTokens());
        
        return response;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public int getOrder() {
        return 0; // Lower = higher priority
    }
}
```

### Step 2: Register as Spring Bean
```java
@Configuration
public class AdvisorConfig {
    @Bean
    public CustomAdvisor customAdvisor() {
        return new CustomAdvisor();
    }
}
```

### Step 3: Use in Controller
```java
@RestController
public class ChatController {
    private final ChatClient chatClient;
    private final CustomAdvisor customAdvisor;

    public ChatController(ChatClient.Builder builder, CustomAdvisor customAdvisor) {
        this.chatClient = builder.build();
        this.customAdvisor = customAdvisor;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatClient.prompt()
            .user(message)
            .advisors(customAdvisor)
            .call()
            .content();
    }
}
```

## Option 2: Spring AOP Aspect (Alternative)

If CallAdvisor interface has dependency issues, use Spring AOP:

### Step 1: Add Dependency
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### Step 2: Create Aspect
```java
@Aspect
@Component
public class PromptEnhancerAdvisor {
    
    @Around("execution(* com.example.controller.ChatController.chat(..)) && args(message)")
    public Object enhancePrompt(ProceedingJoinPoint joinPoint, String message) throws Throwable {
        // Modify the message
        String enhanced = message + " Please format as bullet points.";
        
        // Replace arguments
        Object[] args = joinPoint.getArgs();
        args[0] = enhanced;
        
        return joinPoint.proceed(args);
    }
}
```

## Key APIs

| Class/Interface | Package | Purpose |
|---|---|---|
| `CallAdvisor` | `o.s.ai.chat.client.advisor.api` | Sync advisor interface |
| `StreamAdvisor` | `o.s.ai.chat.client.advisor.api` | Streaming advisor interface |
| `ChatClientRequest` | `o.s.ai.chat.client` | Request wrapper |
| `ChatClientResponse` | `o.s.ai.chat.client` | Response wrapper |
| `CallAdvisorChain` | `o.s.ai.chat.client.advisor.api` | Chain for next call |

## Token Usage Access
```java
var usage = response.chatResponse().getMetadata().getUsage();
usage.getPromptTokens();      // Input tokens
usage.getCompletionTokens();  // Output tokens (NOT getGenerationTokens!)
usage.getTotalTokens();       // Total
```

## Chaining Multiple Advisors
```java
chatClient.prompt()
    .user(message)
    .advisors(loggingAdvisor)      // First
    .advisors(safeGuardAdvisor)    // Second
    .advisors(memoryAdvisor)       // Third
    .call()
    .content();
```

## Built-in Advisors
- `SimpleLoggerAdvisor` - Basic logging
- `SafeGuardAdvisor` - Content filtering
- `MessageChatMemoryAdvisor` - Chat memory
- `QuestionAnswerAdvisor` - RAG (use VectorStore directly for more control)

## Common Issues

| Error | Solution |
|---|---|
| `AdvisedRequest not found` | Use `ChatClientRequest` from `o.s.ai.chat.client` |
| `getGenerationTokens() not found` | Use `getCompletionTokens()` instead |
| `CallAroundAdvisor not found` | Use `CallAdvisor` (1.1.x naming) |
