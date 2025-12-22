package com.docqa.mapper;

import com.docqa.dto.ChatMessageDto;
import com.docqa.dto.ChatSessionResponse;
import com.docqa.model.ChatSession;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatSessionMapper {

    public static ChatSessionResponse toResponse(ChatSession session, String currentResponse) {
        List<ChatMessageDto> messages = session.getMessages() != null ? session.getMessages().stream()
                .map(msg -> ChatMessageDto.builder()
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .build())
                .collect(Collectors.toList()) : List.of();

        return ChatSessionResponse.builder()
                .sessionId(session.getId())
                .documentId(session.getDocumentId())
                .documentName(session.getDocumentName())
                .messages(messages)
                .messageCount(session.getMessages() != null ? session.getMessages().size() : 0)
                .createdAt(session.getCreatedAt())
                .lastInteractionAt(session.getUpdatedAt())
                .currentResponse(currentResponse)
                .build();
    }

    public static ChatSessionResponse toResponse(ChatSession session) {
        return toResponse(session, null);
    }
}

