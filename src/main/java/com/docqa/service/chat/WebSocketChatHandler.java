package com.docqa.service.chat;

import com.docqa.dto.ChatMessageDto;
import com.docqa.dto.ChatMessageRequest;
import com.docqa.validator.ChatRequestValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebSocketChatHandler {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketChatHandler(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    public void handleChatMessage(ChatMessageRequest request) {
        log.info("Received WebSocket chat message for session: {}", request.getSessionId());

        try {
            ChatRequestValidator.validateChatMessageRequest(request);

            String messageId = java.util.UUID.randomUUID().toString();

            sendUserMessageConfirmation(request.getSessionId(), request.getQuestion(), messageId);
            sendStreamingStartSignal(request.getSessionId(), messageId);
            processStreamingChat(request.getSessionId(), request.getQuestion(), messageId);

        } catch (RuntimeException e) {
            log.error("Error processing chat message", e);
            sendErrorMessage(request.getSessionId(), "Error processing message: " + e.getMessage());
        }
    }

    private void sendUserMessageConfirmation(String sessionId, String question, String messageId) {
        ChatMessageDto userMsg = ChatMessageDto.builder()
                .role("user")
                .content(question)
                .type("message")
                .messageId(messageId)
                .build();
        messagingTemplate.convertAndSend("/topic/chat/" + sessionId, userMsg);
    }

    private void sendStreamingStartSignal(String sessionId, String messageId) {
        ChatMessageDto startMsg = ChatMessageDto.builder()
                .role("assistant")
                .content("")
                .type("start")
                .messageId(messageId)
                .build();
        messagingTemplate.convertAndSend("/topic/chat/" + sessionId, startMsg);
    }

    private void processStreamingChat(String sessionId, String question, String messageId) {
        chatService.chatStream(
            sessionId,
            question,
            chunk -> sendChunk(sessionId, chunk, messageId),
            fullResponse -> sendComplete(sessionId, fullResponse, messageId),
            error -> handleStreamingError(sessionId, error)
        );
    }

    private void sendChunk(String sessionId, String chunk, String messageId) {
        ChatMessageDto chunkMsg = ChatMessageDto.builder()
                .role("assistant")
                .content(chunk)
                .type("chunk")
                .messageId(messageId)
                .build();
        messagingTemplate.convertAndSend("/topic/chat/" + sessionId, chunkMsg);
    }

    private void sendComplete(String sessionId, String fullResponse, String messageId) {
        ChatMessageDto endMsg = ChatMessageDto.builder()
                .role("assistant")
                .content(fullResponse)
                .type("end")
                .messageId(messageId)
                .build();
        messagingTemplate.convertAndSend("/topic/chat/" + sessionId, endMsg);
        log.info("Streaming completed for session: {}", sessionId);
    }

    private void handleStreamingError(String sessionId, Throwable error) {
        log.error("Error during streaming", error);
        sendErrorMessage(sessionId, "Error processing message: " + error.getMessage());
    }

    private void sendErrorMessage(String sessionId, String errorMessage) {
        ChatMessageDto errorMsg = ChatMessageDto.builder()
                .role("error")
                .content(errorMessage)
                .type("error")
                .build();
        messagingTemplate.convertAndSend("/topic/chat/" + sessionId, errorMsg);
    }
}

