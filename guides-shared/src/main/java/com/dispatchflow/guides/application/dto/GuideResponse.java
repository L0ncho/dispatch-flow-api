package com.dispatchflow.guides.application.dto;

import com.dispatchflow.guides.domain.entities.DispatchGuide;

import java.time.Instant;
import java.time.LocalDate;

public record GuideResponse(
        String id,
        String guideNumber,
        String carrierName,
        String recipientName,
        String originAddress,
        String destinationAddress,
        String description,
        LocalDate dispatchDate,
        Instant createdAt,
        Instant updatedAt,
        String ownerEmail,
        String status,
        String efsPath,
        String s3Key) {

    public static GuideResponse from(DispatchGuide guide) {
        return new GuideResponse(
                guide.getId().value(),
                guide.getGuideNumber().value(),
                guide.getCarrierName(),
                guide.getRecipientName(),
                guide.getOriginAddress(),
                guide.getDestinationAddress(),
                guide.getDescription(),
                guide.getDispatchDate(),
                guide.getCreatedAt(),
                guide.getUpdatedAt(),
                guide.getOwnerEmail().value(),
                guide.getStatus().name(),
                guide.getEfsPath(),
                guide.getS3Key());
    }
}
