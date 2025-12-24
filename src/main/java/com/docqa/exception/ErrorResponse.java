package com.docqa.exception;

import java.time.LocalDateTime;

public record ErrorResponse(int status,
                            String message,
                            String details,
                            LocalDateTime timestamp) {
}

