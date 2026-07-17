package com.dispatchflow.guides.unit.infrastructure.adapters;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.repositories.InMemoryGuideRepository;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.domain.valueobjects.GuideNumber;
import com.dispatchflow.guides.domain.valueobjects.GuideStatus;
import com.dispatchflow.guides.infrastructure.adapters.AsyncGuideStore;
import com.dispatchflow.guides.infrastructure.adapters.CompositeGuideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompositeGuideRepositoryTest {

    /*
     * 1. findAllActive une legacy + async
     * 2. findById prioriza legacy
     * 3. save de guía solo-async actualiza async
     * 4. duplicados: gana legacy
     * 5. FAILED no aparece (store no lo indexa como activo)
     */

    private InMemoryGuideRepository legacyRepository;
    private InMemoryAsyncGuideStore asyncGuideStore;
    private CompositeGuideRepository composite;

    @BeforeEach
    void setUp() {
        legacyRepository = new InMemoryGuideRepository();
        asyncGuideStore = new InMemoryAsyncGuideStore();
        composite = new CompositeGuideRepository(legacyRepository, asyncGuideStore);
    }

    @Test
    void findAllActiveMergesLegacyAndAsyncGuides() {
        legacyRepository.save(guide("legacy-1", "Transportes A", GuideStatus.UPLOADED_TO_S3));
        asyncGuideStore.save(guide("async-1", "Transportes B", GuideStatus.UPLOADED_TO_S3));

        List<DispatchGuide> guides = composite.findAllActive();

        assertEquals(2, guides.size());
        assertTrue(guides.stream().anyMatch(g -> g.getId().value().equals("legacy-1")));
        assertTrue(guides.stream().anyMatch(g -> g.getId().value().equals("async-1")));
    }

    @Test
    void findByIdPrefersLegacyWhenPresentInBoth() {
        legacyRepository.save(guide("shared-1", "Legacy Carrier", GuideStatus.UPLOADED_TO_S3));
        asyncGuideStore.save(guide("shared-1", "Async Carrier", GuideStatus.UPLOADED_TO_S3));

        DispatchGuide found = composite.findById(GuideId.create("shared-1")).orElseThrow();

        assertEquals("Legacy Carrier", found.getCarrierName());
    }

    @Test
    void findByIdFallsBackToAsync() {
        asyncGuideStore.save(guide("async-2", "Solo Async", GuideStatus.UPLOADED_TO_S3));

        Optional<DispatchGuide> found = composite.findById(GuideId.create("async-2"));

        assertTrue(found.isPresent());
        assertEquals("Solo Async", found.get().getCarrierName());
    }

    @Test
    void saveUpdatesAsyncWhenGuideExistsOnlyThere() {
        asyncGuideStore.save(guide("async-3", "Antes", GuideStatus.UPLOADED_TO_S3));
        DispatchGuide updated = guide("async-3", "Después", GuideStatus.UPLOADED_TO_S3);

        composite.save(updated);

        assertEquals("Después", asyncGuideStore.findByGuideId(GuideId.create("async-3")).orElseThrow().getCarrierName());
        assertTrue(legacyRepository.findById(GuideId.create("async-3")).isEmpty());
    }

    @Test
    void findAllActivePrefersLegacyOnDuplicateIds() {
        legacyRepository.save(guide("dup-1", "Legacy Carrier", GuideStatus.UPLOADED_TO_S3));
        asyncGuideStore.save(guide("dup-1", "Async Carrier", GuideStatus.UPLOADED_TO_S3));

        List<DispatchGuide> guides = composite.findAllActive();

        assertEquals(1, guides.size());
        assertEquals("Legacy Carrier", guides.getFirst().getCarrierName());
    }

    @Test
    void findByCarrierAndDispatchDateIncludesAsyncMatches() {
        LocalDate date = LocalDate.of(2026, 6, 2);
        asyncGuideStore.save(guide("async-4", "Transportes Rápidos", GuideStatus.UPLOADED_TO_S3));

        List<DispatchGuide> guides = composite.findByCarrierAndDispatchDate("Transportes Rápidos", date);

        assertEquals(1, guides.size());
        assertEquals("async-4", guides.getFirst().getId().value());
    }

    private static DispatchGuide guide(String id, String carrierName, GuideStatus status) {
        return DispatchGuide.restore(
                GuideId.create(id),
                GuideNumber.create("GD-2026-000001"),
                carrierName,
                "Recipient",
                "Origin",
                "Destination",
                "Desc",
                LocalDate.of(2026, 6, 2),
                Instant.parse("2026-06-02T09:00:00Z"),
                Instant.parse("2026-06-02T10:00:00Z"),
                Email.create("owner@empresa.cl"),
                status,
                "/efs/a.pdf",
                "s3/a.pdf");
    }

    static class InMemoryAsyncGuideStore implements AsyncGuideStore {

        private final Map<String, DispatchGuide> guides = new HashMap<>();

        @Override
        public Optional<DispatchGuide> findByGuideId(GuideId id) {
            return Optional.ofNullable(guides.get(id.value()));
        }

        @Override
        public boolean existsByGuideId(GuideId id) {
            return guides.containsKey(id.value());
        }

        @Override
        public List<DispatchGuide> findAllActive() {
            return guides.values().stream().filter(g -> !g.isDeleted()).toList();
        }

        @Override
        public List<DispatchGuide> findActiveByCarrierAndDispatchDate(String carrierName, LocalDate dispatchDate) {
            return guides.values().stream()
                    .filter(g -> !g.isDeleted())
                    .filter(g -> g.getCarrierName().equals(carrierName) && g.getDispatchDate().equals(dispatchDate))
                    .toList();
        }

        @Override
        public void save(DispatchGuide guide) {
            guides.put(guide.getId().value(), guide);
        }
    }
}
