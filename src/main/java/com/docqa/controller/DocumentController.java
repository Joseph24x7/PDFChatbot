package com.docqa.controller;

import com.docqa.dto.DocumentUploadResponse;
import com.docqa.model.ChatSession;
import com.docqa.service.chat.ChatService;
import com.docqa.service.document.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.docqa.validator.ChatBotValidator.validateFile;

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
                            @Value("${app.max-file-size:1048576}") long maxFileSize) {
        this.documentService = documentService;
        this.chatService = chatService;
        this.maxFileSize = maxFileSize;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(@RequestParam("file") MultipartFile file,
                                                                 @RequestParam(value = "query", required = false) String query) {

        log.info("Received document upload request: {}", file.getOriginalFilename());

        validateFile(file, maxFileSize);

        String documentId = documentService.uploadDocument(file);
        ChatSession session = chatService.startChatSession(documentId);

        String initialResponse;
        if (query != null && !query.trim().isEmpty()) {
            initialResponse = chatService.chat(session.getId(), query);
        } else {
            initialResponse = "Document loaded successfully! Ask me any questions about the document.";
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
            DocumentUploadResponse.builder()
                .query(query)
                .response(initialResponse)
                .sessionId(session.getId())
                .documentId(documentId)
                .build()
        );
    }

}

