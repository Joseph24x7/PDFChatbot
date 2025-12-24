# Document Q&A System 🤖

An AI-powered document analysis platform with real-time chat and intelligent search capabilities.

## ✨ Features

- 📄 **PDF Upload & Analysis** - Upload PDFs and chat with your documents
- 🔍 **Smart Search** - WebSocket & Elasticsearch-powered fuzzy search with autocomplete
- 🔐 **Privacy First** - 100% local processing with Ollama (no external APIs)
- 🎯 **Session Management** - Multiple concurrent document conversations

## 🛠️ Technology Stack

**Backend**
- Spring Boot 3.5.7 + Java 21
- Spring AI (LLM integration)
- Elasticsearch 8.11 (search)
- MongoDB 7.0 (storage)
- WebSocket/STOMP (real-time)
- Apache PDFBox (PDF parsing)

**Frontend**
- React 18 + Vite
- STOMP.js + SockJS (WebSocket client)
- Custom design system

**Infrastructure**
- Docker Compose
- Ollama (Llama 3.1 8B model)
- Single-port deployment

## 📐 Architecture & Data Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                           USER BROWSER                              │
│                     React 18 + Vite Frontend                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐   │
│  │ Session List │  │ Document     │  │ Chat Interface           │   │
│  │ (Search)     │  │ Upload       │  │ (WebSocket Streaming)    │   │
│  └──────────────┘  └──────────────┘  └──────────────────────────┘   │
└─────────────┬────────────┬────────────────────┬────────────────── ──┘
              │            │                    │
              │  WebSocket │ REST API           │ REST API
              │ (Search)   │ (Upload)           │ (Real-time Chat)
              │            │                    │
┌─────────────▼────────────▼────────────────────▼────────────────────┐
│                    SPRING BOOT APPLICATION                         │
│                      (Single Port: 8080)                           │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                     Controllers Layer                        │  │
│  │  • WebSocketController  • DocumentController • ChatController│  │
│  └────────────┬─────────────────────┬──────────────────┬────────┘  │
│               │                     │                  │           │
│  ┌────────────▼─────────────────────▼──────────────────▼─────────┐ │
│  │                      Service Layer                            │ │
│  │  • ChatService                                                │ │
│  │  • DocumentService                                            │ │    
│  │  • ElasticsearchSearchService                                 │ │
│  │                                                               │ │
│  └────────┬──────────────┬──────────────┬─────────────┬──────────┘ │
│           │              │              │             │            │
│  ┌────────▼────┐  ┌──────▼──────┐ ┌────▼─────┐ ┌────▼──────────┐   │
│  │  AI         │  │   PDFBox    │ │ MongoDB  │ │ Elasticsearch │   │
│  │ Integration │  │  Text       │ │ Repo     │ │    Client     │   │
│  └────────┬────┘  │  Extraction │ └────┬─────┘ └────┬──────────┘   │
│           │       └─────────────┘      │            │              │
└───────────┼────────────────────────────┼────────────┼──────────────┘
            │                            │            │
    ┌───────▼────────┐         ┌─────────▼──────┐ ┌──▼─────────────┐
    │  OLLAMA LLM    │         │    MONGODB     │ │ ELASTICSEARCH  │
    │  (Llama 3.1)   │         │                │ │                │
    └────────────────┘         └────────────────┘ └────────────────┘
```

### 🔄 Data Flow Scenarios

**1. Document Upload Flow:**
```
User → Upload PDF → DocumentController → PDFBox (Extract) 
     → Save to MongoDB → Create Session → Sync to Elasticsearch 
     → Return Session ID
```

**2. Chat Message Flow (WebSocket):**
```
User → Send Message → WebSocketController → ChatService 
     → Retrieve Context from MongoDB → Spring AI → Ollama LLM
     → Stream Tokens → WebSocket → User (Real-time Display)
     → Save to MongoDB → Sync to Elasticsearch
```

**3. Search Flow (Real-time):**
```
User → Type Query → WebSocketController → ElasticsearchSearchService
     → Fuzzy Search in Elasticsearch → Return Results → WebSocket
     → Display Matches (Live Update)
```

**4. Session Retrieval Flow:**
```
User → Select Session → REST API → ChatService 
     → Fetch from MongoDB → Return History → Display Chat
```

## 🚀 Quick Start

### Prerequisites
- 8GB RAM minimum (for Ollama LLM)
- Docker Desktop or Podman
- Maven 4.x+
- Java 21+
- IntelliJ IDEA or VSCode (optional)

### One-Command Setup

```bash
docker-compose up -d
```

Wait 1-2 minutes for services to start and Ollama model to download.

```bash
mvn clean install
```

# 4. Build & Run Application
```bash
spring-boot:run 
(or)
java -jar target/PDFChatBot.jar
```

Then open: **http://localhost:8080**

## 📖 How It Works

1. **Upload PDF** → System extracts text and creates a chat session
2. **Ask Questions** → LLM analyzes document context and responds
3. **Real-Time Streaming** → Responses stream token-by-token like ChatGPT
4. **Search Sessions** → Find past conversations with fuzzy search
5. **Continue Conversations** → Resume any chat session

## 📦 Project Structure

```
document-summary/
├── src/main/java/com/docqa/
│   ├── config/         # Spring & Elasticsearch config
│   ├── controller/     # REST & WebSocket endpoints
│   ├── service/        # Business logic & LLM integration
│   ├── repository/     # MongoDB repositories
│   └── model/          # Domain entities
├── frontend/src/
│   ├── components/     # React components
│   ├── api/           # API client
│   └── design-system.css  # UI styles
└── docker-compose.yml  # Infrastructure setup
```

## 🔍 Architecture Highlights

- **Single-Port Deployment** - Frontend served from Spring Boot
- **Async Streaming** - Non-blocking WebSocket responses
- **Search Indexing** - Auto-sync MongoDB → Elasticsearch
- **Session Isolation** - Each document gets separate context

## 📄 License

MIT License - Feel free to use for personal or commercial projects!

## 🤝 Contributing

Contributions welcome! Please open an issue or PR.

---

**Built with ❤️ using Spring Boot • React • Ollama • Elasticsearch • MongoDB**
