package com.dispatchflow.guides.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataAsyncDispatchGuideRepository extends JpaRepository<AsyncDispatchGuideJpaEntity, Long> {

    Optional<AsyncDispatchGuideJpaEntity> findByGuideId(String guideId);

    boolean existsByGuideId(String guideId);

    List<AsyncDispatchGuideJpaEntity> findByStatus(AsyncDispatchGuideJpaEntity.ProcessingStatus status);

    List<AsyncDispatchGuideJpaEntity> findByCarrierNameAndDispatchDateAndStatus(
            String carrierName,
            LocalDate dispatchDate,
            AsyncDispatchGuideJpaEntity.ProcessingStatus status);
}
