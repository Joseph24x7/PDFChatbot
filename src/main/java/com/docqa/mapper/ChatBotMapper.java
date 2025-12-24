package com.docqa.mapper;

import com.docqa.dto.ChatMessageDto;
import com.docqa.dto.ChatSessionResponse;
import com.docqa.model.ChatMessage;
import com.docqa.model.ChatSession;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatBotMapper {

    public static ChatSessionResponse toResponse(ChatSession session, String currentResponse) {

        ChatSessionResponse response = new ChatSessionResponse();

        // Map messages
        List<ChatMessageDto> messages = new ArrayList<>();
        if (!CollectionUtils.isEmpty(session.getMessages())) {
            for (ChatMessage msg : session.getMessages()) {
                ChatMessageDto dto = new ChatMessageDto(msg.getRole(), msg.getContent());
                messages.add(dto);
            }
        }

        // Populate response fields
        response.setSessionId(session.getId());
        response.setDocumentId(session.getDocumentId());
        response.setDocumentName(session.getDocumentName());
        response.setMessages(messages);
        response.setMessageCount(messages.size());
        response.setCreatedAt(session.getCreatedAt());
        response.setLastInteractionAt(session.getUpdatedAt());
        response.setCurrentResponse(currentResponse);

        return response;
    }

    public static ChatSessionResponse toResponse(ChatSession session) {
        return toResponse(session, null);
    }

}

