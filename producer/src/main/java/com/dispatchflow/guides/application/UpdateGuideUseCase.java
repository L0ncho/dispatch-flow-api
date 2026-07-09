package com.dispatchflow.guides.application;

import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.application.dto.UpdateGuideCommand;
import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.repositories.GuideRepository;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.shared.domain.DomainError;

import java.time.Clock;

public class UpdateGuideUseCase {

    private final GuideRepository guideRepository;
    private final GuidePdfEfsStorage guidePdfEfsStorage;
    private final GuidePdfS3Storage guidePdfS3Storage;
    private final Clock clock;

    public UpdateGuideUseCase(
            GuideRepository guideRepository,
            GuidePdfEfsStorage guidePdfEfsStorage,
            GuidePdfS3Storage guidePdfS3Storage,
            Clock clock) {
        this.guideRepository = guideRepository;
        this.guidePdfEfsStorage = guidePdfEfsStorage;
        this.guidePdfS3Storage = guidePdfS3Storage;
        this.clock = clock;
    }

    public GuideResponse execute(String id, UpdateGuideCommand command) {
        DispatchGuide guide = guideRepository.findById(GuideId.create(id))
                .orElseThrow(() -> DomainError.notFound("Guide " + id + " not found"));

        if (guide.isDeleted()) {
            throw DomainError.notFound("Guide " + id + " not found");
        }

        guide.update(
                command.carrierName(),
                command.recipientName(),
                command.originAddress(),
                command.destinationAddress(),
                command.description(),
                command.dispatchDate(),
                Email.create(command.ownerEmail()),
                clock.instant());

        byte[] pdfContent = guidePdfEfsStorage.storeOnEfs(guide, clock.instant());
        guidePdfS3Storage.storeOnS3(guide, pdfContent, clock.instant());
        guideRepository.save(guide);
        return GuideResponse.from(guide);
    }
}
