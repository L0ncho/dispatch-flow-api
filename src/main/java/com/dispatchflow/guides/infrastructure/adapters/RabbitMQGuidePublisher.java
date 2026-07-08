package com.dispatchflow.guides.infrastructure.adapters;

import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQGuidePublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQGuidePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishGuideCreated(GuideResponse guideResponse) {
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.MAIN_ROUTING_KEY,
                guideResponse
        );
    }
}