package com.docqa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private String role;      // "user", "assistant", "system"
    private String content;   // Message content
    private String type;      // "message", "start", "chunk", "end", "error"
    private String messageId; // Unique ID for tracking streaming messages
}

