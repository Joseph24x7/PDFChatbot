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
import java.util.stream.Collectors;

@Service
@Slf4j
@ConditionalOnBean(name = "elasticsearchClient")
public class ElasticsearchSearchService {

    private final ElasticsearchClient elasticsearchClient;

    @Value("${elasticsearch.index.sessions:chat-sessions}")
    private String sessionsIndex;

    @Value("${elasticsearch.search.max-results:50}")
    private int maxResults;

    @Value("${elasticsearch.search.fuzzy-enabled:true}")
    private boolean fuzzyEnabled;

    @Value("${elasticsearch.search.fuzzy-distance:2}")
    private int fuzzyDistance;

    public ElasticsearchSearchService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public List<ChatSessionResponse> searchSessions(String queryText) {
        try {
            long startTime = System.currentTimeMillis();

            log.info("Searching Elasticsearch index '{}' for query: '{}' (fuzzy: {}, distance: {})",
                    sessionsIndex, queryText, fuzzyEnabled, fuzzyDistance);

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(sessionsIndex)
                    .query(buildQuery(queryText))
                    .size(maxResults)
            );

            SearchResponse<ChatSessionDocument> response = elasticsearchClient.search(
                    searchRequest,
                    ChatSessionDocument.class
            );

            long searchTime = System.currentTimeMillis() - startTime;
            long totalHits = response.hits().total() != null ? response.hits().total().value() : response.hits().hits().size();
            log.info("Elasticsearch search completed in {}ms, found {} hits for query '{}'",
                    searchTime, totalHits, queryText);

            if (totalHits == 0) {
                log.warn("No results found for query '{}'. Index may be empty or query doesn't match any documents.", queryText);
            }

            return response.hits().hits().stream()
                    .map(this::toSessionResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("index_not_found_exception")) {
                log.warn("Elasticsearch index '{}' not found. Please sync data or wait for index creation.", sessionsIndex);
            } else {
                log.error("Error searching Elasticsearch", e);
            }
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

