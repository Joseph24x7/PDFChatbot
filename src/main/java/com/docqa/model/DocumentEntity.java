package com.docqa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentEntity {

    @Id
    private String id;

    @TextIndexed(weight = 3)
    private String fileName;

    private String mimeType;
    private long fileSize;
    private String fileHash;

    @TextIndexed(weight = 1)
    private String extractedText;

    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;

}

