package com.dispatchflow.guides.infrastructure.adapters;

import com.dispatchflow.guides.application.ports.GuideMessagePublisher;
import com.dispatchflow.shared.messaging.GuideCreationMessage;
import com.dispatchflow.shared.messaging.RabbitMqTopology;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQGuidePublisher implements GuideMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQGuidePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(GuideCreationMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMqTopology.EXCHANGE,
                RabbitMqTopology.MAIN_ROUTING_KEY,
                message);
    }
}
