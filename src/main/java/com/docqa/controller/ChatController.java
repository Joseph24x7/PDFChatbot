package com.docqa.controller;

import com.docqa.dto.ChatMessageRequest;
import com.docqa.dto.ChatSessionResponse;
import com.docqa.mapper.ChatBotMapper;
import com.docqa.model.ChatSession;
import com.docqa.service.chat.ChatService;
import com.docqa.validator.ChatBotValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping(value = "/message", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ChatSessionResponse> sendMessage(@RequestBody ChatMessageRequest request) {

        log.info("Received REST chat message for session: {}", request.sessionId());

        // Validate request
        ChatBotValidator.validateChatMessageRequest(request);

        // Process chat message
        String response = chatService.chat(request.sessionId(), request.question());

        // Retrieve updated chat session
        ChatSession session = chatService.getChatSession(request.sessionId());

        // Map to response DTO
        return ResponseEntity.ok(ChatBotMapper.toResponse(session, response));
    }

    @GetMapping(value = "/{sessionId}", produces = "application/json")
    public ResponseEntity<ChatSessionResponse> getChatSession(@PathVariable String sessionId) {
        log.info("Retrieving chat session: {}", sessionId);

        // Validate session ID
        ChatBotValidator.validateSessionId(sessionId);

        // Retrieve chat session
        ChatSession session = chatService.getChatSession(sessionId);

        // Map to response DTO
        return ResponseEntity.ok(ChatBotMapper.toResponse(session));
    }

    @GetMapping(value = "/sessions", produces = "application/json")
    public ResponseEntity<List<ChatSessionResponse>> getAllSessions() {
        log.info("Retrieving all chat sessions");
        List<ChatSession> sessions = chatService.getAllSessions();
        return ResponseEntity.ok(sessions.stream().map(ChatBotMapper::toResponse).toList());
    }
}


