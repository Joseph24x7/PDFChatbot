package com.docqa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentEntity {

    @Id
    private String id;
    private String fileName;
    private String mimeType;
    private long fileSize;
    private String fileHash;
    private String extractedText;
    private Map<String, String> queryCache;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;

}

