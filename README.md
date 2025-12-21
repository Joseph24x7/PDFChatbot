
# Document Q&A System

An intelligent document analysis platform that enables conversational AI-powered Q&A over PDF documents with real-time streaming responses.

## Overview

### What is this solution?

This application provides an interactive chatbot interface for querying PDF documents using Large Language Models (LLMs). Unlike traditional document search or simple keyword matching, this system understands context, maintains conversation history, and provides intelligent answers based on document content.

**Key Advantages:**
- **Contextual Understanding**: Uses LLM to comprehend complex queries and document semantics rather than simple text matching
- **Conversational Memory**: Maintains session-based chat history for follow-up questions and contextual conversations
- **Real-time Streaming**: Delivers ChatGPT-like token-by-token response streaming for better UX
- **Privacy-First**: Runs entirely on local infrastructure using Ollama - no data sent to external APIs
- **Single-Port Deployment**: Unified frontend-backend deployment simplifies architecture and reduces infrastructure overhead

## Technology Stack

### Backend: Spring Boot (Java 21)

**Why Spring Boot?**
- **Enterprise-grade**: Production-ready with extensive ecosystem for monitoring, security, and scalability
- **Native AI Integration**: Spring AI framework provides seamless LLM integration with consistent abstractions
- **WebSocket Support**: Built-in STOMP/WebSocket for bi-directional real-time communication
- **MongoDB Integration**: Spring Data MongoDB offers reactive, scalable document persistence
- **Ecosystem Maturity**: Extensive tooling, testing support, and community resources

**Core Technologies:**
- Spring Boot 3.5.7 with Java 21 for modern language features and performance
- Spring AI for LLM orchestration (abstraction over multiple LLM providers)
- Spring WebSocket/STOMP for real-time streaming responses
- Spring Data MongoDB for chat session and document metadata persistence
- Apache PDFBox for PDF text extraction

### Frontend: React + Vite

**Why React + Vite?**
- **Developer Experience**: Vite offers instant hot module replacement and optimized builds
- **Component Reusability**: React's component model enables maintainable UI architecture
- **Rich Ecosystem**: Extensive libraries for WebSocket clients, UI components, and state management
- **Performance**: Vite's ES module-based dev server and optimized production builds
- **Modern Standards**: Supports latest JavaScript/TypeScript features out of the box

**Core Technologies:**
- React 18 for component-based UI development
- Vite for blazing-fast builds and development server
- STOMP.js + SockJS for WebSocket client implementation
- Axios for REST API communication

### Infrastructure

**Ollama (Local LLM Runtime):**
- Privacy-focused: All processing happens locally
- No API costs or rate limits
- Model flexibility: Supports llama3.1, mistral, qwen2.5, etc.
- Streaming-native API for real-time responses

**MongoDB:**
- Document-oriented structure matches chat session/message hierarchy
- Schema flexibility for evolving data models
- Horizontal scalability for production deployments
- Native support for complex nested documents

**Docker Compose:**
- Simplified multi-service orchestration
- Consistent development and production environments
- Easy dependency management (Ollama, MongoDB, application)

## API Architecture

### Flow Overview

```
User → Frontend → REST/WebSocket → Spring Boot → Ollama LLM
                        ↓                           ↓
                   MongoDB ← Document Metadata & Chat Sessions
```

### Endpoints

#### 1. Document Upload
**`POST /api/v1/documents/upload`**

**Purpose**: Upload PDF, extract text, create chat session, generate initial summary

**Flow**:
1. Client uploads PDF via multipart/form-data
2. Backend validates file type and size (max 1MB)
3. PDFBox extracts text content
4. Document metadata saved to MongoDB
5. Chat session initialized with document context
6. Returns `sessionId` and `documentId` for subsequent queries

**Response**:
```json
{
  "sessionId": "sess-abc123",
  "documentId": "doc-xyz789",
  "documentName": "report.pdf",
  "response": "Document loaded successfully!"
}
```

#### 2. REST Chat (Synchronous)
**`POST /api/v1/chat/message`**

**Purpose**: Send question and receive complete response (backward compatibility)

**Flow**:
1. Client sends question with `sessionId`
2. Backend retrieves chat session and document from MongoDB
3. Constructs prompt with document context + conversation history
4. Calls Ollama LLM API (blocking call)
5. Saves user question and AI response to MongoDB
6. Returns complete response

**Response**:
```json
{
  "sessionId": "sess-abc123",
  "documentId": "doc-xyz789",
  "messages": [...],
  "currentResponse": "The document discusses..."
}
```

#### 3. WebSocket Chat (Streaming)
**`WS ws://localhost:8080/ws`**

**Purpose**: Real-time streaming responses with token-level granularity

**Flow**:
1. Client establishes WebSocket connection via STOMP protocol
2. Client subscribes to `/topic/chat/{sessionId}`
3. Client sends message to `/app/chat/message`
4. Backend processes asynchronously:
   - Sends "start" message
   - Streams each token as it's generated from Ollama
   - Sends "chunk" messages incrementally
   - Sends "end" message with full response
5. Frontend renders tokens in real-time (ChatGPT-like typing effect)

**Message Types**:
- `message`: User message echo
- `start`: Stream initialization
- `chunk`: Individual token/word (multiple)
- `end`: Stream completion with full response
- `error`: Error notification

#### 4. Session Retrieval
**`GET /api/v1/chat/{sessionId}`**

**Purpose**: Retrieve conversation history for session restoration

**Flow**:
1. Client requests session by ID
2. Backend fetches from MongoDB
3. Returns all messages and document metadata

## Quick Start

### Docker Compose (Recommended)
```bash
docker-compose up -d
# Wait 1-2 minutes for Ollama model download
# Open http://localhost:8080
```

### Manual Setup
```bash
# 1. Start Ollama
docker run -d -p 11434:11434 ollama/ollama
docker exec ollama ollama pull llama3.1:8b

# 2. Start MongoDB
docker run -d -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=example \
  mongo:7.0

# 3. Build and Run
mvn clean package
java -jar target/PDFChatBot.jar
```

## Architecture Highlights

**Single-Port Deployment**: Frontend built into Spring Boot JAR and served from root path - simplifies reverse proxy configuration and reduces operational complexity.

**Session-Based Context**: Each uploaded document gets isolated chat session, enabling multiple concurrent document conversations without context bleeding.

**Streaming Architecture**: WebSocket-based streaming provides superior UX over polling or SSE, with STOMP protocol enabling bidirectional communication and subscription management.

**LLM Abstraction**: Spring AI abstracts LLM provider details, enabling easy swapping between Ollama, OpenAI, Azure OpenAI, or other providers without business logic changes.

---

**Built with Spring Boot 3.5.7 • React 18 • Ollama • MongoDB**
