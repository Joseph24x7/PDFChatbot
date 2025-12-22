package com.docqa.service.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.docqa.model.ChatSession;
import com.docqa.model.elasticsearch.ChatSessionDocument;
import com.docqa.repository.ChatSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@ConditionalOnBean(name = "elasticsearchClient")
public class ElasticsearchSyncService {

    private final ChatSessionRepository mongoRepository;
    private final ElasticsearchClient elasticsearchClient;

    @Value("${elasticsearch.index.sessions:chat-sessions}")
    private String sessionsIndex;

    public ElasticsearchSyncService(ChatSessionRepository mongoRepository,
                                    ElasticsearchClient elasticsearchClient) {
        this.mongoRepository = mongoRepository;
        this.elasticsearchClient = elasticsearchClient;
    }

    @Async
    public void syncSession(ChatSession session) {
        try {
            ChatSessionDocument document = toDocument(session);

            log.info("Syncing session to Elasticsearch - ID: {}, DocumentName: {}, MessageCount: {}",
                    document.getId(), document.getDocumentName(), document.getMessageCount());

            // Save/index single document using Elasticsearch Java Client
            elasticsearchClient.index(i -> i
                .index(sessionsIndex)
                .id(document.getId())
                .document(document)
            );

            log.info("Successfully synced session {} to Elasticsearch", session.getId());
        } catch (Exception e) {
            log.error("Error syncing session {} to Elasticsearch: {}", session.getId(), e.getMessage(), e);
        }
    }

    public void syncAllSessions() {
        try {
            log.info("Starting full Elasticsearch sync");
            List<ChatSession> allSessions = mongoRepository.findAll();

            List<ChatSessionDocument> documents = allSessions.stream()
                    .map(this::toDocument)
                    .collect(Collectors.toList());

            // Bulk save using Elasticsearch Java Client
            elasticsearchClient.bulk(b -> {
                for (ChatSessionDocument doc : documents) {
                    b.operations(op -> op.index(idx -> idx
                        .index(sessionsIndex)
                        .id(doc.getId())
                        .document(doc)
                    ));
                }
                return b;
            });

            log.info("Synced {} sessions to Elasticsearch", documents.size());
        } catch (Exception e) {
            log.error("Error during full sync", e);
        }
    }

    @Async
    public void deleteSession(String sessionId) {
        try {
            // Delete using Elasticsearch Java Client
            elasticsearchClient.delete(d -> d
                .index(sessionsIndex)
                .id(sessionId)
            );

            log.debug("Deleted session {} from Elasticsearch", sessionId);
        } catch (Exception e) {
            log.error("Error deleting session from Elasticsearch: {}", e.getMessage());
        }
    }

    private ChatSessionDocument toDocument(ChatSession session) {
        return ChatSessionDocument.builder()
                .id(session.getId())
                .documentName(session.getDocumentName())
                .documentId(session.getDocumentId())
                .createdAt(session.getCreatedAt())
                .lastInteractionAt(session.getUpdatedAt())
                .messageCount(session.getMessages() != null ? session.getMessages().size() : 0)
                .lastMessage(session.getMessages() != null && !session.getMessages().isEmpty() ?
                        session.getMessages().getLast().getContent() : "")
                .build();
    }
}

