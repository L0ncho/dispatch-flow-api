package com.dispatchflow.guides.infrastructure.persistence;

public enum GuideStatusJpa {
    CREATED,
    PDF_GENERATED,
    UPLOADED_TO_S3,
    UPDATED,
    DELETED
}
