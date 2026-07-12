package com.dispatchflow.guides.application;

import com.dispatchflow.guides.application.dto.GuidePdfDownload;
import com.dispatchflow.guides.application.ports.EfsStoragePort;
import com.dispatchflow.guides.application.ports.ObjectStoragePort;
import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.repositories.GuideRepository;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.shared.domain.DomainError;

public class DownloadGuidePdfUseCase {

    private final GuideRepository guideRepository;
    private final ObjectStoragePort objectStorage;
    private final EfsStoragePort efsStorage;

    public DownloadGuidePdfUseCase(
            GuideRepository guideRepository,
            ObjectStoragePort objectStorage,
            EfsStoragePort efsStorage) {
        this.guideRepository = guideRepository;
        this.objectStorage = objectStorage;
        this.efsStorage = efsStorage;
    }

    public GuidePdfDownload execute(String id) {
        DispatchGuide guide = guideRepository.findById(GuideId.create(id))
                .orElseThrow(() -> DomainError.notFound("Guide " + id + " not found"));

        if (guide.isDeleted()) {
            throw DomainError.notFound("Guide " + id + " not found");
        }

        byte[] content = readPdfContent(guide, id);
        String fileName = "guide-" + guide.getId().value() + ".pdf";
        return new GuidePdfDownload(content, fileName);
    }

    private byte[] readPdfContent(DispatchGuide guide, String id) {
        if (guide.getS3Key() != null && !guide.getS3Key().isBlank()) {
            return objectStorage.read(guide.getS3Key());
        }
        if (guide.getEfsPath() != null && !guide.getEfsPath().isBlank()) {
            return efsStorage.read(guide.getEfsPath());
        }
        throw DomainError.notFound("PDF for guide " + id + " not found");
    }
}
