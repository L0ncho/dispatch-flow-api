package com.dispatchflow.guides.infrastructure.adapters;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.domain.valueobjects.GuideNumber;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfBoxGuidePdfGeneratorTest {

    private final PdfBoxGuidePdfGenerator generator = new PdfBoxGuidePdfGenerator();

    @Test
    void generatesValidPdfBytes() {
        DispatchGuide guide = DispatchGuide.create(
                GuideId.create("guide-123"),
                GuideNumber.create("GD-2026-000001"),
                "Transportes Rápidos",
                "María González",
                "Av. Providencia 1234, Santiago",
                "Calle Huérfanos 567, Santiago",
                "Electrónicos",
                LocalDate.of(2026, 6, 2),
                Email.create("responsable@empresa.cl"),
                Instant.parse("2026-06-02T10:00:00Z"));

        byte[] pdfBytes = generator.generate(guide);

        assertTrue(pdfBytes.length > 100);
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
    }
}
