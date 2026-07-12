package com.dispatchflow.shared.messaging;

import com.dispatchflow.shared.domain.DomainError;

import java.time.Instant;
import java.time.LocalDate;

public record GuideCreationMessage(
        String trackingId,
        String carrierName,
        String recipientName,
        String originAddress,
        String destinationAddress,
        String description,
        LocalDate dispatchDate,
        String ownerEmail,
        Instant requestedAt) {

    public static GuideCreationMessage create(
            String trackingId,
            String carrierName,
            String recipientName,
            String originAddress,
            String destinationAddress,
            String description,
            LocalDate dispatchDate,
            String ownerEmail,
            Instant requestedAt) {
        validateRequiredText(trackingId, "Tracking id");
        validateRequiredText(carrierName, "Carrier name");
        validateRequiredText(recipientName, "Recipient name");
        validateRequiredText(originAddress, "Origin address");
        validateRequiredText(destinationAddress, "Destination address");
        validateRequiredText(ownerEmail, "Owner email");
        if (dispatchDate == null) {
            throw DomainError.validation("Dispatch date is required");
        }
        if (requestedAt == null) {
            throw DomainError.validation("Requested at is required");
        }

        return new GuideCreationMessage(
                trackingId.trim(),
                carrierName.trim(),
                recipientName.trim(),
                originAddress.trim(),
                destinationAddress.trim(),
                description != null ? description.trim() : null,
                dispatchDate,
                ownerEmail.trim(),
                requestedAt);
    }

    private static void validateRequiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw DomainError.validation(fieldName + " is required");
        }
    }
}
