# 🎓 Spring AI Agentic Masterclass
> A "Living Textbook" for building Production-Grade AI Agents with Spring AI.

This repository is structured as a **Practical Course**. Instead of a single finished project, it evolves through **Levels**.

## 📍 Current Status: Level 4 (Agentic AI)
- **Level 4: Agentic AI**: Autonomous "Agent" that uses tools (`createTicket`) to take action based on reasoning.
- **Level 4.5: Streaming UI**: Real-time token streaming (SSE) and a premium minimalist "Neural" design theme.

---

## 🗺️ The Curriculum

| Level | Branch | Concept | Project |
|-------|--------|---------|---------|
| **1** | `level-1-basics` | Basics (ChatClient) | Simple Console Bot |
| **2** | `level-2-rag` | Knowledge (VectorStore) | DocuBot |
| **3** | `level-3-advisors` | Middleware (Advisors) | TaskFlow Bot (Regex) |
| **4** | `main` | **Agency (Tools)** | **TaskFlow Agent** |
| **5** | `level-5-production` | Engineering (Eval, Docker) | *Future* |

👉 **[Start Learning: View Full Curriculum & Roadmap](docs/COURSE_CURRICULUM.md)**

---

## 🚀 Quick Start (Level 3 Support Bot)

### 1. Run the Bot
```bash
export OPENAI_API_KEY=sk-...
./mvnw spring-boot:run
```

### 2. Try the UI
Open **[http://localhost:8080/supportbot](http://localhost:8080/supportbot)**

### 3. Learn the Architecture
We have detailed documentation on how this bot works:
- **[Support Bot Tutorial (Deep Dive)](docs/SUPPORT_BOT_TUTORIAL.md)**: Explains the Advisor Chain, RAG, and UI.
- **[Advisor Pattern Guide](src/main/java/com/example/advisor/supportbot/advisor/README.md)**: Explains the middleware logic.

---

## 🛠️ Tech Stack
- **Spring AI 1.x**
- **OpenAI GPT-4o**
- **H2 / PGVector**
- **Thymeleaf UI**

---
*Maintained by the Spring AI Learning Team.*
