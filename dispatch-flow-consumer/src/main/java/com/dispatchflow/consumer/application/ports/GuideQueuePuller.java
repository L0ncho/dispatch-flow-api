package com.dispatchflow.consumer.application.ports;

import com.dispatchflow.shared.messaging.GuideCreationMessage;

import java.util.Optional;

public interface GuideQueuePuller {

    Optional<PulledGuideMessage> pullNext();

    interface PulledGuideMessage {
        GuideCreationMessage payload();

        void acknowledge();

        void rejectWithoutRequeue();
    }
}
