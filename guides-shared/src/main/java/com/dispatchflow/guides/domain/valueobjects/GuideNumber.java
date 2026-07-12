package com.dispatchflow.guides.domain.valueobjects;

import com.dispatchflow.shared.domain.DomainError;

public record GuideNumber(String value) {

    public GuideNumber {
        if (value == null || value.isBlank()) {
            throw DomainError.validation("Guide number must not be blank");
        }
    }

    public static GuideNumber create(String value) {
        return new GuideNumber(value);
    }
}
