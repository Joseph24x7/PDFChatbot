package com.docqa.controller;

import com.docqa.dto.DocumentUploadResponse;
import com.docqa.model.ChatSession;
import com.docqa.service.chat.ChatService;
import com.docqa.service.document.DocumentService;
import com.docqa.validator.ChatBotValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DocumentController {

    private final DocumentService documentService;
    private final ChatService chatService;
    private final long maxFileSize;

    public DocumentController(DocumentService documentService,
                            ChatService chatService,
                            @Value("${app.max-file-size}") long maxFileSize) {
        this.documentService = documentService;
        this.chatService = chatService;
        this.maxFileSize = maxFileSize;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(@RequestParam("file") MultipartFile file,
                                                                 @RequestParam(value = "query", required = false) String query) {

        log.info("Received document upload request: {}", file.getOriginalFilename());

        // Validate the uploaded file
        ChatBotValidator.validateFile(file, maxFileSize);

        // Process the document upload
        String documentId = documentService.uploadDocument(file);

        // Start a new chat session for the uploaded document
        ChatSession session = chatService.startChatSession(documentId);

        // Start chat with initial query if provided
        String initialResponse = chatService.chat(session.getId(), query);

        // Build and return the response
        return ResponseEntity.status(HttpStatus.CREATED).body(new DocumentUploadResponse(query, initialResponse, session.getId(), documentId));

    }

}

