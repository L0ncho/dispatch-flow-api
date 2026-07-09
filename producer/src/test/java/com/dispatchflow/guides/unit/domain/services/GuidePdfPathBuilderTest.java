package com.dispatchflow.guides.unit.domain.services;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.services.GuidePdfPathBuilder;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.domain.valueobjects.GuideNumber;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuidePdfPathBuilderTest {

    private static final GuideId GUIDE_ID = GuideId.create("abc-123-def");
    private static final Instant NOW = Instant.parse("2026-06-02T10:00:00Z");

    private final GuidePdfPathBuilder pathBuilder = new GuidePdfPathBuilder();

    @Test
    void buildsRelativePathWithDateSlugAndId() {
        DispatchGuide guide = DispatchGuide.create(
                GUIDE_ID,
                GuideNumber.create("GD-2026-000001"),
                "Transportes Rápidos",
                "María González",
                "Av. Providencia 1234, Santiago",
                "Calle Huérfanos 567, Santiago",
                null,
                LocalDate.of(2026, 6, 2),
                Email.create("responsable@empresa.cl"),
                NOW);

        String relativePath = pathBuilder.buildRelativePath(guide);

        assertEquals("guides/2026-06-02/transportes-rapidos/guide-abc-123-def.pdf", relativePath);
    }

    @Test
    void slugifiesCarrierNameWithSpacesAndAccents() {
        DispatchGuide guide = DispatchGuide.create(
                GUIDE_ID,
                GuideNumber.create("GD-2026-000002"),
                "Envíos del Sur",
                "María González",
                "Origen",
                "Destino",
                null,
                LocalDate.of(2026, 6, 2),
                Email.create("responsable@empresa.cl"),
                NOW);

        String relativePath = pathBuilder.buildRelativePath(guide);

        assertEquals("guides/2026-06-02/envios-del-sur/guide-abc-123-def.pdf", relativePath);
    }
}
