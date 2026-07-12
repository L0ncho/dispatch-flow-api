package com.dispatchflow.guides.infrastructure.http;

import com.dispatchflow.shared.domain.DomainError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainError.class)
    public ResponseEntity<ErrorResponse> handleDomainError(DomainError error) {
        HttpStatus status = switch (error.getType()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case OTHER -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status).body(ErrorResponse.ofMessage(error.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(ErrorResponse.ofValidation("Validation failed", fieldErrors));
    }
}
