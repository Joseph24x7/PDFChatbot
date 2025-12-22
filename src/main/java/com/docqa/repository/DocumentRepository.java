package com.docqa.repository;

import com.docqa.model.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {
    Optional<DocumentEntity> findByFileHash(String fileHash);

    // Text search methods
    List<DocumentEntity> findAllBy(TextCriteria criteria, Pageable pageable);

    // Filename search
    List<DocumentEntity> findByFileNameContainingIgnoreCase(String fileName, Pageable pageable);

    // Date range search
    List<DocumentEntity> findByUploadedAtAfter(LocalDateTime date, Pageable pageable);

    // Combined search
    List<DocumentEntity> findByFileNameContainingIgnoreCaseAndUploadedAtAfter(
        String fileName, LocalDateTime date, Pageable pageable);
}

