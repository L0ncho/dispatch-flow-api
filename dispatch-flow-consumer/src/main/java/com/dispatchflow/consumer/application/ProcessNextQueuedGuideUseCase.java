package com.dispatchflow.consumer.application;

import com.dispatchflow.consumer.application.ports.GuideQueuePuller;
import com.dispatchflow.shared.messaging.GuideCreationMessage;

import java.util.Optional;

public class ProcessNextQueuedGuideUseCase {

    private final GuideQueuePuller guideQueuePuller;
    private final ProcessGuideMessageUseCase processGuideMessageUseCase;

    public ProcessNextQueuedGuideUseCase(
            GuideQueuePuller guideQueuePuller,
            ProcessGuideMessageUseCase processGuideMessageUseCase) {
        this.guideQueuePuller = guideQueuePuller;
        this.processGuideMessageUseCase = processGuideMessageUseCase;
    }

    public Optional<String> execute() {
        Optional<GuideQueuePuller.PulledGuideMessage> pulled = guideQueuePuller.pullNext();
        if (pulled.isEmpty()) {
            return Optional.empty();
        }

        GuideQueuePuller.PulledGuideMessage message = pulled.get();
        GuideCreationMessage payload = message.payload();
        try {
            processGuideMessageUseCase.execute(payload);
            message.acknowledge();
            return Optional.of(payload.trackingId());
        } catch (RuntimeException error) {
            message.rejectWithoutRequeue();
            throw error;
        }
    }
}
