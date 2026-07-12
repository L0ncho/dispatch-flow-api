package com.dispatchflow.guides.unit.application;

import com.dispatchflow.guides.application.GuidePdfEfsStorage;
import com.dispatchflow.guides.application.ports.EfsStoragePort;
import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.services.GuidePdfPathBuilder;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.domain.valueobjects.GuideNumber;
import com.dispatchflow.guides.domain.valueobjects.GuideStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuidePdfEfsStorageTest {

    private static final Instant NOW = Instant.parse("2026-06-02T10:00:00Z");
    private static final byte[] PDF_BYTES = new byte[] {37, 80, 68, 70};

    private RecordingEfsStorage efsStorage;
    private GuidePdfEfsStorage guidePdfEfsStorage;

    @BeforeEach
    void setUp() {
        efsStorage = new RecordingEfsStorage();
        guidePdfEfsStorage = new GuidePdfEfsStorage(
                new GuidePdfPathBuilder(),
                guide -> PDF_BYTES,
                efsStorage);
    }

    @Test
    void storesPdfOnEfsAndMarksGuideAsGenerated() {
        DispatchGuide guide = createGuide();

        byte[] pdfContent = guidePdfEfsStorage.storeOnEfs(guide, NOW);

        assertEquals(GuideStatus.PDF_GENERATED, guide.getStatus());
        assertEquals("/efs/" + efsStorage.lastRelativePath, guide.getEfsPath());
        assertEquals(PDF_BYTES, efsStorage.lastContent);
        assertEquals(PDF_BYTES, pdfContent);
    }

    private DispatchGuide createGuide() {
        return DispatchGuide.create(
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
    }

    private static class RecordingEfsStorage implements EfsStoragePort {

        private String lastRelativePath;
        private byte[] lastContent;

        @Override
        public String write(String relativePath, byte[] content) {
            lastRelativePath = relativePath;
            lastContent = content;
            return "/efs/" + relativePath;
        }

        @Override
        public byte[] read(String absolutePath) {
            return lastContent;
        }
    }
}
