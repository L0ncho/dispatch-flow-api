package com.dispatchflow.guides.unit.application;

import com.dispatchflow.guides.application.GuidePdfS3Storage;
import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.services.GuidePdfPathBuilder;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.domain.valueobjects.GuideNumber;
import com.dispatchflow.guides.domain.valueobjects.GuideStatus;
import com.dispatchflow.guides.unit.application.support.GuideApplicationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuidePdfS3StorageTest {

    private static final Instant NOW = Instant.parse("2026-06-02T10:00:00Z");
    private static final byte[] PDF_BYTES = GuideApplicationTestSupport.PDF_BYTES;

    private GuideApplicationTestSupport.InMemoryObjectStorage objectStorage;
    private GuidePdfS3Storage guidePdfS3Storage;

    @BeforeEach
    void setUp() {
        objectStorage = GuideApplicationTestSupport.inMemoryObjectStorage();
        guidePdfS3Storage = new GuidePdfS3Storage(new GuidePdfPathBuilder(), objectStorage);
    }

    @Test
    void storesPdfOnS3AndMarksGuideAsUploaded() {
        DispatchGuide guide = createGuideWithEfsPdf();

        guidePdfS3Storage.storeOnS3(guide, PDF_BYTES, NOW);

        assertEquals(GuideStatus.UPLOADED_TO_S3, guide.getStatus());
        assertEquals("guides/2026-06-02/transportes-rapidos/guide-guide-123.pdf", guide.getS3Key());
        assertTrue(objectStorage.contains(guide.getS3Key()));
    }

    private DispatchGuide createGuideWithEfsPdf() {
        DispatchGuide guide = DispatchGuide.create(
                GuideId.create("guide-123"),
                GuideNumber.create("GD-2026-000001"),
                "Transportes Rápidos",
                "María González",
                "Av. Providencia 1234, Santiago",
                "Calle Huérfanos 567, Santiago",
                null,
                LocalDate.of(2026, 6, 2),
                Email.create("responsable@empresa.cl"),
                NOW);
        guide.markPdfGenerated("/efs/guides/guide.pdf", NOW);
        return guide;
    }
}
