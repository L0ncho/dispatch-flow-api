package com.dispatchflow.guides.unit.application;

import com.dispatchflow.guides.application.DeleteGuideUseCase;
import com.dispatchflow.guides.application.SearchGuidesUseCase;
import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.domain.repositories.InMemoryGuideRepository;
import com.dispatchflow.guides.unit.application.support.GuideApplicationTestSupport;
import com.dispatchflow.shared.domain.DomainError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchGuidesUseCaseTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-06-02T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryGuideRepository repository;
    private SearchGuidesUseCase searchGuidesUseCase;
    private DeleteGuideUseCase deleteGuideUseCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryGuideRepository();
        searchGuidesUseCase = new SearchGuidesUseCase(repository);
        deleteGuideUseCase = GuideApplicationTestSupport.deleteGuideUseCase(
                repository, FIXED_CLOCK, GuideApplicationTestSupport.inMemoryObjectStorage());
    }

    @Test
    void findsGuidesByCarrierAndExactDate() {
        GuideResponse match = GuideApplicationTestSupport.seedGuide(repository, FIXED_CLOCK, "Transportes Rápidos");
        GuideApplicationTestSupport.seedGuide(repository, FIXED_CLOCK, "Envíos del Sur");

        List<GuideResponse> results = searchGuidesUseCase.execute("Transportes Rápidos", LocalDate.of(2026, 6, 2));

        assertEquals(1, results.size());
        assertEquals(match.id(), results.getFirst().id());
    }

    @Test
    void excludesDeletedGuidesFromSearch() {
        GuideResponse guide = GuideApplicationTestSupport.seedGuide(repository, FIXED_CLOCK, "Transportes Rápidos");
        deleteGuideUseCase.execute(guide.id());

        List<GuideResponse> results = searchGuidesUseCase.execute("Transportes Rápidos", LocalDate.of(2026, 6, 2));

        assertTrue(results.isEmpty());
    }

    @Test
    void rejectsSearchWithoutCarrierName() {
        assertThrows(DomainError.class, () -> searchGuidesUseCase.execute("  ", LocalDate.of(2026, 6, 2)));
    }

    @Test
    void rejectsSearchWithoutDate() {
        assertThrows(DomainError.class, () -> searchGuidesUseCase.execute("Transportes Rápidos", null));
    }
}
