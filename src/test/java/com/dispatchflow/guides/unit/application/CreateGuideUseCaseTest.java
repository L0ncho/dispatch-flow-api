package com.dispatchflow.guides.unit.application;

import com.dispatchflow.guides.application.CreateGuideUseCase;
import com.dispatchflow.guides.application.dto.CreateGuideCommand;
import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.domain.repositories.InMemoryGuideRepository;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.infrastructure.adapters.RabbitMQGuidePublisher;
import com.dispatchflow.guides.unit.application.support.GuideApplicationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateGuideUseCaseTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-06-02T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    private InMemoryGuideRepository repository;
    private GuideApplicationTestSupport.InMemoryObjectStorage objectStorage;
    private RabbitMQGuidePublisher mockPublisher;
    private CreateGuideUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryGuideRepository();
        objectStorage = GuideApplicationTestSupport.inMemoryObjectStorage();
        
        // Creamos un Mock (simulador) del publisher para que no intente conectarse al RabbitMQ real
        mockPublisher = Mockito.mock(RabbitMQGuidePublisher.class);
        
        useCase = GuideApplicationTestSupport.createGuideUseCase(repository, FIXED_CLOCK, objectStorage, mockPublisher);
    }

    @Test
    void createsGuideWithPdfOnEfsAndS3AndPersists() {
        CreateGuideCommand command = new CreateGuideCommand(
                "Transportes Rápidos",
                "María González",
                "Av. Providencia 1234, Santiago",
                "Calle Huérfanos 567, Santiago",
                "Electrónicos",
                LocalDate.of(2026, 6, 2),
                "responsable@empresa.cl");

        GuideResponse response = useCase.execute(command);

        assertNotNull(response.id());
        assertTrue(response.guideNumber().startsWith("GD-"));
        assertEquals("UPLOADED_TO_S3", response.status());
        assertNotNull(response.efsPath());
        assertNotNull(response.s3Key());
        assertTrue(objectStorage.contains(response.s3Key()));
        assertTrue(repository.findById(GuideId.create(response.id())).isPresent());
        
        
        Mockito.verify(mockPublisher, Mockito.times(1)).publishGuideCreated(Mockito.any(GuideResponse.class));
    }
}