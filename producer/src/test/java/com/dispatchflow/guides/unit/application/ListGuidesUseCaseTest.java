package com.dispatchflow.guides.unit.application;

import com.dispatchflow.guides.application.DeleteGuideUseCase;
import com.dispatchflow.guides.application.ListGuidesUseCase;
import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.domain.repositories.InMemoryGuideRepository;
import com.dispatchflow.guides.unit.application.support.GuideApplicationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListGuidesUseCaseTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-06-02T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryGuideRepository repository;
    private ListGuidesUseCase listGuidesUseCase;
    private DeleteGuideUseCase deleteGuideUseCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryGuideRepository();
        listGuidesUseCase = new ListGuidesUseCase(repository);
        deleteGuideUseCase = GuideApplicationTestSupport.deleteGuideUseCase(
                repository, FIXED_CLOCK, GuideApplicationTestSupport.inMemoryObjectStorage());
    }

    @Test
    void listsOnlyActiveGuides() {
        GuideResponse active = GuideApplicationTestSupport.seedGuide(repository, FIXED_CLOCK, "Transportes Activos");
        GuideResponse toDelete = GuideApplicationTestSupport.seedGuide(repository, FIXED_CLOCK, "Transportes Eliminados");
        deleteGuideUseCase.execute(toDelete.id());

        List<GuideResponse> guides = listGuidesUseCase.execute();

        assertEquals(1, guides.size());
        assertEquals(active.id(), guides.getFirst().id());
    }
}
