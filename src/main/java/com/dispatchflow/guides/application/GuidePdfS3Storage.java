package com.dispatchflow.guides.application;

import com.dispatchflow.guides.application.ports.ObjectStoragePort;
import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.services.GuidePdfPathBuilder;

import java.time.Instant;

public class GuidePdfS3Storage {

    private final GuidePdfPathBuilder pathBuilder;
    private final ObjectStoragePort objectStorage;

    public GuidePdfS3Storage(GuidePdfPathBuilder pathBuilder, ObjectStoragePort objectStorage) {
        this.pathBuilder = pathBuilder;
        this.objectStorage = objectStorage;
    }

    public void storeOnS3(DispatchGuide guide, byte[] pdfContent, Instant now) {
        String key = pathBuilder.buildRelativePath(guide);
        String previousKey = guide.getS3Key();
        if (previousKey != null && !previousKey.isBlank() && !previousKey.equals(key)) {
            objectStorage.delete(previousKey);
        }
        objectStorage.store(key, pdfContent);
        guide.markUploadedToS3(key, now);
    }
}
