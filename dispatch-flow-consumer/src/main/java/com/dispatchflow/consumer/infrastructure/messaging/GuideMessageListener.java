package com.dispatchflow.consumer.infrastructure.messaging;

import com.dispatchflow.consumer.application.ProcessGuideMessageUseCase;
import com.dispatchflow.shared.messaging.GuideCreationMessage;
import com.dispatchflow.shared.messaging.RabbitMqTopology;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class GuideMessageListener {

    private final ProcessGuideMessageUseCase processGuideMessageUseCase;

    public GuideMessageListener(ProcessGuideMessageUseCase processGuideMessageUseCase) {
        this.processGuideMessageUseCase = processGuideMessageUseCase;
    }

    @RabbitListener(
            queues = RabbitMqTopology.MAIN_QUEUE,
            autoStartup = "${dispatch.consumer.listener-enabled:false}")
    public void onGuideCreationMessage(GuideCreationMessage message) {
        processGuideMessageUseCase.execute(message);
    }
}
