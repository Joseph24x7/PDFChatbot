package com.docqa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    private String id;
    private Role role;              // "user" or "assistant"
    private String content;           // Message content
    private LocalDateTime timestamp;

    public ChatMessage(Role role, String content) {
        this.id = java.util.UUID.randomUUID().toString();
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
}

