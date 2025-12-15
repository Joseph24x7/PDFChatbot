package com.docqa.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

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

}
