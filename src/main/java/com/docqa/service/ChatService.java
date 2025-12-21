package com.docqa.service;

import com.docqa.model.ChatMessage;
import com.docqa.model.ChatSession;
import com.docqa.model.DocumentEntity;
import com.docqa.repository.ChatSessionRepository;
import com.docqa.repository.DocumentRepository;
import com.docqa.util.PromptBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Slf4j
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final DocumentRepository documentRepository;
    private final OllamaService ollamaService;

    public ChatService(ChatSessionRepository chatSessionRepository,
                      DocumentRepository documentRepository,
                      OllamaService ollamaService) {
        this.chatSessionRepository = chatSessionRepository;
        this.documentRepository = documentRepository;
        this.ollamaService = ollamaService;
    }

    /**
     * Start a new chat session for a document
     */
    public ChatSession startChatSession(String documentId) {
        log.info("Starting new chat session for document: {}", documentId);

        // Get the document
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        // Create new chat session
        ChatSession session = new ChatSession(
                documentId,
                document.getFileName(),
                document.getExtractedText()
        );

        return chatSessionRepository.save(session);
    }

    /**
     * Get existing chat session
     */
    public ChatSession getChatSession(String sessionId) {
        log.info("Retrieving chat session: {}", sessionId);
        return chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Chat session not found: " + sessionId));
    }

    /**
     * Send a message to the chatbot and get a response (non-streaming)
     */
    public String chat(String sessionId, String userMessage) {
        log.info("Processing chat message for session: {}", sessionId);

        ChatSession session = getChatSession(sessionId);

        // Add user message to history
        ChatMessage userMsg = new ChatMessage("user", userMessage);
        session.addMessage(userMsg);

        // Build prompt with conversation context
        String prompt = PromptBuilder.buildContextualPrompt(session, userMessage);

        // Get response from Ollama
        String assistantResponse = ollamaService.generateText(prompt);
        log.info("Generated response from Ollama, length: {} characters", assistantResponse.length());

        // Add assistant response to history
        ChatMessage assistantMsg = new ChatMessage("assistant", assistantResponse);
        session.addMessage(assistantMsg);

        // Save updated session
        chatSessionRepository.save(session);

        return assistantResponse;
    }

    /**
     * Send a message to the chatbot with streaming response
     * @param sessionId The chat session ID
     * @param userMessage The user's message
     * @param onToken Callback for each token chunk
     * @param onComplete Callback when streaming completes with full response
     * @param onError Callback for errors
     */
    public void chatStream(String sessionId, String userMessage,
                          Consumer<String> onToken,
                          Consumer<String> onComplete,
                          Consumer<Throwable> onError) {
        log.info("Processing streaming chat message for session: {}", sessionId);

        try {
            ChatSession session = getChatSession(sessionId);

            // Add user message to history
            ChatMessage userMsg = new ChatMessage("user", userMessage);
            session.addMessage(userMsg);
            chatSessionRepository.save(session);

            // Build prompt with conversation context
            String prompt = PromptBuilder.buildContextualPrompt(session, userMessage);

            // Stream response from Ollama
            ollamaService.streamText(
                prompt,
                onToken,
                completeResponse -> {
                    // Add assistant response to history after streaming completes
                    ChatMessage assistantMsg = new ChatMessage("assistant", completeResponse);
                    session.addMessage(assistantMsg);
                    chatSessionRepository.save(session);

                    onComplete.accept(completeResponse);
                },
                onError
            );

        } catch (Exception e) {
            log.error("Error in chatStream", e);
            onError.accept(e);
        }
    }
}

