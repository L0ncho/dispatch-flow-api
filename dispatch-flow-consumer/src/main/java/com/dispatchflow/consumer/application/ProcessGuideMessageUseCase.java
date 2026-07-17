package com.dispatchflow.consumer.application;

import com.dispatchflow.guides.application.GuidePdfEfsStorage;
import com.dispatchflow.guides.application.GuidePdfS3Storage;
import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.services.GuideNumberGenerator;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.consumer.infrastructure.persistence.AsyncDispatchGuideEntity;
import com.dispatchflow.consumer.infrastructure.persistence.AsyncDispatchGuideRepository;
import com.dispatchflow.consumer.infrastructure.persistence.AsyncGuideSequenceRepository;
import com.dispatchflow.shared.messaging.GuideCreationMessage;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class ProcessGuideMessageUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessGuideMessageUseCase.class);

    private final AsyncGuideSequenceRepository sequenceRepository;
    private final AsyncDispatchGuideRepository asyncDispatchGuideRepository;
    private final GuideNumberGenerator guideNumberGenerator;
    private final GuidePdfEfsStorage guidePdfEfsStorage;
    private final GuidePdfS3Storage guidePdfS3Storage;
    private final String s3BucketName;
    private final Clock clock;

    public ProcessGuideMessageUseCase(
            AsyncGuideSequenceRepository sequenceRepository,
            AsyncDispatchGuideRepository asyncDispatchGuideRepository,
            GuideNumberGenerator guideNumberGenerator,
            GuidePdfEfsStorage guidePdfEfsStorage,
            GuidePdfS3Storage guidePdfS3Storage,
            String s3BucketName,
            Clock clock) {
        this.sequenceRepository = sequenceRepository;
        this.asyncDispatchGuideRepository = asyncDispatchGuideRepository;
        this.guideNumberGenerator = guideNumberGenerator;
        this.guidePdfEfsStorage = guidePdfEfsStorage;
        this.guidePdfS3Storage = guidePdfS3Storage;
        this.s3BucketName = s3BucketName;
        this.clock = clock;
    }

    @Transactional
    public String execute(GuideCreationMessage message) {
        log.info("Processing guide message. trackingId={}", message.trackingId());

        long sequence = sequenceRepository.nextSequence();
        DispatchGuide guide = DispatchGuide.create(
                GuideId.generate(),
                guideNumberGenerator.generate(sequence),
                message.carrierName(),
                message.recipientName(),
                message.originAddress(),
                message.destinationAddress(),
                message.description(),
                message.dispatchDate(),
                Email.create(message.ownerEmail()),
                clock.instant());

        byte[] pdfContent = guidePdfEfsStorage.storeOnEfs(guide, clock.instant());
        guidePdfS3Storage.storeOnS3(guide, pdfContent, clock.instant());

        String guideId = guide.getId().value();
        AsyncDispatchGuideEntity entity = new AsyncDispatchGuideEntity();
        entity.setTrackingId(message.trackingId());
        entity.setGuideId(guideId);
        entity.setGuideNumber(guide.getGuideNumber().value());
        entity.setCarrierName(guide.getCarrierName());
        entity.setRecipientName(guide.getRecipientName());
        entity.setOriginAddress(guide.getOriginAddress());
        entity.setDestinationAddress(guide.getDestinationAddress());
        entity.setDescription(guide.getDescription());
        entity.setDispatchDate(guide.getDispatchDate());
        entity.setOwnerEmail(guide.getOwnerEmail().value());
        entity.setReceivedAt(LocalDateTime.ofInstant(message.requestedAt(), clock.getZone()));
        entity.setProcessedAt(LocalDateTime.now(clock.getZone()));
        entity.setStatus(AsyncDispatchGuideEntity.ProcessingStatus.PROCESSED);
        entity.setS3Bucket(s3BucketName);
        entity.setS3Key(guide.getS3Key());
        entity.setEfsPath(guide.getEfsPath());

        asyncDispatchGuideRepository.save(entity);
        log.info(
                "Guide processed successfully. trackingId={}, guideId={}, guideNumber={}",
                message.trackingId(),
                guideId,
                guide.getGuideNumber().value());
        return guideId;
    }
}
