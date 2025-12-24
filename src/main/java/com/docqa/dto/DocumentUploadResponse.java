package com.docqa.dto;

public record DocumentUploadResponse(
        String query,
        String response,
        String sessionId,
        String documentId
) {
}






