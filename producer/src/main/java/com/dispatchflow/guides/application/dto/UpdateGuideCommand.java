package com.dispatchflow.guides.application.dto;

import java.time.LocalDate;

public record UpdateGuideCommand(
        String carrierName,
        String recipientName,
        String originAddress,
        String destinationAddress,
        String description,
        LocalDate dispatchDate,
        String ownerEmail) {
}
