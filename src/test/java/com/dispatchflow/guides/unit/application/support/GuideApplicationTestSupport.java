package com.dispatchflow.guides.unit.application.support;

import com.dispatchflow.guides.application.CreateGuideUseCase;
import com.dispatchflow.guides.application.DeleteGuideUseCase;
import com.dispatchflow.guides.application.DownloadGuidePdfUseCase;
import com.dispatchflow.guides.application.GuidePdfEfsStorage;
import com.dispatchflow.guides.application.GuidePdfS3Storage;
import com.dispatchflow.guides.application.UpdateGuideUseCase;
import com.dispatchflow.guides.application.ports.EfsStoragePort;
import com.dispatchflow.guides.application.ports.ObjectStoragePort;
import com.dispatchflow.guides.domain.repositories.GuideRepository;
import com.dispatchflow.guides.domain.services.GuideNumberGenerator;
import com.dispatchflow.guides.domain.services.GuidePdfPathBuilder;
import com.dispatchflow.guides.infrastructure.adapters.RabbitMQGuidePublisher;
import com.dispatchflow.shared.domain.DomainError;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;

public final class GuideApplicationTestSupport {

    public static final byte[] PDF_BYTES = new byte[] {37, 80, 68, 70};

    private GuideApplicationTestSupport() {
    }

    public static CreateGuideUseCase createGuideUseCase(GuideRepository repository, Clock clock) {
        return createGuideUseCase(repository, clock, inMemoryObjectStorage(), Mockito.mock(RabbitMQGuidePublisher.class));
    }

    public static CreateGuideUseCase createGuideUseCase(
            GuideRepository repository,
            Clock clock,
            ObjectStoragePort objectStorage, 
            RabbitMQGuidePublisher rabbitMQGuidePublisher){
        return new CreateGuideUseCase(
                repository,
                new GuideNumberGenerator(),
                guidePdfEfsStorage(),
                guidePdfS3Storage(objectStorage),
                rabbitMQGuidePublisher,
                clock);
    }
    public static CreateGuideUseCase createGuideUseCase(GuideRepository repository, Clock clock, ObjectStoragePort objectStorage) {
        return createGuideUseCase(repository, clock, objectStorage, Mockito.mock(RabbitMQGuidePublisher.class));
    }

    public static UpdateGuideUseCase updateGuideUseCase(
            GuideRepository repository,
            Clock clock,
            ObjectStoragePort objectStorage) {
        return new UpdateGuideUseCase(
                repository,
                guidePdfEfsStorage(),
                guidePdfS3Storage(objectStorage),
                clock);
    }

    public static DeleteGuideUseCase deleteGuideUseCase(
            GuideRepository repository,
            Clock clock,
            ObjectStoragePort objectStorage) {
        return new DeleteGuideUseCase(repository, objectStorage, clock);
    }

    public static DownloadGuidePdfUseCase downloadGuidePdfUseCase(
            GuideRepository repository,
            ObjectStoragePort objectStorage) {
        return new DownloadGuidePdfUseCase(repository, objectStorage, stubEfsStorage());
    }

    public static GuidePdfEfsStorage guidePdfEfsStorage() {
        return new GuidePdfEfsStorage(
                new GuidePdfPathBuilder(),
                guide -> PDF_BYTES,
                stubEfsStorage());
    }

    public static GuidePdfS3Storage guidePdfS3Storage(ObjectStoragePort objectStorage) {
        return new GuidePdfS3Storage(new GuidePdfPathBuilder(), objectStorage);
    }

    public static InMemoryObjectStorage inMemoryObjectStorage() {
        return new InMemoryObjectStorage();
    }

    public static EfsStoragePort stubEfsStorage() {
        return new EfsStoragePort() {
            @Override
            public String write(String relativePath, byte[] content) {
                return "/efs/" + relativePath;
            }

            @Override
            public byte[] read(String absolutePath) {
                return PDF_BYTES;
            }
        };
    }

    public static class InMemoryObjectStorage implements ObjectStoragePort {

        private final Map<String, byte[]> objects = new HashMap<>();

        @Override
        public void store(String key, byte[] content) {
            objects.put(key, content);
        }

        @Override
        public byte[] read(String key) {
            byte[] content = objects.get(key);
            if (content == null) {
                throw DomainError.notFound("Object " + key + " not found in storage");
            }
            return content;
        }

        @Override
        public void delete(String key) {
            objects.remove(key);
        }

        public boolean contains(String key) {
            return objects.containsKey(key);
        }
    }
}
