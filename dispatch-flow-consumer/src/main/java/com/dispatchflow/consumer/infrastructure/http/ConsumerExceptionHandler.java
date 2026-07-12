package com.dispatchflow.consumer.infrastructure.http;

import com.dispatchflow.shared.domain.DomainError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ConsumerExceptionHandler {

    @ExceptionHandler(DomainError.class)
    public ResponseEntity<Map<String, String>> handleDomainError(DomainError error) {
        HttpStatus status = error.getType() == DomainError.Type.NOT_FOUND
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("message", error.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException error) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("message", "Failed to process queued guide: " + error.getMessage()));
    }
}
