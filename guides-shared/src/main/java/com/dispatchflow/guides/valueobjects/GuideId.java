package com.dispatchflow.guides.domain.valueobjects;

import com.dispatchflow.shared.domain.DomainError;

import java.util.UUID;

public record GuideId(String value) {

    public GuideId {
        if (value == null || value.isBlank()) {
            throw DomainError.validation("Guide id must not be blank");
        }
    }

    public static GuideId generate() {
        return new GuideId(UUID.randomUUID().toString());
    }

    public static GuideId create(String value) {
        return new GuideId(value);
    }
}
