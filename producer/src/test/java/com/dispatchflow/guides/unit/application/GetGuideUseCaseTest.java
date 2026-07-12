package com.dispatchflow.guides.unit.application;

import com.dispatchflow.guides.application.DeleteGuideUseCase;
import com.dispatchflow.guides.application.GetGuideUseCase;
import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.domain.repositories.InMemoryGuideRepository;
import com.dispatchflow.guides.unit.application.support.GuideApplicationTestSupport;
import com.dispatchflow.shared.domain.DomainError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetGuideUseCaseTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-06-02T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryGuideRepository repository;
    private GetGuideUseCase getGuideUseCase;
    private DeleteGuideUseCase deleteGuideUseCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryGuideRepository();
        getGuideUseCase = new GetGuideUseCase(repository);
        deleteGuideUseCase = GuideApplicationTestSupport.deleteGuideUseCase(
                repository, FIXED_CLOCK, GuideApplicationTestSupport.inMemoryObjectStorage());
    }

    @Test
    void returnsGuideById() {
        GuideResponse created = GuideApplicationTestSupport.seedGuide(repository, FIXED_CLOCK);

        GuideResponse found = getGuideUseCase.execute(created.id());

        assertEquals(created.id(), found.id());
    }

    @Test
    void throwsNotFoundWhenGuideDoesNotExist() {
        DomainError error = assertThrows(DomainError.class, () -> getGuideUseCase.execute("missing-id"));

        assertEquals(DomainError.Type.NOT_FOUND, error.getType());
    }

    @Test
    void throwsNotFoundWhenGuideIsDeleted() {
        GuideResponse created = GuideApplicationTestSupport.seedGuide(repository, FIXED_CLOCK);
        deleteGuideUseCase.execute(created.id());

        assertThrows(DomainError.class, () -> getGuideUseCase.execute(created.id()));
    }
}
