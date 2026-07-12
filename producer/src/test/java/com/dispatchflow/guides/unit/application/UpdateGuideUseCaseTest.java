package com.dispatchflow.guides.unit.application;

import com.dispatchflow.guides.application.UpdateGuideUseCase;
import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.application.dto.UpdateGuideCommand;
import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.repositories.InMemoryGuideRepository;
import com.dispatchflow.guides.domain.services.GuideNumberGenerator;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.unit.application.support.GuideApplicationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateGuideUseCaseTest {

    private static final Instant CREATE_TIME = Instant.parse("2026-06-02T10:00:00Z");
    private static final Instant UPDATE_TIME = Instant.parse("2026-06-02T11:00:00Z");

    private InMemoryGuideRepository repository;
    private GuideApplicationTestSupport.InMemoryObjectStorage objectStorage;
    private UpdateGuideUseCase updateGuideUseCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryGuideRepository();
        objectStorage = GuideApplicationTestSupport.inMemoryObjectStorage();
        updateGuideUseCase = GuideApplicationTestSupport.updateGuideUseCase(
                repository, Clock.fixed(UPDATE_TIME, ZoneOffset.UTC), objectStorage);
        seedGuide();
    }

    @Test
    void updatesGuideAndRegeneratesPdfWithUploadedToS3Status() {
        GuideResponse created = repository.findAllActive().stream()
                .findFirst()
                .map(GuideResponse::from)
                .orElseThrow();
        UpdateGuideCommand updateCommand = new UpdateGuideCommand(
                "Transportes Norte",
                "Juan Pérez",
                "Av. Libertador 99, Santiago",
                "Calle Estado 100, Santiago",
                "Despacho actualizado",
                LocalDate.of(2026, 6, 3),
                "nuevo.responsable@empresa.cl");

        GuideResponse updated = updateGuideUseCase.execute(created.id(), updateCommand);

        assertEquals("Transportes Norte", updated.carrierName());
        assertEquals("UPLOADED_TO_S3", updated.status());
        assertTrue(objectStorage.contains(updated.s3Key()));
    }

    private void seedGuide() {
        DispatchGuide guide = DispatchGuide.create(
                GuideId.generate(),
                new GuideNumberGenerator().generate(1),
                "Transportes Rápidos",
                "María González",
                "Av. Providencia 1234, Santiago",
                "Calle Huérfanos 567, Santiago",
                "Electrónicos",
                LocalDate.of(2026, 6, 2),
                Email.create("responsable@empresa.cl"),
                CREATE_TIME);
        guide.markPdfGenerated("/efs/guide.pdf", CREATE_TIME);
        guide.markUploadedToS3("guides/2026-06-02/transportes-rapidos/guide-1.pdf", CREATE_TIME);
        repository.save(guide);
    }
}
