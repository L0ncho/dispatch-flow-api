package com.dispatchflow.shared.messaging;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GuideCreationMessageTest {

    @Test
    void createsMessageWithRequiredFields() {
        GuideCreationMessage message = GuideCreationMessage.create(
                "track-1",
                "Transportes Rápidos",
                "María González",
                "Av. Providencia 1234",
                "Calle Huérfanos 567",
                "Electrónicos",
                LocalDate.of(2026, 6, 2),
                "responsable@empresa.cl",
                Instant.parse("2026-06-02T10:00:00Z"));

        assertEquals("track-1", message.trackingId());
        assertEquals("Transportes Rápidos", message.carrierName());
        assertEquals("responsable@empresa.cl", message.ownerEmail());
    }

    @Test
    void rejectsMessageWithoutTrackingId() {
        assertThrows(
                com.dispatchflow.shared.domain.DomainError.class,
                () -> GuideCreationMessage.create(
                        " ",
                        "Transportes Rápidos",
                        "María González",
                        "Av. Providencia 1234",
                        "Calle Huérfanos 567",
                        null,
                        LocalDate.of(2026, 6, 2),
                        "responsable@empresa.cl",
                        Instant.parse("2026-06-02T10:00:00Z")));
    }
}
