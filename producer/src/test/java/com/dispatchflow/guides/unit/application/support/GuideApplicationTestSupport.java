package com.dispatchflow.guides.unit.application.support;

import com.dispatchflow.guides.application.DeleteGuideUseCase;
import com.dispatchflow.guides.application.DownloadGuidePdfUseCase;
import com.dispatchflow.guides.application.GuidePdfEfsStorage;
import com.dispatchflow.guides.application.GuidePdfS3Storage;
import com.dispatchflow.guides.application.UpdateGuideUseCase;
import com.dispatchflow.guides.application.ports.EfsStoragePort;
import com.dispatchflow.guides.application.ports.ObjectStoragePort;
import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.repositories.GuideRepository;
import com.dispatchflow.guides.domain.services.GuideNumberGenerator;
import com.dispatchflow.guides.domain.services.GuidePdfPathBuilder;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.shared.domain.DomainError;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public final class GuideApplicationTestSupport {

    public static final byte[] PDF_BYTES = new byte[] {37, 80, 68, 70};

    private GuideApplicationTestSupport() {
    }

    public static GuideResponse seedGuide(GuideRepository repository, Clock clock) {
        return seedGuide(repository, clock, "Transportes Rápidos", null);
    }

    public static GuideResponse seedGuide(GuideRepository repository, Clock clock, String carrierName) {
        return seedGuide(repository, clock, carrierName, null);
    }

    public static GuideResponse seedGuide(
            GuideRepository repository,
            Clock clock,
            String carrierName,
            ObjectStoragePort objectStorage) {
        long sequence = repository.nextSequence();
        GuideId guideId = GuideId.generate();
        Instant now = Instant.now(clock);
        DispatchGuide guide = DispatchGuide.create(
                guideId,
                new GuideNumberGenerator().generate(sequence),
                carrierName,
                "María González",
                "Av. Providencia 1234, Santiago",
                "Calle Huérfanos 567, Santiago",
                "Electrónicos",
                LocalDate.of(2026, 6, 2),
                Email.create("responsable@empresa.cl"),
                now);
        guide.markPdfGenerated("/efs/guide-" + guideId.value() + ".pdf", now);
        guide.markUploadedToS3("guides/2026-06-02/transportes-rapidos/guide-" + guideId.value() + ".pdf", now);
        if (objectStorage != null) {
            objectStorage.store(guide.getS3Key(), PDF_BYTES);
        }
        repository.save(guide);
        return GuideResponse.from(guide);
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
