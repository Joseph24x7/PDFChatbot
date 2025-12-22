package com.docqa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSessionResponse {
    private String sessionId;
    private String documentId;
    private String documentName;
    private List<ChatMessageDto> messages;
    private Integer messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastInteractionAt;
    private String currentResponse;  // Latest assistant response
}

