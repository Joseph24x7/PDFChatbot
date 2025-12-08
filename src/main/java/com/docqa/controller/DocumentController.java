package com.docqa.controller;

import com.docqa.dto.DocumentUploadResponse;
import com.docqa.service.DocumentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Document Management", description = "APIs for document upload, summarization, and Q&A")
public class DocumentController {

    private final DocumentService documentService;

    private final long maxFileSize;

    public DocumentController(DocumentService documentService, @Value("${app.max-file-size:1048576}") long maxFileSize) {
        this.documentService = documentService;
        this.maxFileSize = maxFileSize;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(@RequestParam("file") MultipartFile file,
                                                                 @RequestParam(value = "query", required = false) String query) {

        log.info("Received document upload request: {}", file.getOriginalFilename());

        validateFile(file);

        String response = documentService.uploadAndProcessDocument(file, query);

        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentUploadResponse.builder().query(query).response(response).build());
    }

    private void validateFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!MediaType.APPLICATION_PDF_VALUE.equals(file.getContentType())) {
            throw new IllegalArgumentException("File must be a PDF");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(String.format("File size exceeds maximum allowed size of %d bytes", maxFileSize));
        }

    }

}

