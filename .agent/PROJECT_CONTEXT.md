# Spring AI Advisor - Project Context

## Project Overview
Spring Boot 3.5.9 + Spring AI 1.1.2 application demonstrating chat advisors.

## Key Files
| File | Purpose |
|---|---|
| `AdvisorConfiguration.java` | Bean definitions: VectorStore, SafeGuardAdvisor, CustomLoggingAdvisor, ChatMemory beans |
| `AdvisorAssignmentController.java` | 8 REST endpoints for different advisor demos |
| `CustomLoggingAdvisor.java` | CallAdvisor implementation for token/performance logging |
| `PromptEnhancerAdvisor.java` | Spring AOP @Aspect for prompt modification |
| `app.js` | Frontend: dynamic tutorials, markdown rendering in chat |
| `styles.css` | Dark theme with glassmorphism |

## Custom Advisor Pattern (Spring AI 1.1.2)
```java
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

public class CustomLoggingAdvisor implements CallAdvisor {
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        ChatClientResponse response = chain.nextCall(request);
        // Access: response.chatResponse().getMetadata().getUsage().getPromptTokens()
        return response;
    }
}
```

## Advisors Available
- `MessageChatMemoryAdvisor` - Chat memory management
- `SafeGuardAdvisor` - Content filtering
- `SimpleLoggerAdvisor` - Basic logging
- `QuestionAnswerAdvisor` - RAG (deprecated, use manual VectorStore search)

## Common Issues Resolved
1. **AdvisedRequest/Response not found**: Use `ChatClientRequest`/`ChatClientResponse` from `org.springframework.ai.chat.client`
2. **getGenerationTokens() not found**: Use `getCompletionTokens()` instead
3. **Port 8080 in use**: Kill with `taskkill /F /PID <pid>` (find via `jps -l`)

## Testing Commands
```bash
mvn spring-boot:run              # Start server
curl "localhost:8080/advisor/chat/memory?message=Hello"
curl -H "userId: user1" "localhost:8080/advisor/chat/user?message=Hello"
```
