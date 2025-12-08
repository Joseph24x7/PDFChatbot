package com.docqa.repository;

import com.docqa.model.DocumentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {
    Optional<DocumentEntity> findByFileHash(String fileHash);
}

