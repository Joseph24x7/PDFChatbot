package com.docqa.controller;

import com.docqa.dto.ChatSessionResponse;
import com.docqa.dto.SearchRequest;
import com.docqa.service.elasticsearch.ElasticsearchSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

import static com.docqa.mapper.DocumentMapper.buildSearchResponse;

@Controller
@Slf4j
@ConditionalOnBean(ElasticsearchSearchService.class)
@RequiredArgsConstructor
public class SearchWebSocketController {

    private final ElasticsearchSearchService elasticsearchSearchService;

    @MessageMapping("/search/sessions")
    @SendToUser("/queue/search/sessions")
    public Map<String, Object> searchSessions(SearchRequest request) {
        log.info("WebSocket search request: query='{}', token={}", request.query(), request.token());

        try {
            // Use Elasticsearch for fast, fuzzy search
            List<ChatSessionResponse> results = elasticsearchSearchService.searchSessions(request.query());
            log.info("Elasticsearch returned {} results for token {}", results.size(), request.token());

            // Build and return response
            return buildSearchResponse(results, request.token());

        } catch (Exception e) {

            log.error("Error processing search request", e);
            return buildSearchResponse(List.of(), request.token());

        }
    }

}

