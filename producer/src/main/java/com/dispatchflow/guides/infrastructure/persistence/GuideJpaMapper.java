package com.dispatchflow.guides.infrastructure.persistence;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.guides.domain.valueobjects.GuideNumber;
import com.dispatchflow.guides.domain.valueobjects.GuideStatus;

public final class GuideJpaMapper {

    private GuideJpaMapper() {
    }

    public static GuideJpaEntity toEntity(DispatchGuide guide) {
        GuideJpaEntity entity = new GuideJpaEntity();
        entity.setId(guide.getId().value());
        entity.setGuideNumber(guide.getGuideNumber().value());
        entity.setCarrierName(guide.getCarrierName());
        entity.setRecipientName(guide.getRecipientName());
        entity.setOriginAddress(guide.getOriginAddress());
        entity.setDestinationAddress(guide.getDestinationAddress());
        entity.setDescription(guide.getDescription());
        entity.setDispatchDate(guide.getDispatchDate());
        entity.setCreatedAt(guide.getCreatedAt());
        entity.setUpdatedAt(guide.getUpdatedAt());
        entity.setOwnerEmail(guide.getOwnerEmail().value());
        entity.setStatus(GuideStatusJpa.valueOf(guide.getStatus().name()));
        entity.setEfsPath(guide.getEfsPath());
        entity.setS3Key(guide.getS3Key());
        return entity;
    }

    public static DispatchGuide toDomain(GuideJpaEntity entity) {
        return DispatchGuide.restore(
                GuideId.create(entity.getId()),
                GuideNumber.create(entity.getGuideNumber()),
                entity.getCarrierName(),
                entity.getRecipientName(),
                entity.getOriginAddress(),
                entity.getDestinationAddress(),
                entity.getDescription(),
                entity.getDispatchDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                Email.create(entity.getOwnerEmail()),
                GuideStatus.valueOf(entity.getStatus().name()),
                entity.getEfsPath(),
                entity.getS3Key());
    }
}
