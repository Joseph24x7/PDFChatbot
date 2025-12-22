package com.docqa.model.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "chat-sessions")
public class ChatSessionDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String documentName;

    @Field(type = FieldType.Keyword)
    private String documentId;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime lastInteractionAt;

    @Field(type = FieldType.Integer)
    private Integer messageCount;

    @Field(type = FieldType.Text)
    private String lastMessage;
}

