package com.docqa.repository;

import com.docqa.model.ChatSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {

    // Search by document name
    List<ChatSession> findByDocumentNameContainingIgnoreCase(String documentName, Pageable pageable);

    // Search within messages using MongoDB aggregation
    @Query("{ 'messages.content': { $regex: ?0, $options: 'i' } }")
    List<ChatSession> findByMessagesContentContaining(String content, Pageable pageable);

    // Date range search
    List<ChatSession> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);

    // Find by document ID
    List<ChatSession> findByDocumentId(String documentId);
}

