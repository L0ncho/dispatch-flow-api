package com.dispatchflow.guides.infrastructure.adapters;

import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.infrastructure.config.RabbitMQConfig;
import com.dispatchflow.guides.infrastructure.persistence.ProcessedGuideEntity;
import com.dispatchflow.guides.infrastructure.persistence.ProcessedGuideJpaRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class RabbitMQGuideListener {

    private final ProcessedGuideJpaRepository repository;
    private final RabbitTemplate rabbitTemplate;

   
    public RabbitMQGuideListener(ProcessedGuideJpaRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    
    public String consumeOneMessageAndSave() {
        
        Object message = rabbitTemplate.receiveAndConvert(RabbitMQConfig.MAIN_QUEUE);

        if (message == null) {
            return "No hay mensajes pendientes en la cola 1.";
        }

        if (message instanceof GuideResponse guideResponse) {
            ProcessedGuideEntity entity = new ProcessedGuideEntity();
            entity.setGuideId(guideResponse.id());
            entity.setGuideNumber(guideResponse.guideNumber());
            entity.setProcessedAt(LocalDateTime.now());
            
            repository.save(entity);
            return "Mensaje consumido exitosamente. Datos guardados en la tabla processed_guides. Guía N°: " + guideResponse.guideNumber();
        }

        return "Se leyó un mensaje, pero no era del formato correcto.";
    }
}