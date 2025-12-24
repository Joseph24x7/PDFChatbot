package com.docqa.validator;

import com.docqa.dto.ChatMessageRequest;
import com.docqa.exception.ValidationException;
import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatBotValidator {

    public static void validateFile(MultipartFile file, long maxFileSize) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!MediaType.APPLICATION_PDF_VALUE.equals(file.getContentType())) {
            throw new IllegalArgumentException("File must be a PDF");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(String.format("File size exceeds maximum allowed size of %d bytes", maxFileSize));
        }
    }

    public static void validateChatMessageRequest(ChatMessageRequest request) {

        if (Objects.isNull(request)) {
            throw new ValidationException("Request cannot be null");
        }

        if (StringUtils.isBlank(request.sessionId())) {
            throw new ValidationException("Session ID is required");
        }

        if (StringUtils.isBlank(request.question())) {
            throw new ValidationException("Question is required");
        }
    }

    public static void validateSessionId(String sessionId) {
        if (StringUtils.isBlank(sessionId)) {
            throw new ValidationException("Session ID is required");
        }
    }

}
