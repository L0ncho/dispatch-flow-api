package com.dispatchflow.guides.application;

import com.dispatchflow.guides.application.dto.CreateGuideCommand;
import com.dispatchflow.guides.application.dto.GuideAcceptedResponse;
import com.dispatchflow.guides.application.ports.GuideMessagePublisher;
import com.dispatchflow.shared.messaging.GuideCreationMessage;

import java.time.Clock;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcceptGuideRequestUseCase {

    private static final Logger log = LoggerFactory.getLogger(AcceptGuideRequestUseCase.class);

    private final GuideMessagePublisher guideMessagePublisher;
    private final Clock clock;

    public AcceptGuideRequestUseCase(GuideMessagePublisher guideMessagePublisher, Clock clock) {
        this.guideMessagePublisher = guideMessagePublisher;
        this.clock = clock;
    }

    public GuideAcceptedResponse execute(CreateGuideCommand command) {
        String trackingId = UUID.randomUUID().toString();
        GuideCreationMessage message = GuideCreationMessage.create(
                trackingId,
                command.carrierName(),
                command.recipientName(),
                command.originAddress(),
                command.destinationAddress(),
                command.description(),
                command.dispatchDate(),
                command.ownerEmail(),
                clock.instant());

        guideMessagePublisher.publish(message);
        log.info("Guide request accepted for async processing. trackingId={}", trackingId);

        return GuideAcceptedResponse.accepted(trackingId);
    }
}
