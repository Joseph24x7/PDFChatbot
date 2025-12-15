package com.docqa.controller;

import com.docqa.dto.ChatMessageDto;
import com.docqa.dto.ChatMessageRequest;
import com.docqa.dto.ChatSessionResponse;
import com.docqa.model.ChatSession;
import com.docqa.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Chat Management", description = "APIs for chatbot conversation with documents")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Send a message to the chatbot and get a response
     */
    @PostMapping(value = "/message", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ChatSessionResponse> sendMessage(@RequestBody ChatMessageRequest request) {
        log.info("Received chat message for session: {}", request.getSessionId());

        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            log.error("sendMessage: Session ID is required");
            return ResponseEntity.badRequest().build();
        }

        if (request.getQuestion() == null || request.getQuestion().isEmpty()) {
            log.error("Question is required");
            return ResponseEntity.badRequest().build();
        }

        try {
            String response = chatService.chat(request.getSessionId(), request.getQuestion());
            ChatSession session = chatService.getChatSession(request.getSessionId());

            List<ChatMessageDto> messages = session.getMessages().stream()
                    .map(msg -> ChatMessageDto.builder()
                            .role(msg.getRole())
                            .content(msg.getContent())
                            .build())
                    .collect(Collectors.toList());

            ChatSessionResponse sessionResponse = ChatSessionResponse.builder()
                    .sessionId(session.getId())
                    .documentId(session.getDocumentId())
                    .documentName(session.getDocumentName())
                    .messages(messages)
                    .currentResponse(response)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(sessionResponse);
        } catch (RuntimeException e) {
            log.error("Error processing chat message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get chat session with full conversation history
     */
    @GetMapping(value = "/{sessionId}", produces = "application/json")
    public ResponseEntity<ChatSessionResponse> getChatSession(@PathVariable String sessionId) {
        log.info("Retrieving chat session: {}", sessionId);

        if (sessionId == null || sessionId.isEmpty()) {
            log.error("getChatSession: Session ID is required");
            return ResponseEntity.badRequest().build();
        }

        try {
            ChatSession session = chatService.getChatSession(sessionId);

            List<ChatMessageDto> messages = session.getMessages().stream()
                    .map(msg -> ChatMessageDto.builder()
                            .role(msg.getRole())
                            .content(msg.getContent())
                            .build())
                    .collect(Collectors.toList());

            ChatSessionResponse response = ChatSessionResponse.builder()
                    .sessionId(session.getId())
                    .documentId(session.getDocumentId())
                    .documentName(session.getDocumentName())
                    .messages(messages)
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error retrieving chat session", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

