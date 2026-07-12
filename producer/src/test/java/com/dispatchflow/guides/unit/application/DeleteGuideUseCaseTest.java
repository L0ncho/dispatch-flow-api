package com.dispatchflow.guides.unit.application;

import com.dispatchflow.guides.application.DeleteGuideUseCase;
import com.dispatchflow.guides.application.UpdateGuideUseCase;
import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.application.dto.UpdateGuideCommand;
import com.dispatchflow.guides.application.ports.ObjectStoragePort;
import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.repositories.GuideRepository;
import com.dispatchflow.guides.domain.repositories.InMemoryGuideRepository;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.domain.valueobjects.GuideStatus;
import com.dispatchflow.guides.unit.application.support.GuideApplicationTestSupport;
import com.dispatchflow.shared.domain.DomainError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeleteGuideUseCaseTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-06-02T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryGuideRepository repository;
    private GuideApplicationTestSupport.InMemoryObjectStorage objectStorage;
    private DeleteGuideUseCase deleteGuideUseCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryGuideRepository();
        objectStorage = GuideApplicationTestSupport.inMemoryObjectStorage();
        deleteGuideUseCase = GuideApplicationTestSupport.deleteGuideUseCase(repository, FIXED_CLOCK, objectStorage);
    }

    @Test
    void marksGuideAsDeletedAndRemovesPdfFromS3() {
        GuideResponse created = GuideApplicationTestSupport.seedGuide(repository, FIXED_CLOCK, "Transportes Rápidos", objectStorage);
        String s3Key = created.s3Key();
        assertTrue(objectStorage.contains(s3Key));

        deleteGuideUseCase.execute(created.id());

        DispatchGuide stored = repository.findById(GuideId.create(created.id())).orElseThrow();
        assertEquals(GuideStatus.DELETED, stored.getStatus());
        assertTrue(stored.isDeleted());
        assertFalse(objectStorage.contains(s3Key));
    }

    @Test
    void removesAllS3ObjectsAfterGuideWasUpdated() {
        GuideResponse created = GuideApplicationTestSupport.seedGuide(repository, FIXED_CLOCK, "Transportes Rápidos", objectStorage);
        String originalS3Key = created.s3Key();

        UpdateGuideUseCase updateGuideUseCase = GuideApplicationTestSupport.updateGuideUseCase(
                repository,
                Clock.fixed(Instant.parse("2026-06-02T11:00:00Z"), ZoneOffset.UTC),
                objectStorage);
        GuideResponse updated = updateGuideUseCase.execute(created.id(), new UpdateGuideCommand(
                "Transportes Norte",
                "Juan Pérez",
                "Av. Libertador 99, Santiago",
                "Calle Estado 100, Santiago",
                "Despacho actualizado",
                LocalDate.of(2026, 6, 3),
                "nuevo.responsable@empresa.cl"));

        deleteGuideUseCase.execute(created.id());

        assertFalse(objectStorage.contains(originalS3Key));
        assertFalse(objectStorage.contains(updated.s3Key()));
    }

    @Test
    void throwsNotFoundWhenGuideDoesNotExist() {
        assertThrows(DomainError.class, () -> deleteGuideUseCase.execute("missing-id"));
    }

    @Test
    void persistsDeletedStatusBeforeRemovingS3Object() {
        GuideResponse created = GuideApplicationTestSupport.seedGuide(repository, FIXED_CLOCK, "Transportes Rápidos", objectStorage);
        java.util.List<String> callOrder = new java.util.ArrayList<>();
        GuideRepository orderingRepository = new GuideRepository() {
            @Override
            public void save(DispatchGuide guide) {
                callOrder.add("save:" + guide.getStatus());
                repository.save(guide);
            }

            @Override
            public java.util.Optional<DispatchGuide> findById(GuideId id) {
                return repository.findById(id);
            }

            @Override
            public java.util.List<DispatchGuide> findAllActive() {
                return repository.findAllActive();
            }

            @Override
            public java.util.List<DispatchGuide> findByCarrierAndDispatchDate(
                    String carrierName, LocalDate dispatchDate) {
                return repository.findByCarrierAndDispatchDate(carrierName, dispatchDate);
            }

            @Override
            public long nextSequence() {
                return repository.nextSequence();
            }
        };
        ObjectStoragePort orderingStorage = new ObjectStoragePort() {
            @Override
            public void store(String key, byte[] content) {
                objectStorage.store(key, content);
            }

            @Override
            public byte[] read(String key) {
                return objectStorage.read(key);
            }

            @Override
            public void delete(String key) {
                callOrder.add("s3-delete");
                objectStorage.delete(key);
            }
        };
        DeleteGuideUseCase orderedDelete = new DeleteGuideUseCase(orderingRepository, orderingStorage, FIXED_CLOCK);

        orderedDelete.execute(created.id());

        assertEquals(java.util.List.of("save:DELETED", "s3-delete"), callOrder);
        assertEquals(GuideStatus.DELETED, repository.findById(GuideId.create(created.id())).orElseThrow().getStatus());
    }
}
