# 🤖 Agent Context Memory
> **READ THIS FIRST** when resuming the session.

## 📍 Current State: Level 4 (Agentic AI)
We have successfully implemented the **Support Agent** using **Spring AI Tools** (Function Calling).

### Key Accomplishments
1.  **Agentic Tools**: Implemented `TicketTools` with `@Tool`.
2.  **Autonomous Logic**: Replaced regex-based Advisor with LLM reasoning for ticket creation.
3.  **Advisor Chair**: Still using Safety, Context, Memory, and Sentiment advisors.
4.  **Verified**: Browser smoke tests confirmed autonomous ticket creation.

## 🛣️ The Roadmap (Next Steps)
We are ready for **Level 5: Production Engineering**.

**Goals:**
1.  **Evaluation**: Implement `Spring AI Eval` tests.
2.  **Infrastructure**: Replace H2 with PostgreSQL (PGVector) via Testcontainers.
3.  **Deployment**: Dockerize the application.

## 🏗️ Architecture Notes
- **Tools**: `src/main/java/.../tool/TicketTools.java`.
- **Config**: `SupportBotConfiguration` configures advisors. `SupportBotService` configures Tools.
- **Docs**: Detailed docs in `docs/`.

## ⚠️ Constraints
- Maintain "Educational" standard (comments explaining *why*).
