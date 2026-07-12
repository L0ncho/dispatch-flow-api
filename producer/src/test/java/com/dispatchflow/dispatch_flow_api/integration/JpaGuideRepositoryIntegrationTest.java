package com.dispatchflow.dispatch_flow_api.integration;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.domain.valueobjects.GuideNumber;
import com.dispatchflow.guides.infrastructure.adapters.JpaGuideRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaGuideRepository.class)
class JpaGuideRepositoryIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-06-02T10:00:00Z");

    @Autowired
    private JpaGuideRepository repository;

    @Test
    void persistsAndFindsGuideById() {
        DispatchGuide guide = createGuide("Transportista A", GuideNumber.create("GD-2026-000001"));

        repository.save(guide);

        assertTrue(repository.findById(guide.getId()).isPresent());
    }

    @Test
    void findAllActiveExcludesDeletedGuides() {
        DispatchGuide active = createGuide("Transportista A", GuideNumber.create("GD-2026-000001"));
        DispatchGuide deleted = createGuide("Transportista B", GuideNumber.create("GD-2026-000002"));
        deleted.markDeleted(NOW);
        repository.save(active);
        repository.save(deleted);

        List<DispatchGuide> activeGuides = repository.findAllActive();

        assertEquals(1, activeGuides.size());
    }

    @Test
    void findsByCarrierAndDispatchDate() {
        DispatchGuide match = createGuide("Transportes Rápidos", GuideNumber.create("GD-2026-000003"));
        DispatchGuide other = createGuide("Envíos del Sur", GuideNumber.create("GD-2026-000004"));
        repository.save(match);
        repository.save(other);

        List<DispatchGuide> results = repository.findByCarrierAndDispatchDate(
                "Transportes Rápidos", LocalDate.of(2026, 6, 2));

        assertEquals(1, results.size());
        assertEquals(match.getId(), results.getFirst().getId());
    }

    @Test
    void incrementsSequence() {
        long first = repository.nextSequence();
        long second = repository.nextSequence();

        assertTrue(second > first);
    }

    @Test
    void returnsEmptyWhenGuideNotFound() {
        assertFalse(repository.findById(GuideId.generate()).isPresent());
    }

    private DispatchGuide createGuide(String carrierName, GuideNumber guideNumber) {
        return DispatchGuide.create(
                GuideId.generate(),
                guideNumber,
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
