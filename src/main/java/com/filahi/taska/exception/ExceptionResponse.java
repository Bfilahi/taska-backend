package com.filahi.taska.exception;

public record ExceptionResponse(
        int status,
        String message,
        long timestamp
) {
}
