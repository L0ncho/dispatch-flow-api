package com.dispatchflow.guides.unit.application;

import com.dispatchflow.guides.application.AcceptGuideRequestUseCase;
import com.dispatchflow.guides.application.dto.CreateGuideCommand;
import com.dispatchflow.guides.application.dto.GuideAcceptedResponse;
import com.dispatchflow.guides.application.ports.GuideMessagePublisher;
import com.dispatchflow.shared.messaging.GuideCreationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AcceptGuideRequestUseCaseTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-06-02T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    private InMemoryGuideMessagePublisher publisher;
    private AcceptGuideRequestUseCase useCase;

    @BeforeEach
    void setUp() {
        publisher = new InMemoryGuideMessagePublisher();
        useCase = new AcceptGuideRequestUseCase(publisher, FIXED_CLOCK);
    }

    @Test
    void acceptsGuideRequestAndPublishesMessageForAsyncProcessing() {
        CreateGuideCommand command = new CreateGuideCommand(
                "Transportes Rápidos",
                "María González",
                "Av. Providencia 1234, Santiago",
                "Calle Huérfanos 567, Santiago",
                "Electrónicos",
                LocalDate.of(2026, 6, 2),
                "responsable@empresa.cl");

        GuideAcceptedResponse response = useCase.execute(command);

        assertEquals("ACCEPTED", response.status());
        assertEquals("La guía fue enviada a procesamiento asíncrono", response.message());
        assertNotNull(response.trackingId());
        assertEquals(1, publisher.publishedMessages().size());
        assertEquals(response.trackingId(), publisher.publishedMessages().getFirst().trackingId());
        assertEquals("Transportes Rápidos", publisher.publishedMessages().getFirst().carrierName());
    }

    static class InMemoryGuideMessagePublisher implements GuideMessagePublisher {

        private final List<GuideCreationMessage> messages = new ArrayList<>();

        @Override
        public void publish(GuideCreationMessage message) {
            messages.add(message);
        }

        List<GuideCreationMessage> publishedMessages() {
            return messages;
        }
    }
}
