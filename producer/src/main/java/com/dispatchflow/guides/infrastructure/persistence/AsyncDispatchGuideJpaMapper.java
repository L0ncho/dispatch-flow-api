package com.dispatchflow.guides.infrastructure.persistence;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.domain.valueobjects.GuideNumber;
import com.dispatchflow.guides.domain.valueobjects.GuideStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class AsyncDispatchGuideJpaMapper {

    private AsyncDispatchGuideJpaMapper() {
    }

    public static DispatchGuide toDomain(AsyncDispatchGuideJpaEntity entity) {
        if (entity.getStatus() == AsyncDispatchGuideJpaEntity.ProcessingStatus.FAILED) {
            return null;
        }

        GuideStatus status = entity.getStatus() == AsyncDispatchGuideJpaEntity.ProcessingStatus.DELETED
                ? GuideStatus.DELETED
                : GuideStatus.UPLOADED_TO_S3;

        LocalDateTime processedAt = entity.getProcessedAt() != null
                ? entity.getProcessedAt()
                : entity.getReceivedAt();

        return DispatchGuide.restore(
                GuideId.create(entity.getGuideId()),
                GuideNumber.create(entity.getGuideNumber()),
                entity.getCarrierName(),
                entity.getRecipientName(),
                entity.getOriginAddress(),
                entity.getDestinationAddress(),
                entity.getDescription(),
                entity.getDispatchDate(),
                entity.getReceivedAt().toInstant(ZoneOffset.UTC),
                processedAt.toInstant(ZoneOffset.UTC),
                Email.create(entity.getOwnerEmail()),
                status,
                entity.getEfsPath(),
                entity.getS3Key());
    }

    public static void applyDomainToEntity(DispatchGuide guide, AsyncDispatchGuideJpaEntity entity) {
        entity.setGuideId(guide.getId().value());
        entity.setGuideNumber(guide.getGuideNumber().value());
        entity.setCarrierName(guide.getCarrierName());
        entity.setRecipientName(guide.getRecipientName());
        entity.setOriginAddress(guide.getOriginAddress());
        entity.setDestinationAddress(guide.getDestinationAddress());
        entity.setDescription(guide.getDescription());
        entity.setDispatchDate(guide.getDispatchDate());
        entity.setOwnerEmail(guide.getOwnerEmail().value());
        entity.setProcessedAt(LocalDateTime.ofInstant(guide.getUpdatedAt(), ZoneOffset.UTC));
        entity.setEfsPath(guide.getEfsPath());
        entity.setS3Key(guide.getS3Key());
        entity.setStatus(toProcessingStatus(guide.getStatus()));
    }

    private static AsyncDispatchGuideJpaEntity.ProcessingStatus toProcessingStatus(GuideStatus status) {
        if (status == GuideStatus.DELETED) {
            return AsyncDispatchGuideJpaEntity.ProcessingStatus.DELETED;
        }
        return AsyncDispatchGuideJpaEntity.ProcessingStatus.PROCESSED;
    }
}
