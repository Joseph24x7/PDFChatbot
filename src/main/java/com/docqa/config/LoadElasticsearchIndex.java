package com.docqa.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnBean(name = "elasticsearchClient")
@RequiredArgsConstructor
public class LoadElasticsearchIndex {

    private final ElasticsearchClient elasticsearchClient;

    @Value("${elasticsearch.index.sessions}")
    private String sessionsIndex;

    @PostConstruct
    public void initializeIndices() {
        try {
            createIndexIfNotExists(sessionsIndex);
        } catch (Exception e) {
            log.error("Error initializing Elasticsearch indices", e);
        }
    }

    public void createIndexIfNotExists(String indexName) {
        try {
            // Check if index exists
            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(indexName)))
                    .value();

            if (!exists) {
                log.info("Creating Elasticsearch index: {}", indexName);

                elasticsearchClient.indices().create(CreateIndexRequest.of(c -> c
                        .index(indexName)
                        .mappings(m -> m
                                .properties("id", p -> p.keyword(k -> k))
                                .properties("documentName", p -> p
                                        .searchAsYouType(s -> s)
                                )
                                .properties("documentId", p -> p.keyword(k -> k))
                                .properties("createdAt", p -> p.date(d -> d))
                                .properties("lastInteractionAt", p -> p.date(d -> d))
                                .properties("messageCount", p -> p.integer(i -> i))
                                .properties("lastMessage", p -> p.text(t -> t))
                        )
                ));

                log.info("Successfully created index: {}", indexName);
            } else {
                log.info("Index already exists: {}", indexName);
            }
        } catch (Exception e) {
            log.error("Error creating index: {}", indexName, e);
            throw new RuntimeException("Failed to create Elasticsearch index: " + indexName, e);
        }
    }
}

