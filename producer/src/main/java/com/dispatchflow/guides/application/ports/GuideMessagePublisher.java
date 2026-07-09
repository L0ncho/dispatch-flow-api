package com.dispatchflow.guides.application.ports;

import com.dispatchflow.shared.messaging.GuideCreationMessage;

public interface GuideMessagePublisher {

    void publish(GuideCreationMessage message);
}
