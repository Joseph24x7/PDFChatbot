package com.docqa.controller;

import com.docqa.dto.ChatSessionResponse;
import com.docqa.service.elasticsearch.ElasticsearchSearchService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@ConditionalOnBean(ElasticsearchSearchService.class)
public class SearchWebSocketController {

    private final ElasticsearchSearchService elasticsearchSearchService;

    public SearchWebSocketController(ElasticsearchSearchService elasticsearchSearchService) {
        this.elasticsearchSearchService = elasticsearchSearchService;
    }

    @MessageMapping("/search/sessions")
    @SendToUser("/queue/search/sessions")
    public Map<String, Object> searchSessions(SearchRequest request) {
        log.info("WebSocket search request: query='{}', token={}", request.getQuery(), request.getToken());

        try {
            // Use Elasticsearch for fast, fuzzy search
            List<ChatSessionResponse> results = elasticsearchSearchService.searchSessions(request.getQuery());

            log.info("Elasticsearch returned {} results for token {}", results.size(), request.getToken());

            return Map.of(
                    "results", results,
                    "token", request.getToken() != null ? request.getToken() : 0
            );
        } catch (Exception e) {
            log.error("Error processing search request", e);
            return Map.of(
                    "results", List.of(),
                    "token", request.getToken() != null ? request.getToken() : 0
            );
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        private String query;
        private Integer token;
    }
}

