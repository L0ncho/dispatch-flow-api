package com.dispatchflow.guides.domain.valueobjects;

import com.dispatchflow.shared.domain.DomainError;

public record Email(String value) {

    public Email {
        if (value == null || value.isBlank()) {
            throw DomainError.validation("Email must not be blank");
        }
        if (!value.contains("@") || !value.contains(".")) {
            throw DomainError.validation("Email format is invalid");
        }
    }

    public static Email create(String value) {
        return new Email(value);
    }
}
