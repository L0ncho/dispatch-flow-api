package com.dispatchflow.guides.infrastructure.http;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateGuideRequest(
        @NotBlank String carrierName,
        @NotBlank String recipientName,
        @NotBlank String originAddress,
        @NotBlank String destinationAddress,
        String description,
        @NotNull LocalDate dispatchDate,
        @NotBlank @Email String ownerEmail) {
}
