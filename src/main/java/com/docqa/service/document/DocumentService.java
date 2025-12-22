package com.docqa.service.document;

import com.docqa.model.DocumentEntity;
import com.docqa.repository.DocumentRepository;
import com.docqa.util.FileHashUtil;
import com.docqa.util.PDFExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public String uploadDocument(MultipartFile file) {
        log.info("Uploading document: {}, file size: {}", file.getOriginalFilename(), file.getSize());

        String fileHash = FileHashUtil.calculateFileHash(file);
        log.info("Calculated file hash: {}", fileHash);

        DocumentEntity document = documentRepository.findByFileHash(fileHash)
                .orElseGet(() -> createNewDocument(file, fileHash));

        document.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(document);

        log.info("Document saved with ID: {}", document.getId());
        return document.getId();
    }

    private DocumentEntity createNewDocument(MultipartFile file, String fileHash) {
        log.info("Duplicate PDF detected! File hash {} already exists. Reusing cached content from file: {}",
                fileHash, file.getOriginalFilename());

        String extractedText = PDFExtractor.extractTextFromPDF(file);
        log.info("Successfully extracted text from PDF, length: {} characters", extractedText.length());

        LocalDateTime now = LocalDateTime.now();

        return DocumentEntity.builder()
                .fileName(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .fileHash(fileHash)
                .extractedText(extractedText)
                .uploadedAt(now)
                .updatedAt(now)
                .build();
    }
}

