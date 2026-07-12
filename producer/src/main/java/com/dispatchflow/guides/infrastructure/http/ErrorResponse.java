package com.dispatchflow.guides.infrastructure.http;

import java.util.Map;

public record ErrorResponse(String message, Map<String, String> fieldErrors) {

    public static ErrorResponse ofMessage(String message) {
        return new ErrorResponse(message, null);
    }

    public static ErrorResponse ofValidation(String message, Map<String, String> fieldErrors) {
        return new ErrorResponse(message, fieldErrors);
    }
}
