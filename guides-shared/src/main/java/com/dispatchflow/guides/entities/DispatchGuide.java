package com.dispatchflow.guides.domain.entities;

import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.domain.valueobjects.GuideNumber;
import com.dispatchflow.guides.domain.valueobjects.GuideStatus;
import com.dispatchflow.shared.domain.DomainError;

import java.time.Instant;
import java.time.LocalDate;

public class DispatchGuide {

    private final GuideId id;
    private final GuideNumber guideNumber;
    private String carrierName;
    private String recipientName;
    private String originAddress;
    private String destinationAddress;
    private String description;
    private LocalDate dispatchDate;
    private final Instant createdAt;
    private Instant updatedAt;
    private Email ownerEmail;
    private GuideStatus status;
    private String efsPath;
    private String s3Key;

    private DispatchGuide(
            GuideId id,
            GuideNumber guideNumber,
            String carrierName,
            String recipientName,
            String originAddress,
            String destinationAddress,
            String description,
            LocalDate dispatchDate,
            Instant createdAt,
            Instant updatedAt,
            Email ownerEmail,
            GuideStatus status,
            String efsPath,
            String s3Key) {
        this.id = id;
        this.guideNumber = guideNumber;
        this.carrierName = carrierName;
        this.recipientName = recipientName;
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.description = description;
        this.dispatchDate = dispatchDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.ownerEmail = ownerEmail;
        this.status = status;
        this.efsPath = efsPath;
        this.s3Key = s3Key;
    }

    public static DispatchGuide restore(
            GuideId id,
            GuideNumber guideNumber,
            String carrierName,
            String recipientName,
            String originAddress,
            String destinationAddress,
            String description,
            LocalDate dispatchDate,
            Instant createdAt,
            Instant updatedAt,
            Email ownerEmail,
            GuideStatus status,
            String efsPath,
            String s3Key) {
        return new DispatchGuide(
                id,
                guideNumber,
                carrierName,
                recipientName,
                originAddress,
                destinationAddress,
                description,
                dispatchDate,
                createdAt,
                updatedAt,
                ownerEmail,
                status,
                efsPath,
                s3Key);
    }

    public static DispatchGuide create(
            GuideId id,
            GuideNumber guideNumber,
            String carrierName,
            String recipientName,
            String originAddress,
            String destinationAddress,
            String description,
            LocalDate dispatchDate,
            Email ownerEmail,
            Instant now) {
        validateRequiredText(carrierName, "Carrier name");
        validateRequiredText(recipientName, "Recipient name");
        validateRequiredText(originAddress, "Origin address");
        validateRequiredText(destinationAddress, "Destination address");
        if (dispatchDate == null) {
            throw DomainError.validation("Dispatch date is required");
        }

        return new DispatchGuide(
                id,
                guideNumber,
                carrierName.trim(),
                recipientName.trim(),
                originAddress.trim(),
                destinationAddress.trim(),
                description != null ? description.trim() : null,
                dispatchDate,
                now,
                now,
                ownerEmail,
                GuideStatus.CREATED,
                null,
                null);
    }

    public void update(
            String carrierName,
            String recipientName,
            String originAddress,
            String destinationAddress,
            String description,
            LocalDate dispatchDate,
            Email ownerEmail,
            Instant now) {
        if (isDeleted()) {
            throw DomainError.validation("Cannot update a deleted guide");
        }
        validateRequiredText(carrierName, "Carrier name");
        validateRequiredText(recipientName, "Recipient name");
        validateRequiredText(originAddress, "Origin address");
        validateRequiredText(destinationAddress, "Destination address");
        if (dispatchDate == null) {
            throw DomainError.validation("Dispatch date is required");
        }

        this.carrierName = carrierName.trim();
        this.recipientName = recipientName.trim();
        this.originAddress = originAddress.trim();
        this.destinationAddress = destinationAddress.trim();
        this.description = description != null ? description.trim() : null;
        this.dispatchDate = dispatchDate;
        this.ownerEmail = ownerEmail;
        this.status = GuideStatus.UPDATED;
        this.updatedAt = now;
    }

    public void markPdfGenerated(String efsPath, Instant now) {
        if (isDeleted()) {
            throw DomainError.validation("Cannot generate PDF for a deleted guide");
        }
        if (efsPath == null || efsPath.isBlank()) {
            throw DomainError.validation("EFS path is required");
        }
        this.efsPath = efsPath;
        this.status = GuideStatus.PDF_GENERATED;
        this.updatedAt = now;
    }

    public void markUploadedToS3(String s3Key, Instant now) {
        if (isDeleted()) {
            throw DomainError.validation("Cannot upload to S3 for a deleted guide");
        }
        if (s3Key == null || s3Key.isBlank()) {
            throw DomainError.validation("S3 key is required");
        }
        this.s3Key = s3Key;
        this.status = GuideStatus.UPLOADED_TO_S3;
        this.updatedAt = now;
    }

    public void markDeleted(Instant now) {
        if (isDeleted()) {
            throw DomainError.validation("Guide is already deleted");
        }
        this.status = GuideStatus.DELETED;
        this.updatedAt = now;
    }

    public boolean isDeleted() {
        return status == GuideStatus.DELETED;
    }

    private static void validateRequiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw DomainError.validation(fieldName + " is required");
        }
    }

    public GuideId getId() {
        return id;
    }

    public GuideNumber getGuideNumber() {
        return guideNumber;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getOriginAddress() {
        return originAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDispatchDate() {
        return dispatchDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Email getOwnerEmail() {
        return ownerEmail;
    }

    public GuideStatus getStatus() {
        return status;
    }

    public String getEfsPath() {
        return efsPath;
    }

    public String getS3Key() {
        return s3Key;
    }
}
