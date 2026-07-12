package com.dispatchflow.guides.unit.domain.entities;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.domain.valueobjects.GuideNumber;
import com.dispatchflow.guides.domain.valueobjects.GuideStatus;
import com.dispatchflow.shared.domain.DomainError;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DispatchGuideTest {

    private static final Instant NOW = Instant.parse("2026-06-02T10:00:00Z");

    @Test
    void createsGuideWithCreatedStatus() {
        DispatchGuide guide = DispatchGuide.create(
                GuideId.generate(),
                GuideNumber.create("GD-2026-000001"),
                "Transportes Rápidos",
                "María González",
                "Av. Providencia 1234, Santiago",
                "Calle Huérfanos 567, Santiago",
                "Electrónicos",
                LocalDate.of(2026, 6, 2),
                Email.create("responsable@empresa.cl"),
                NOW);

        assertEquals(GuideStatus.CREATED, guide.getStatus());
        assertEquals(NOW, guide.getCreatedAt());
        assertEquals(NOW, guide.getUpdatedAt());
        assertNull(guide.getEfsPath());
        assertNull(guide.getS3Key());
    }

    @Test
    void rejectsCreateWhenCarrierNameIsBlank() {
        assertThrows(DomainError.class, () -> DispatchGuide.create(
                GuideId.generate(),
                GuideNumber.create("GD-2026-000001"),
                "  ",
                "María González",
                "Av. Providencia 1234, Santiago",
                "Calle Huérfanos 567, Santiago",
                null,
                LocalDate.of(2026, 6, 2),
                Email.create("responsable@empresa.cl"),
                NOW));
    }

    @Test
    void updatesGuideAndSetsUpdatedStatus() {
        DispatchGuide guide = createSampleGuide();
        Instant updateTime = NOW.plusSeconds(60);

        guide.update(
                "Transportes Norte",
                "Juan Pérez",
                "Av. Libertador 99, Santiago",
                "Calle Estado 100, Santiago",
                "Despacho actualizado",
                LocalDate.of(2026, 6, 3),
                Email.create("nuevo.responsable@empresa.cl"),
                updateTime);

        assertEquals("Transportes Norte", guide.getCarrierName());
        assertEquals(GuideStatus.UPDATED, guide.getStatus());
        assertEquals(updateTime, guide.getUpdatedAt());
    }

    @Test
    void rejectsUpdateWhenGuideIsDeleted() {
        DispatchGuide guide = createSampleGuide();
        guide.markDeleted(NOW.plusSeconds(30));

        assertThrows(DomainError.class, () -> guide.update(
                "Transportista",
                "Destinatario",
                "Origen",
                "Destino",
                null,
                LocalDate.of(2026, 6, 2),
                Email.create("responsable@empresa.cl"),
                NOW.plusSeconds(60)));
    }

    @Test
    void marksGuideAsDeleted() {
        DispatchGuide guide = createSampleGuide();
        Instant deleteTime = NOW.plusSeconds(120);

        guide.markDeleted(deleteTime);

        assertTrue(guide.isDeleted());
        assertEquals(GuideStatus.DELETED, guide.getStatus());
        assertEquals(deleteTime, guide.getUpdatedAt());
    }

    @Test
    void rejectsDeleteWhenAlreadyDeleted() {
        DispatchGuide guide = createSampleGuide();
        guide.markDeleted(NOW.plusSeconds(30));

        assertThrows(DomainError.class, () -> guide.markDeleted(NOW.plusSeconds(60)));
    }

    @Test
    void isNotDeletedInitially() {
        DispatchGuide guide = createSampleGuide();

        assertFalse(guide.isDeleted());
    }

    @Test
    void marksPdfGeneratedWithEfsPathAndStatus() {
        DispatchGuide guide = createSampleGuide();
        Instant pdfTime = NOW.plusSeconds(30);
        String efsPath = "/tmp/efs/guides/2026-06-02/transportes-rapidos/guide-id.pdf";

        guide.markPdfGenerated(efsPath, pdfTime);

        assertEquals(GuideStatus.PDF_GENERATED, guide.getStatus());
        assertEquals(efsPath, guide.getEfsPath());
        assertEquals(pdfTime, guide.getUpdatedAt());
    }

    @Test
    void allowsRegeneratingPdfWhenAlreadyGenerated() {
        DispatchGuide guide = createSampleGuide();
        guide.markPdfGenerated("/tmp/first.pdf", NOW.plusSeconds(30));
        Instant regenerateTime = NOW.plusSeconds(60);
        String newPath = "/tmp/second.pdf";

        guide.markPdfGenerated(newPath, regenerateTime);

        assertEquals(GuideStatus.PDF_GENERATED, guide.getStatus());
        assertEquals(newPath, guide.getEfsPath());
        assertEquals(regenerateTime, guide.getUpdatedAt());
    }

    @Test
    void rejectsPdfGenerationWhenGuideIsDeleted() {
        DispatchGuide guide = createSampleGuide();
        guide.markDeleted(NOW.plusSeconds(30));

        assertThrows(DomainError.class, () -> guide.markPdfGenerated("/tmp/path.pdf", NOW.plusSeconds(60)));
    }

    @Test
    void marksUploadedToS3WithKeyAndStatus() {
        DispatchGuide guide = createSampleGuide();
        guide.markPdfGenerated("/tmp/efs/guide.pdf", NOW.plusSeconds(30));
        Instant uploadTime = NOW.plusSeconds(45);
        String s3Key = "guides/2026-06-02/transportes-rapidos/guide-id.pdf";

        guide.markUploadedToS3(s3Key, uploadTime);

        assertEquals(GuideStatus.UPLOADED_TO_S3, guide.getStatus());
        assertEquals(s3Key, guide.getS3Key());
        assertEquals("/tmp/efs/guide.pdf", guide.getEfsPath());
        assertEquals(uploadTime, guide.getUpdatedAt());
    }

    @Test
    void allowsReuploadingToS3WhenAlreadyUploaded() {
        DispatchGuide guide = createSampleGuide();
        guide.markPdfGenerated("/tmp/efs/guide.pdf", NOW.plusSeconds(30));
        guide.markUploadedToS3("guides/old-key.pdf", NOW.plusSeconds(45));
        Instant reuploadTime = NOW.plusSeconds(60);
        String newKey = "guides/2026-06-03/transportes-norte/guide-id.pdf";

        guide.markUploadedToS3(newKey, reuploadTime);

        assertEquals(GuideStatus.UPLOADED_TO_S3, guide.getStatus());
        assertEquals(newKey, guide.getS3Key());
        assertEquals(reuploadTime, guide.getUpdatedAt());
    }

    @Test
    void rejectsS3UploadWhenGuideIsDeleted() {
        DispatchGuide guide = createSampleGuide();
        guide.markDeleted(NOW.plusSeconds(30));

        assertThrows(DomainError.class, () -> guide.markUploadedToS3("guides/key.pdf", NOW.plusSeconds(60)));
    }

    private DispatchGuide createSampleGuide() {
        return DispatchGuide.create(
                GuideId.generate(),
                GuideNumber.create("GD-2026-000001"),
                "Transportes Rápidos",
                "María González",
                "Av. Providencia 1234, Santiago",
                "Calle Huérfanos 567, Santiago",
                "Electrónicos",
                LocalDate.of(2026, 6, 2),
                Email.create("responsable@empresa.cl"),
                NOW);
    }
}
