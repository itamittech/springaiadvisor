# 🏭 Production Best Practices (Level 4 Agent)

Now that we have a working Agent, how do we make it "Enterprise Ready"?
Here are the key improvements to bridge the gap between Tutorial and Production.

## 1. Prompt Management (Externalization)
**Current:** Hardcoded `String SYSTEM_PROMPT = "..."` in Java.
**Problem:** Requires recompiling code to change the bot's personality.
**Best Practice:** Use `SystemPromptTemplate` loading from `src/main/resources/prompts/system.st`.
- Allows non-developers (Prompt Engineers) to edit prompts.
- Supports versioning of prompts separately from code.

## 2. Tool Robustness
**Current:** `TicketTools` accepts strings and does basic error handling.
**Problem:** What if the LLM hallucinates a priority "MEGA_HIGH"?
**Best Practice:**
- **Validation**: Explicitly validate inputs inside the tool (e.g. `Assert.isTrue`).
- **Idempotency**: Ensure that if the LLM retries the tool call, we don't duplicate the ticket (check `subject` + `customer` + `time`).
- **Structured Returns**: Return JSON from the tool, not just simple Strings. This gives the LLM more context on *what* happened.

## 3. Observability & Tracing
**Current:** `System.out.println("🤖 Tool Call...")`.
**Problem:** Cannot debug production issues.
**Best Practice:**
- **Structured Logging**: Use `WaitForIt` or SLF4J with MDC.
- **Spring AI Observability**: Enable Actuator metrics to track:
    - Token usage (Cost)
    - Latency (Performance)
    - Tool Execution Success/Failure rates.

## 4. Testing Strategy
**Current:** Browser Smoke Tests.
**Problem:** Slow and manual.
**Best Practice:**
- **Unit Tests for Tools**: Test `TicketTools` in isolation (standard JUnit).
- **Integration Tests (Eval)**: Use `Spring AI Eval` to mock the LLM and assert that "Refund request" -> "Tool Call Matcher".

## 5. Security (Prompt Injection)
**Current:** Basic `SafetyAdvisor`.
**Best Practice:**
- **Context Boundaries**: Clearly demarcate user input from system instructions using XML tags `<user_input>...</user_input>`.
- **Tool Scoping**: Ensure the `TicketTool` checks if the `customerId` passed by the LLM actually matches the authenticated user (preventing Cross-User actions).

---
## 🚀 Recommended Immediate Refinements
To make this repository a "Gold Standard", we should implement:

1.  **Refactor Prompts**: Extract `system.st`.
2.  **Secure Tools**: Add validation to `TicketTools`.
3.  **Add Unit Test**: Create `TicketToolsTest.java`.
