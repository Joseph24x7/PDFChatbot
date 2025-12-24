package com.docqa.dto;

import com.docqa.model.Role;

public record ChatMessageDto(
        Role role,        // "user", "assistant"
        String content   // Message content
) {
}
