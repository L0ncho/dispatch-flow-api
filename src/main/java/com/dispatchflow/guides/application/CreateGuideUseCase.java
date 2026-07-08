package com.dispatchflow.guides.application;

import com.dispatchflow.guides.application.dto.CreateGuideCommand;
import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.repositories.GuideRepository;
import com.dispatchflow.guides.domain.services.GuideNumberGenerator;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.infrastructure.adapters.RabbitMQGuidePublisher;

import java.time.Clock;

public class CreateGuideUseCase {

    private final GuideRepository guideRepository;
    private final GuideNumberGenerator guideNumberGenerator;
    private final GuidePdfEfsStorage guidePdfEfsStorage;
    private final GuidePdfS3Storage guidePdfS3Storage;
    private final RabbitMQGuidePublisher rabbitMQGuidePublisher;
    private final Clock clock;

    public CreateGuideUseCase(
            GuideRepository guideRepository,
            GuideNumberGenerator guideNumberGenerator,
            GuidePdfEfsStorage guidePdfEfsStorage,
            GuidePdfS3Storage guidePdfS3Storage,
            RabbitMQGuidePublisher rabbitMQGuidePublisher,
            Clock clock) {
        this.guideRepository = guideRepository;
        this.guideNumberGenerator = guideNumberGenerator;
        this.guidePdfEfsStorage = guidePdfEfsStorage;
        this.guidePdfS3Storage = guidePdfS3Storage;
        this.rabbitMQGuidePublisher = rabbitMQGuidePublisher;
        this.clock = clock;
    }

    public GuideResponse execute(CreateGuideCommand command) {
        long sequence = guideRepository.nextSequence();
        DispatchGuide guide = DispatchGuide.create(
                GuideId.generate(),
                guideNumberGenerator.generate(sequence),
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
        GuideResponse response = GuideResponse.from(guide);
        rabbitMQGuidePublisher.publishGuideCreated((response));
        return GuideResponse.from(guide);
    }
}
