package com.docqa.service.document;

import com.docqa.model.DocumentEntity;
import com.docqa.repository.DocumentRepository;
import com.docqa.util.FileHashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static com.docqa.mapper.DocumentMapper.createNewDocument;

@Service
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public String uploadDocument(MultipartFile file) {

        log.info("Uploading document: {}, file size: {}", file.getOriginalFilename(), file.getSize());

        // Calculate file hash to check for duplicates
        String fileHash = FileHashUtil.calculateFileHash(file);
        log.info("Calculated file hash: {}", fileHash);

        // Check if document with the same hash already exists
        DocumentEntity document = documentRepository.findByFileHash(fileHash).orElseGet(() -> createNewDocument(file, fileHash));

        // Update the updatedAt timestamp
        document.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(document);

        log.info("Document saved with ID: {}", document.getId());
        return document.getId();
    }

}

