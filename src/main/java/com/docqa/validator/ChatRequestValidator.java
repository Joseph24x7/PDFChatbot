package com.docqa.validator;

import com.docqa.dto.ChatMessageRequest;
import com.docqa.exception.ValidationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRequestValidator {

    public static void validateChatMessageRequest(ChatMessageRequest request) {
        if (request == null) {
            throw new ValidationException("Request cannot be null");
        }

        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            throw new ValidationException("Session ID is required");
        }

        if (request.getQuestion() == null || request.getQuestion().isEmpty()) {
            throw new ValidationException("Question is required");
        }
    }

    public static void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new ValidationException("Session ID is required");
        }
    }
}

