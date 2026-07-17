package com.dispatchflow.consumer.unit.application;

import com.dispatchflow.consumer.application.ProcessGuideMessageUseCase;
import com.dispatchflow.consumer.infrastructure.persistence.AsyncDispatchGuideEntity;
import com.dispatchflow.consumer.infrastructure.persistence.AsyncDispatchGuideRepository;
import com.dispatchflow.consumer.infrastructure.persistence.AsyncGuideSequenceRepository;
import com.dispatchflow.guides.application.GuidePdfEfsStorage;
import com.dispatchflow.guides.application.GuidePdfS3Storage;
import com.dispatchflow.guides.application.ports.EfsStoragePort;
import com.dispatchflow.guides.application.ports.ObjectStoragePort;
import com.dispatchflow.guides.domain.services.GuideNumberGenerator;
import com.dispatchflow.guides.domain.services.GuidePdfPathBuilder;
import com.dispatchflow.shared.messaging.GuideCreationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProcessGuideMessageUseCaseTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-02T10:00:00Z"), ZoneOffset.UTC);

    private ProcessGuideMessageUseCase useCase;
    private InMemoryObjectStorage objectStorage;
    private InMemoryAsyncDispatchGuideRepository asyncDispatchGuideRepository;

    @BeforeEach
    void setUp() {
        objectStorage = new InMemoryObjectStorage();
        asyncDispatchGuideRepository = new InMemoryAsyncDispatchGuideRepository();
        GuidePdfEfsStorage efsStorage = new GuidePdfEfsStorage(
                new GuidePdfPathBuilder(),
                guide -> new byte[] {37, 80, 68, 70},
                stubEfsStorage());
        GuidePdfS3Storage s3Storage = new GuidePdfS3Storage(new GuidePdfPathBuilder(), objectStorage);
        useCase = new ProcessGuideMessageUseCase(
                new InMemoryAsyncGuideSequenceRepository(),
                asyncDispatchGuideRepository,
                new GuideNumberGenerator(),
                efsStorage,
                s3Storage,
                "dispatch-flow-local",
                FIXED_CLOCK);
    }

    @Test
    void processesValidMessageAndPersistsAsyncDispatchGuide() {
        GuideCreationMessage message = GuideCreationMessage.create(
                "track-123",
                "Transportes Rápidos",
                "María González",
                "Av. Providencia 1234",
                "Calle Huérfanos 567",
                "Electrónicos",
                LocalDate.of(2026, 6, 2),
                "responsable@empresa.cl",
                Instant.parse("2026-06-02T09:00:00Z"));

        String guideId = useCase.execute(message);

        AsyncDispatchGuideEntity saved = asyncDispatchGuideRepository.savedEntities().getFirst();
        assertEquals("track-123", saved.getTrackingId());
        assertEquals(guideId, saved.getGuideId());
        assertEquals(AsyncDispatchGuideEntity.ProcessingStatus.PROCESSED, saved.getStatus());
        assertEquals("dispatch-flow-local", saved.getS3Bucket());
        assertNotNull(saved.getS3Key());
        assertNotNull(saved.getGuideNumber());
        assertEquals(true, objectStorage.contains(saved.getS3Key()));
    }

    private static EfsStoragePort stubEfsStorage() {
        return new EfsStoragePort() {
            @Override
            public String write(String relativePath, byte[] content) {
                return "/efs/" + relativePath;
            }

            @Override
            public byte[] read(String absolutePath) {
                return new byte[] {37, 80, 68, 70};
            }
        };
    }

    static class InMemoryAsyncGuideSequenceRepository extends AsyncGuideSequenceRepository {

        private final AtomicLong sequence = new AtomicLong();

        InMemoryAsyncGuideSequenceRepository() {
            super(null);
        }

        @Override
        public long nextSequence() {
            return sequence.incrementAndGet();
        }
    }

    static class InMemoryAsyncDispatchGuideRepository extends AsyncDispatchGuideRepository {

        private final List<AsyncDispatchGuideEntity> entities = new ArrayList<>();

        InMemoryAsyncDispatchGuideRepository() {
            super(null);
        }

        @Override
        public AsyncDispatchGuideEntity save(AsyncDispatchGuideEntity entity) {
            entities.add(entity);
            return entity;
        }

        List<AsyncDispatchGuideEntity> savedEntities() {
            return entities;
        }
    }

    static class InMemoryObjectStorage implements ObjectStoragePort {

        private final Map<String, byte[]> objects = new HashMap<>();

        @Override
        public void store(String key, byte[] content) {
            objects.put(key, content);
        }

        @Override
        public byte[] read(String key) {
            return objects.get(key);
        }

        @Override
        public void delete(String key) {
            objects.remove(key);
        }

        boolean contains(String key) {
            return objects.containsKey(key);
        }
    }
}
