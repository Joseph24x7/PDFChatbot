package com.docqa.mapper;

import com.docqa.dto.ChatSessionResponse;
import com.docqa.model.DocumentEntity;
import com.docqa.util.PDFExtractor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DocumentMapper {

    public static DocumentEntity createNewDocument(MultipartFile file, String fileHash) {
        log.info("Duplicate PDF detected! File hash {} already exists. Reusing cached content from file: {}",
                fileHash, file.getOriginalFilename());

        String extractedText = PDFExtractor.extractTextFromPDF(file);
        log.info("Successfully extracted text from PDF, length: {} characters", extractedText.length());

        LocalDateTime now = LocalDateTime.now();

        return DocumentEntity.builder()
                .fileName(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .fileHash(fileHash)
                .extractedText(extractedText)
                .uploadedAt(now)
                .updatedAt(now)
                .build();
    }

    public static Map<String, Object> buildSearchResponse(
            List<ChatSessionResponse> results,
            Integer token
    ) {
        return Map.of(
                "results", results,
                "token", token != null ? token : 0
        );
    }


}
