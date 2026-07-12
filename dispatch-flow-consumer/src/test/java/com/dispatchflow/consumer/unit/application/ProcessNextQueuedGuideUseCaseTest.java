package com.dispatchflow.consumer.unit.application;

import com.dispatchflow.consumer.application.ProcessGuideMessageUseCase;
import com.dispatchflow.consumer.application.ProcessNextQueuedGuideUseCase;
import com.dispatchflow.consumer.application.dto.ProcessedQueuedGuide;
import com.dispatchflow.consumer.application.ports.GuideQueuePuller;
import com.dispatchflow.shared.messaging.GuideCreationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProcessNextQueuedGuideUseCaseTest {

    /*
     * 1. Cola vacía → Optional.empty
     * 2. Mensaje OK → process + ack + trackingId/guideId
     * 3. Process falla → nack + rethrow
     */

    private ProcessGuideMessageUseCase processGuideMessageUseCase;
    private ProcessNextQueuedGuideUseCase useCase;

    @BeforeEach
    void setUp() {
        processGuideMessageUseCase = mock(ProcessGuideMessageUseCase.class);
    }

    @Test
    void returnsEmptyWhenQueueHasNoMessages() {
        GuideQueuePuller puller = () -> Optional.empty();
        useCase = new ProcessNextQueuedGuideUseCase(puller, processGuideMessageUseCase);

        Optional<ProcessedQueuedGuide> result = useCase.execute();

        assertTrue(result.isEmpty());
        verifyNoInteractions(processGuideMessageUseCase);
    }

    @Test
    void processesMessageAndAcknowledgesOnSuccess() {
        GuideCreationMessage message = sampleMessage();
        AtomicBoolean acknowledged = new AtomicBoolean(false);
        AtomicBoolean rejected = new AtomicBoolean(false);
        GuideQueuePuller puller = () -> Optional.of(pulled(message, acknowledged, rejected));
        when(processGuideMessageUseCase.execute(message)).thenReturn("guide-42");
        useCase = new ProcessNextQueuedGuideUseCase(puller, processGuideMessageUseCase);

        Optional<ProcessedQueuedGuide> result = useCase.execute();

        assertEquals(Optional.of(new ProcessedQueuedGuide("track-1", "guide-42")), result);
        verify(processGuideMessageUseCase).execute(message);
        assertTrue(acknowledged.get());
        assertTrue(!rejected.get());
    }

    @Test
    void rejectsWithoutRequeueWhenProcessingFails() {
        GuideCreationMessage message = sampleMessage();
        AtomicBoolean acknowledged = new AtomicBoolean(false);
        AtomicBoolean rejected = new AtomicBoolean(false);
        GuideQueuePuller puller = () -> Optional.of(pulled(message, acknowledged, rejected));
        doThrow(new RuntimeException("S3 failed")).when(processGuideMessageUseCase).execute(message);
        useCase = new ProcessNextQueuedGuideUseCase(puller, processGuideMessageUseCase);

        RuntimeException error = assertThrows(RuntimeException.class, useCase::execute);

        assertEquals("S3 failed", error.getMessage());
        assertTrue(rejected.get());
        assertTrue(!acknowledged.get());
    }

    private static GuideCreationMessage sampleMessage() {
        return GuideCreationMessage.create(
                "track-1",
                "Transportes Rápidos",
                "María González",
                "Origen",
                "Destino",
                "Electrónicos",
                LocalDate.of(2026, 6, 2),
                "responsable@empresa.cl",
                Instant.parse("2026-06-02T09:00:00Z"));
    }

    private static GuideQueuePuller.PulledGuideMessage pulled(
            GuideCreationMessage message,
            AtomicBoolean acknowledged,
            AtomicBoolean rejected) {
        return new GuideQueuePuller.PulledGuideMessage() {
            @Override
            public GuideCreationMessage payload() {
                return message;
            }

            @Override
            public void acknowledge() {
                acknowledged.set(true);
            }

            @Override
            public void rejectWithoutRequeue() {
                rejected.set(true);
            }
        };
    }
}
