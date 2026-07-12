package com.dispatchflow.guides.unit.domain.repositories;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.repositories.InMemoryGuideRepository;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.domain.valueobjects.GuideNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryGuideRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-06-02T10:00:00Z");

    private InMemoryGuideRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryGuideRepository();
    }

    @Test
    void savesAndFindsGuideById() {
        DispatchGuide guide = createGuide("Transportista A");
        repository.save(guide);

        assertTrue(repository.findById(guide.getId()).isPresent());
    }

    @Test
    void findAllActiveExcludesDeletedGuides() {
        DispatchGuide active = createGuide("Transportista A");
        DispatchGuide deleted = createGuide("Transportista B");
        deleted.markDeleted(NOW);
        repository.save(active);
        repository.save(deleted);

        List<DispatchGuide> activeGuides = repository.findAllActive();

        assertEquals(1, activeGuides.size());
        assertEquals(active.getId(), activeGuides.getFirst().getId());
    }

    @Test
    void findsByCarrierAndDispatchDate() {
        DispatchGuide match = createGuide("Transportes Rápidos");
        DispatchGuide otherCarrier = createGuide("Envíos del Sur");
        repository.save(match);
        repository.save(otherCarrier);

        List<DispatchGuide> results = repository.findByCarrierAndDispatchDate(
                "Transportes Rápidos", LocalDate.of(2026, 6, 2));

        assertEquals(1, results.size());
        assertEquals(match.getId(), results.getFirst().getId());
    }

    @Test
    void searchExcludesDeletedGuides() {
        DispatchGuide deleted = createGuide("Transportes Rápidos");
        deleted.markDeleted(NOW);
        repository.save(deleted);

        List<DispatchGuide> results = repository.findByCarrierAndDispatchDate(
                "Transportes Rápidos", LocalDate.of(2026, 6, 2));

        assertTrue(results.isEmpty());
    }

    @Test
    void incrementsSequence() {
        long first = repository.nextSequence();
        long second = repository.nextSequence();

        assertEquals(1, first);
        assertEquals(2, second);
    }

    @Test
    void returnsEmptyWhenGuideNotFound() {
        assertFalse(repository.findById(GuideId.generate()).isPresent());
    }

    private DispatchGuide createGuide(String carrierName) {
        return DispatchGuide.create(
                GuideId.generate(),
                GuideNumber.create("GD-2026-000001"),
                carrierName,
                "María González",
                "Av. Providencia 1234, Santiago",
                "Calle Huérfanos 567, Santiago",
                null,
                LocalDate.of(2026, 6, 2),
                Email.create("responsable@empresa.cl"),
                NOW);
    }
}
