package com.dispatchflow.guides.application;

import com.dispatchflow.guides.application.ports.EfsStoragePort;
import com.dispatchflow.guides.application.ports.GuidePdfGeneratorPort;
import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.services.GuidePdfPathBuilder;

import java.time.Instant;

public class GuidePdfEfsStorage {

    private final GuidePdfPathBuilder pathBuilder;
    private final GuidePdfGeneratorPort pdfGenerator;
    private final EfsStoragePort efsStorage;

    public GuidePdfEfsStorage(
            GuidePdfPathBuilder pathBuilder,
            GuidePdfGeneratorPort pdfGenerator,
            EfsStoragePort efsStorage) {
        this.pathBuilder = pathBuilder;
        this.pdfGenerator = pdfGenerator;
        this.efsStorage = efsStorage;
    }

    public byte[] storeOnEfs(DispatchGuide guide, Instant now) {
        String relativePath = pathBuilder.buildRelativePath(guide);
        byte[] pdfContent = pdfGenerator.generate(guide);
        String absolutePath = efsStorage.write(relativePath, pdfContent);
        guide.markPdfGenerated(absolutePath, now);
        return pdfContent;
    }
}
