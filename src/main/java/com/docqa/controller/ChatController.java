package com.docqa.controller;

import com.docqa.dto.ChatMessageRequest;
import com.docqa.dto.ChatSessionResponse;
import com.docqa.mapper.ChatSessionMapper;
import com.docqa.model.ChatSession;
import com.docqa.service.chat.ChatService;
import com.docqa.validator.ChatRequestValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/message", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ChatSessionResponse> sendMessage(@RequestBody ChatMessageRequest request) {
        log.info("Received REST chat message for session: {}", request.getSessionId());

        ChatRequestValidator.validateChatMessageRequest(request);

        String response = chatService.chat(request.getSessionId(), request.getQuestion());
        ChatSession session = chatService.getChatSession(request.getSessionId());
        ChatSessionResponse sessionResponse = ChatSessionMapper.toResponse(session, response);

        return ResponseEntity.ok(sessionResponse);
    }

    @GetMapping(value = "/{sessionId}", produces = "application/json")
    public ResponseEntity<ChatSessionResponse> getChatSession(@PathVariable String sessionId) {
        log.info("Retrieving chat session: {}", sessionId);

        ChatRequestValidator.validateSessionId(sessionId);

        ChatSession session = chatService.getChatSession(sessionId);
        ChatSessionResponse response = ChatSessionMapper.toResponse(session);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/sessions", produces = "application/json")
    public ResponseEntity<List<ChatSessionResponse>> getAllSessions() {
        log.info("Retrieving all chat sessions");

        List<ChatSession> sessions = chatService.getAllSessions();
        List<ChatSessionResponse> responses = sessions.stream()
                .map(ChatSessionMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}


