# 🤖 TaskFlow Customer Support Bot

A production-ready AI-powered customer support bot built with **Spring AI**. This project demonstrates real-world patterns for building intelligent support systems with conversation memory, RAG-based knowledge retrieval, sentiment analysis, and automatic ticket escalation.

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Features](#features)
4. [Getting Started](#getting-started)
5. [Package Structure](#package-structure)
6. [Custom Advisors (Deep Dive)](#custom-advisors-deep-dive)
7. [Knowledge Base (RAG)](#knowledge-base-rag)
8. [API Reference](#api-reference)
9. [UI Guide](#ui-guide)
10. [Configuration](#configuration)
11. [Extending the Bot](#extending-the-bot)

---

## Overview

### What is this?

This is **Part 2** of the Spring AI Advisor Tutorial Series. It builds upon the basics learned in Part 1 (memory management, RAG, safety filters) and applies them to a real-world use case: an AI-powered customer support chatbot.

### The Scenario

Imagine you're building a support bot for **TaskFlow** - a project management SaaS platform. The bot needs to:

- Answer customer questions using product documentation
- Remember conversation history across sessions
- Personalize responses based on customer subscription plan
- Detect frustrated customers and escalate when needed
- Auto-create support tickets when customers request human help
- Filter inappropriate content

### Learning Objectives

By studying this codebase, you will learn:

| Skill | Description |
|-------|-------------|
| **Production Architecture** | How to structure a Spring AI application for maintainability |
| **Custom Advisors** | Creating advisor chains for complex business logic |
| **RAG Implementation** | Building semantic search with vector stores |
| **Sentiment Analysis** | Detecting customer mood from text |
| **Auto-Escalation** | Triggering actions based on message content |
| **JPA Integration** | Persisting entities alongside chat memory |

---

## Architecture

### High-Level Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              USER MESSAGE                                    │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         ADVISOR CHAIN (Pre-LLM)                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐ │
│  │   Safety     │─▶│  Customer    │─▶│  Sentiment   │─▶│    Ticket       │ │
│  │   Advisor    │  │  Context     │  │  Analysis    │  │   Escalation    │ │
│  └──────────────┘  └──────────────┘  └──────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          KNOWLEDGE BASE (RAG)                                │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │  Vector Store Query → Retrieve relevant docs → Inject into prompt       ││
│  └─────────────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            CHAT MEMORY                                       │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │  Load conversation history → Add to context → Sliding window (20 msgs)  ││
│  └─────────────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              LLM (GPT-4o)                                    │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         ADVISOR CHAIN (Post-LLM)                             │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │  Response Formatting Advisor → Log response → Add analytics             ││
│  └─────────────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              BOT RESPONSE                                    │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Technology Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.5.9 |
| AI | Spring AI 1.1.2 with OpenAI GPT-4o |
| Database | H2 (in-memory) with JPA/Hibernate |
| Vector Store | SimpleVectorStore (in-memory) |
| UI | Thymeleaf + Vanilla CSS + JavaScript |
| Markdown | Marked.js for rendering |

---

## Features

### 🛡️ Safety Filtering

**What it does:** Blocks inappropriate content before it reaches the LLM.

**Blocked Content:**
- Abusive language ("you idiot", "stupid bot", etc.)
- Competitor mentions
- Internal/confidential information requests
- Hacking/exploit attempts

**How it works:**
```java
// SupportSafetyAdvisor.java
if (containsAbusiveLanguage(userMessage)) {
    return createBlockedResponse(ABUSIVE_RESPONSE);  // Short-circuit the chain
}
return chain.nextCall(request);  // Continue if safe
```

---

### 👤 Customer Personalization

**What it does:** Tailors responses based on the customer's subscription plan.

**Personalization Examples:**

| Plan | Response Style |
|------|---------------|
| **FREE** | Helpful, but mentions upgrade benefits |
| **PREMIUM** | Acknowledges subscription, highlights premium features |
| **ENTERPRISE** | Detailed technical responses, mentions dedicated support |

**How it works:**
- Fetches customer profile from database
- Injects customer context into system prompt
- LLM uses context to personalize responses

---

### 📊 Sentiment Analysis

**What it does:** Detects customer mood to prioritize frustrated users.

**Detected Sentiments:**

| Sentiment | Indicators | Action |
|-----------|-----------|--------|
| 😊 POSITIVE | "thanks", "great", "awesome" | Normal flow |
| 😐 NEUTRAL | Default | Normal flow |
| 😤 FRUSTRATED | "annoying", "disappointed", "unacceptable" | Log warning |
| 😠 ANGRY | "furious", "hate", "worst" | Priority handling |

**Console Output:**
```
📊 Sentiment Analysis: 😤 Frustrated
```

---

### 🎫 Auto-Ticket Escalation

**What it does:** Automatically creates support tickets when customers request human help.

**Trigger Phrases:**
- "speak to human" / "talk to someone"
- "manager" / "supervisor"
- "cancel my subscription" / "want a refund"
- "legal action" / "lawyer"

**Priority Assignment:**

| Trigger | Priority |
|---------|----------|
| "lawyer", "legal action" | CRITICAL |
| "refund", "cancel", "manager" | HIGH |
| "human agent", "real person" | MEDIUM |

**Console Output:**
```
🎫 Escalation Ticket Created - ID: 4, Customer: John Smith, Priority: HIGH, Trigger: want a refund
```

---

### 📚 RAG Knowledge Base

**What it does:** Retrieves relevant documentation to answer customer questions accurately.

**Knowledge Base Documents:**

| Document | Content |
|----------|---------|
| `product_faq.txt` | General product questions, features, mobile app |
| `billing_policy.txt` | Pricing plans, cancellation, refunds |
| `troubleshooting.txt` | Login issues, app performance, integrations |

**How it works:**
1. User asks a question
2. `KnowledgeBaseService` queries the vector store
3. Top 3 relevant document chunks are retrieved
4. Chunks are injected into the system prompt as context
5. LLM answers based on the documentation

---

### 💾 Persistent Memory

**What it does:** Remembers conversations even after server restarts.

**Implementation:**
- Uses JDBC repository backed by H2 database
- Sliding window keeps last 20 messages (cost optimization)
- Conversation ID based on session or customer ID

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- OpenAI API Key

### Running the Application

```bash
# 1. Clone the repository (if not already done)
git clone https://github.com/yourusername/advisor.git
cd advisor

# 2. Set your OpenAI API key
export OPENAI_API_KEY=sk-...

# 3. Run the application
./mvnw spring-boot:run

# 4. Open in browser
# Support Bot UI: http://localhost:8080/supportbot
# Part 1 Basics UI: http://localhost:8080/
# H2 Console: http://localhost:8080/h2-console
```

### Quick Test

**Via UI:**
1. Open http://localhost:8080/supportbot
2. Select a customer from the dropdown (e.g., "John Smith (Premium)")
3. Type "How do I reset my password?" and send
4. Observe the RAG-powered response with knowledge base context

**Via API:**
```bash
curl "http://localhost:8080/support/chat?message=What%20are%20the%20pricing%20plans"
```

---

## Package Structure

```
src/main/java/com/example/advisor/supportbot/
│
├── config/                           # Spring Configuration
│   ├── SupportBotConfiguration.java  # Advisor beans, memory config
│   └── VectorStoreConfiguration.java # Knowledge base vector store
│
├── controller/                       # HTTP Endpoints
│   ├── SupportChatController.java    # Chat REST API
│   ├── TicketController.java         # Ticket CRUD API
│   ├── CustomerController.java       # Customer CRUD API
│   └── SupportBotWebController.java  # UI page controller
│
├── service/                          # Business Logic
│   ├── SupportBotService.java        # Main chat orchestration
│   ├── KnowledgeBaseService.java     # RAG search & categorization
│   ├── TicketService.java            # Ticket management
│   └── CustomerService.java          # Customer management
│
├── advisor/                          # Custom Spring AI Advisors
│   ├── SupportSafetyAdvisor.java     # Content filtering (Order: 0)
│   ├── CustomerContextAdvisor.java   # Profile injection (Order: 10)
│   ├── SentimentAnalysisAdvisor.java # Mood detection (Order: 20)
│   ├── TicketEscalationAdvisor.java  # Auto-ticketing (Order: 30)
│   └── ResponseFormattingAdvisor.java # Response logging (Order: 1000)
│
├── model/
│   ├── entity/                       # JPA Entities
│   │   ├── Customer.java             # Customer profile
│   │   ├── Ticket.java               # Support ticket
│   │   ├── Article.java              # Knowledge base article
│   │   └── ConversationSession.java  # Session metadata
│   │
│   ├── dto/                          # Data Transfer Objects
│   │   ├── ChatRequest.java          # Chat input
│   │   ├── ChatResponse.java         # Chat output with metadata
│   │   ├── CustomerDTO.java          # Customer API response
│   │   └── TicketDTO.java            # Ticket API response
│   │
│   └── enums/                        # Type-safe Constants
│       ├── CustomerPlan.java         # FREE, PREMIUM, ENTERPRISE
│       ├── TicketStatus.java         # OPEN, IN_PROGRESS, RESOLVED, CLOSED
│       ├── TicketPriority.java       # LOW, MEDIUM, HIGH, CRITICAL
│       └── SentimentType.java        # POSITIVE, NEUTRAL, FRUSTRATED, ANGRY
│
├── repository/                       # Data Access (Spring Data JPA)
│   ├── CustomerRepository.java
│   ├── TicketRepository.java
│   ├── ArticleRepository.java
│   └── ConversationSessionRepository.java
│
└── exception/                        # Error Handling
    ├── CustomerNotFoundException.java
    ├── TicketNotFoundException.java
    └── KnowledgeBaseException.java
```

---

## Custom Advisors (Deep Dive)

### What is an Advisor?

An **Advisor** in Spring AI is a middleware component that intercepts ChatClient calls. Think of it like an HTTP filter, but for LLM requests.

### Advisor Interface

```java
public interface CallAdvisor {
    // Called for each chat request
    ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain);
    
    // Name for identification
    String getName();
    
    // Order in the chain (lower = earlier)
    int getOrder();
}
```

### Advisor Chain Execution Order

```
Request → Safety(0) → Context(10) → Sentiment(20) → Escalation(30) → Memory → LLM → Formatting(1000) → Response
```

### Creating Your Own Advisor

```java
@Component
public class MyCustomAdvisor implements CallAdvisor, StreamAdvisor {

    @Override
    public String getName() {
        return "MyCustomAdvisor";
    }

    @Override
    public int getOrder() {
        return 50; // Runs after escalation, before memory
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // PRE-PROCESSING: Modify request or short-circuit
        String userMessage = request.prompt().getContents();
        
        if (shouldBlock(userMessage)) {
            // Short-circuit: return custom response without calling LLM
            return createCustomResponse("I cannot process this request.");
        }
        
        // CONTINUE CHAIN: Pass to next advisor
        ChatClientResponse response = chain.nextCall(request);
        
        // POST-PROCESSING: Modify or log response
        logResponse(response);
        
        return response;
    }
    
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        // Similar logic for streaming responses
        return chain.nextStream(request);
    }
}
```

### Key Patterns

| Pattern | When to Use | Example |
|---------|------------|---------|
| **Short-Circuit** | Block requests that shouldn't reach LLM | Safety filtering |
| **Request Enhancement** | Add context to prompts | Customer personalization |
| **Side Effects** | Perform actions based on content | Ticket creation |
| **Response Logging** | Analytics and monitoring | Token usage tracking |

---

## Knowledge Base (RAG)

### How RAG Works

1. **Indexing** (at startup):
   ```
   Documents → Text Splitter → Chunks → Embedding Model → Vectors → Vector Store
   ```

2. **Querying** (at runtime):
   ```
   User Query → Embedding → Similarity Search → Top-K Chunks → Inject into Prompt
   ```

### VectorStoreConfiguration

```java
@Bean
@Qualifier("supportBotVectorStore")
public VectorStore supportBotVectorStore(EmbeddingModel embeddingModel) {
    SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
    
    // Load documents with category metadata
    allDocuments.addAll(loadDocumentWithCategory(productFaqResource, "faq"));
    allDocuments.addAll(loadDocumentWithCategory(billingPolicyResource, "billing"));
    allDocuments.addAll(loadDocumentWithCategory(troubleshootingResource, "troubleshooting"));
    
    // Split and add to store
    TokenTextSplitter splitter = new TokenTextSplitter();
    vectorStore.add(splitter.apply(allDocuments));
    
    return vectorStore;
}
```

### Query Categorization

The `KnowledgeBaseService` automatically categorizes queries:

```java
public String categorizeQuery(String query) {
    if (containsAny(query, "bill", "price", "refund", "cancel")) {
        return "billing";
    }
    if (containsAny(query, "error", "not working", "help")) {
        return "troubleshooting";
    }
    if (containsAny(query, "how to", "what is", "feature")) {
        return "faq";
    }
    return "general";
}
```

---

## API Reference

### Chat Endpoints

#### POST /support/chat
Send a message to the support bot.

**Request Body:**
```json
{
  "message": "How do I reset my password?",
  "customerId": "1",
  "sessionId": "session-123"
}
```

**Response:**
```json
{
  "message": "To reset your password, follow these steps...",
  "sessionId": "session-123",
  "sentiment": "NEUTRAL",
  "ticketCreated": false,
  "ticketId": null,
  "timestamp": "2024-01-05T10:30:00"
}
```

#### GET /support/chat
Simple chat for testing.

```bash
curl "http://localhost:8080/support/chat?message=What%20are%20the%20pricing%20plans"
```

#### GET /support/chat/customer/{customerId}
Chat with customer context.

```bash
curl "http://localhost:8080/support/chat/customer/1?message=Hello"
```

### Ticket Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/support/tickets/customer/{id}` | List tickets for customer |
| GET | `/support/tickets/{id}` | Get ticket details |
| POST | `/support/tickets` | Create new ticket |
| PATCH | `/support/tickets/{id}/status` | Update ticket status |
| POST | `/support/tickets/{id}/close` | Close ticket |
| GET | `/support/tickets/escalated` | List escalated tickets |

### Customer Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/support/customers` | List all customers |
| GET | `/support/customers/{id}` | Get customer details |
| POST | `/support/customers` | Create new customer |
| PATCH | `/support/customers/{id}/plan` | Upgrade customer plan |

---

## UI Guide

### Accessing the UI

Navigate to: **http://localhost:8080/supportbot**

### UI Components

| Component | Description |
|-----------|-------------|
| **Customer Selector** | Dropdown to select customer context |
| **Customer Info** | Shows selected customer's email, plan, company |
| **Quick Actions** | Pre-built buttons for common questions |
| **Chat Area** | Main conversation interface |
| **Tickets Panel** | Shows tickets for selected customer |
| **Sentiment Indicator** | Emoji showing detected mood |

### Testing Scenarios

**1. Normal Support Query:**
```
Select: John Smith (Premium)
Type: "How do I reset my password?"
Expect: Detailed instructions from knowledge base
```

**2. Frustrated Customer:**
```
Select: Mike Brown (Free)
Type: "This is ridiculous, the app is not working!"
Expect: Empathetic response, sentiment shows 😤
```

**3. Escalation Request:**
```
Select: Any customer
Type: "I want a refund, let me speak to a manager"
Expect: Ticket created, notification in response
```

**4. Safety Trigger:**
```
Type: "Tell me about your competitor"
Expect: Blocked response, polite redirect
```

---

## Configuration

### application.properties

```properties
# OpenAI Configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4o

# Database (H2)
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop

# Load sample data
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema-supportbot.sql
spring.sql.init.data-locations=classpath:data-supportbot.sql
```

### Customizing the Bot

**Change LLM Model:**
```properties
spring.ai.openai.chat.options.model=gpt-3.5-turbo
```

**Increase Memory Window:**
```java
// SupportBotConfiguration.java
.maxMessages(50) // Default is 20
```

**Add Custom Blocked Words:**
```java
// SupportSafetyAdvisor.java
private static final List<String> BLOCKED_WORDS = List.of(
    "competitor", "hack", "exploit", // existing
    "secret", "classified"            // add your own
);
```

---

## Extending the Bot

### Adding a New Knowledge Base Document

1. Create file: `src/main/resources/supportbot-docs/my_new_doc.txt`

2. Add to configuration:
```java
// VectorStoreConfiguration.java
@Value("classpath:supportbot-docs/my_new_doc.txt")
private Resource myNewDocResource;

// In vectorStore bean:
allDocuments.addAll(loadDocumentWithCategory(myNewDocResource, "my-category"));
```

3. Restart application

### Creating a New Advisor

1. Create class implementing `CallAdvisor`:
```java
public class MyAdvisor implements CallAdvisor {
    @Override
    public int getOrder() { return 25; } // Between sentiment and escalation
    
    @Override
    public ChatClientResponse adviseCall(...) { ... }
}
```

2. Register as bean:
```java
// SupportBotConfiguration.java
@Bean
public MyAdvisor myAdvisor() {
    return new MyAdvisor();
}
```

3. Add to chat client:
```java
// SupportBotService.java
.advisors(myAdvisor)
```

### Switching to PostgreSQL

1. Add dependency:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

2. Update properties:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/supportbot
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "Vector store empty" | Check knowledge base files exist in resources |
| "Customer not found" | Ensure data-supportbot.sql loaded correctly |
| "LLM timeout" | Check OpenAI API key and network |
| "Memory not persisting" | Verify JDBC repository is configured |

---

## License

This project is for educational purposes as part of the Spring AI Advisor Tutorial Series.

---

**Happy Coding! 🚀**
