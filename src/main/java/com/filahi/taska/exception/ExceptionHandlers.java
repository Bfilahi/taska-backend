package com.filahi.taska.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class ExceptionHandlers {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExceptionResponse> handleException(ResponseStatusException exception) {
        return buildResponseEntity(exception, HttpStatus.valueOf(exception.getStatusCode().value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exception) {
        return buildResponseEntity(exception, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ExceptionResponse> buildResponseEntity(Exception exception, HttpStatus status) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                status.value(),
                exception.getMessage(),
                System.currentTimeMillis());
        return new ResponseEntity<>(exceptionResponse, status);
    }
}
