# 🎓 Spring AI Agentic "Masterclass": Course Curriculum

This project is designed distinctively as a **"Living Textbook"**. Instead of just one final codebase, we structure the repository into **evolutionary branches**, where each branch represents a "Level" of mastery.

This allows learners to `git checkout` specific tags and understand the *evolution* of the code.

---

## 🗺️ The Learning Path

### 🌱 Level 1: The Basics (Branch: `level-1-basics`)
**Goal:** Hello World with AI.
- **Concepts:** `ChatClient`, `PromptTemplate`, `SystemMessage`.
- **Project:** A simple CLI CLI Chatbot.
- **Key Files:** `BasicChatController.java`.

### 📚 Level 2: Knowledge & Memory (Branch: `level-2-rag-memory`)
**Goal:** Making the AI "Smart" and "Contextual".
- **Concepts:**
    - **RAG:** Vector Stores (`SimpleVectorStore`), `DocumentReader`, Embedding Models.
    - **Memory:** `ChatMemory`, `MessageWindow`.
- **Project:** "DocuBot" - A bot that answers questions about a text file.

### 🛡️ Level 3: The Advisor Pattern (Branch: `level-3-advisors`)
> **Current State of Project** 📍
**Goal:** Controlling the AI with business logic (Middleware).
- **Concepts:**
    - `CallAdvisor` interface.
    - **Chaining:** Safety -> Context -> Sentiment -> Escalation.
    - **Structured Output:** Handling JSON responses manually.
- **Project:** "TaskFlow Support Bot v1" (Regex-based automation).

### 🤖 Level 4: Agentic AI & Tools (Branch: `level-4-agents`)
**Goal:** From "Passive Receiver" to "Active Doer".
- **Concepts:**
    - **Spring AI `@Tool`**: Function Calling.
    - **Agent Loop:** Reasoning -> Action -> Observation -> Reasoning.
    - **Dynamic execution:** Letting the LLM *choose* to check ticket status or create one.
- **Project:** "TaskFlow Agent" - It can fix date issues, check DB status, and issue refunds *autonomously*.

### 🏭 Level 5: Production Engineering (Branch: `level-5-production`)
**Goal:** Professional Grade AI.
- **Concepts:**
    - **Evaluation:** `Spring AI Eval` (Unit testing prompts).
    - **Infrastructure:** `PGVector` (Postgres) via Testcontainers.
    - **Ops:** Docker, Observability (OpenTelemetry).
- **Project:** "Enterprise Support Suite".

---

## 🧠 Innovation Strategy: "Innovate while we build"

To make this the **best practical learning medium**, we will:

1.  **Code Comments as Lessons**: Every major Spring AI component will have Javadoc explaining *why* it's there.
    ```java
    /**
     * @learning We use MessageWindowChatMemory here (max: 20) instead of unbounded
     * to prevent token overflow and reduce costs. Ideally, use VectorStore for long-term memory.
     */
    ```

2.  **Interactive "Why" Docs**: Markdown files in each package explaining the architectural decision (e.g., `src/main/java/.../advisor/README.md`).

3.  **Refactoring Challenges**: Leave "TODO" comments for the learner.
    *   *Example:* `// TODO: Task for Learner - Implement a 'SpamFilterAdvisor' here using the logical pattern shown above.`

---

## ⏭️ Next Step: Innovating to Level 4

We are currently at **Level 3**. To "innovate while we build", the immediate next step is:

**Refactor `TicketEscalationAdvisor` (Rule-Based) ➡️ `TicketTools` (Agentic).**

Instead of scanning for the word "refund" (fragile), we give the AI a tool:
```java
@Tool(description = "Issue a refund for a customer transaction")
public RefundReceipt refund(@ToolParam String transactionId) { ... }
```
This changes the paradigm from **Scripted** to **Reasoning**.
