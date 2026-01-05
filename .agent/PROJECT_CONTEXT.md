# 🤖 Agent Context Memory
> **READ THIS FIRST** when resuming the session.

## 📍 Current State: Level 3 (Advisors)
We have successfully implemented the **Support Bot** using the **Advisor Pattern** (Chain of Responsibility).

### Key Accomplishments
1.  **Advisor Chain**: Implemented Safety -> Context -> Sentiment -> Escalation -> Formatting.
2.  **RAG**: Implemented `KnowledgeBaseService` with simple text chunking.
3.  **UI**: Created Thymeleaf frontend at `/supportbot`.
4.  **Standardization**: Added educational Javadocs (`@learning`) and diagrams to Level 3 code.

## 🛣️ The Roadmap (Next Steps)
We are moving to **Level 4: Agentic AI**.

**Goal:** Refactor "Ticket Escalation" from *Rule-Based* to *Agentic*.

**Tasks:**
1.  Create feature branch `level-4-agents`.
2.  Refactor `TicketEscalationAdvisor` (delete or disable it).
3.  Create `TicketTools.java` with `@Tool`.
4.  Update `SupportBotService` to enable Function Calling.

## 🏗️ Architecture Notes
- **Docs**: detailed docs are in `docs/`.
- **Advisors**: `src/main/java/.../advisor/`.
- **Config**: `SupportBotConfiguration.java` currently wires manual advisors.

## ⚠️ Constraints
- Keep `application.properties` clean (H2 by default).
- Maintain "Educational" standard (comments explaining *why*).
