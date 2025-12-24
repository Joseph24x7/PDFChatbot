package com.docqa.service.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.docqa.dto.ChatSessionResponse;
import com.docqa.model.elasticsearch.ChatSessionDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@ConditionalOnBean(name = "elasticsearchClient")
public class ElasticsearchSearchService {

    private final ElasticsearchClient elasticsearchClient;

    @Value("${elasticsearch.index.sessions}")
    private String sessionsIndex;

    @Value("${elasticsearch.search.max-results}")
    private int maxResults;

    @Value("${elasticsearch.search.fuzzy-enabled}")
    private boolean fuzzyEnabled;

    @Value("${elasticsearch.search.fuzzy-distance}")
    private int fuzzyDistance;

    public ElasticsearchSearchService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public List<ChatSessionResponse> searchSessions(String queryText) {
        try {
            long startTime = System.currentTimeMillis();

            log.info("Searching Elasticsearch index '{}' for query: '{}' (fuzzy: {}, distance: {})",
                    sessionsIndex, queryText, fuzzyEnabled, fuzzyDistance);

            // Build search request
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(sessionsIndex)
                    .query(buildQuery(queryText))
                    .size(maxResults));

            // Execute search
            SearchResponse<ChatSessionDocument> response = elasticsearchClient.search(searchRequest, ChatSessionDocument.class);

            // Process results
            long totalHits = response.hits().total() != null ? response.hits().total().value() : response.hits().hits().size();
            log.info("Elasticsearch search completed in {}ms, found {} hits for query '{}'", System.currentTimeMillis() - startTime, totalHits, queryText);
            if (totalHits == 0) {
                log.warn("No results found for query '{}'. Index may be empty or query doesn't match any documents.", queryText);
            }

            // Map hits to response DTOs
            return response.hits().hits().stream().map(this::toSessionResponse).toList();

        } catch (Exception e) {
            log.error("Error searching Elasticsearch", e);
            return new ArrayList<>();
        }
    }

    private Query buildQuery(String queryText) {
        return Query.of(q -> q
                .bool(b -> b
                        .should(s -> s
                                .multiMatch(m -> m
                                        .query(queryText)
                                        .fields(
                                                "documentName",
                                                "documentName._2gram",
                                                "documentName._3gram"
                                        )
                                        .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BoolPrefix)
                                )
                        )
                )
        );
    }

    private ChatSessionResponse toSessionResponse(Hit<ChatSessionDocument> hit) {
        ChatSessionDocument doc = hit.source();
        if (doc == null) {
            return null;
        }

        // Use document name directly (highlighting removed to fix media-type header error)
        String displayName = doc.getDocumentName();

        return ChatSessionResponse.builder()
                .sessionId(doc.getId())
                .documentId(doc.getDocumentId())
                .documentName(displayName)
                .createdAt(doc.getCreatedAt())
                .lastInteractionAt(doc.getLastInteractionAt())
                .messageCount(doc.getMessageCount() != null ? doc.getMessageCount() : 0)
                .build();
    }
}

