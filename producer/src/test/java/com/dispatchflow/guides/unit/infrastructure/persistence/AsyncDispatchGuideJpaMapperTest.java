package com.dispatchflow.guides.unit.infrastructure.persistence;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.domain.valueobjects.GuideNumber;
import com.dispatchflow.guides.domain.valueobjects.GuideStatus;
import com.dispatchflow.guides.infrastructure.persistence.AsyncDispatchGuideJpaEntity;
import com.dispatchflow.guides.infrastructure.persistence.AsyncDispatchGuideJpaMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AsyncDispatchGuideJpaMapperTest {

    /*
     * 1. PROCESSED → UPLOADED_TO_S3
     * 2. DELETED → DELETED
     * 3. FAILED → null (excluded)
     * 4. applyDomainToEntity updates fields and maps DELETED
     */

    @Test
    void mapsProcessedEntityToUploadedGuide() {
        AsyncDispatchGuideJpaEntity entity = baseEntity();
        entity.setStatus(AsyncDispatchGuideJpaEntity.ProcessingStatus.PROCESSED);

        DispatchGuide guide = AsyncDispatchGuideJpaMapper.toDomain(entity);

        assertEquals("guide-1", guide.getId().value());
        assertEquals("GD-2026-000001", guide.getGuideNumber().value());
        assertEquals(GuideStatus.UPLOADED_TO_S3, guide.getStatus());
        assertEquals(entity.getReceivedAt().toInstant(ZoneOffset.UTC), guide.getCreatedAt());
        assertEquals(entity.getProcessedAt().toInstant(ZoneOffset.UTC), guide.getUpdatedAt());
        assertEquals("s3/key.pdf", guide.getS3Key());
        assertEquals("/efs/path.pdf", guide.getEfsPath());
    }

    @Test
    void mapsDeletedEntityToDeletedGuide() {
        AsyncDispatchGuideJpaEntity entity = baseEntity();
        entity.setStatus(AsyncDispatchGuideJpaEntity.ProcessingStatus.DELETED);

        DispatchGuide guide = AsyncDispatchGuideJpaMapper.toDomain(entity);

        assertEquals(GuideStatus.DELETED, guide.getStatus());
    }

    @Test
    void returnsNullForFailedEntity() {
        AsyncDispatchGuideJpaEntity entity = baseEntity();
        entity.setStatus(AsyncDispatchGuideJpaEntity.ProcessingStatus.FAILED);

        assertNull(AsyncDispatchGuideJpaMapper.toDomain(entity));
    }

    @Test
    void appliesDomainChangesOntoExistingEntity() {
        AsyncDispatchGuideJpaEntity entity = baseEntity();
        entity.setStatus(AsyncDispatchGuideJpaEntity.ProcessingStatus.PROCESSED);
        entity.setTrackingId("track-keep");

        DispatchGuide updated = DispatchGuide.restore(
                GuideId.create("guide-1"),
                GuideNumber.create("GD-2026-000001"),
                "Transportes Norte",
                "Juan Pérez",
                "Origen nuevo",
                "Destino nuevo",
                "Desc",
                LocalDate.of(2026, 6, 3),
                Instant.parse("2026-06-02T09:00:00Z"),
                Instant.parse("2026-06-03T12:00:00Z"),
                Email.create("nuevo@empresa.cl"),
                GuideStatus.DELETED,
                "/efs/new.pdf",
                "s3/new.pdf");

        AsyncDispatchGuideJpaMapper.applyDomainToEntity(updated, entity);

        assertEquals("track-keep", entity.getTrackingId());
        assertEquals("Transportes Norte", entity.getCarrierName());
        assertEquals("Juan Pérez", entity.getRecipientName());
        assertEquals(AsyncDispatchGuideJpaEntity.ProcessingStatus.DELETED, entity.getStatus());
        assertEquals("s3/new.pdf", entity.getS3Key());
        assertEquals(LocalDateTime.ofInstant(Instant.parse("2026-06-03T12:00:00Z"), ZoneOffset.UTC), entity.getProcessedAt());
    }

    private static AsyncDispatchGuideJpaEntity baseEntity() {
        AsyncDispatchGuideJpaEntity entity = new AsyncDispatchGuideJpaEntity();
        entity.setId(10L);
        entity.setTrackingId("track-1");
        entity.setGuideId("guide-1");
        entity.setGuideNumber("GD-2026-000001");
        entity.setCarrierName("Transportes Rápidos");
        entity.setRecipientName("María González");
        entity.setOriginAddress("Origen");
        entity.setDestinationAddress("Destino");
        entity.setDescription("Electrónicos");
        entity.setDispatchDate(LocalDate.of(2026, 6, 2));
        entity.setOwnerEmail("responsable@empresa.cl");
        entity.setReceivedAt(LocalDateTime.of(2026, 6, 2, 9, 0));
        entity.setProcessedAt(LocalDateTime.of(2026, 6, 2, 10, 0));
        entity.setS3Bucket("bucket");
        entity.setS3Key("s3/key.pdf");
        entity.setEfsPath("/efs/path.pdf");
        return entity;
    }
}
