package com.dispatchflow.guides.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface SpringDataGuideRepository extends JpaRepository<GuideJpaEntity, String> {

    List<GuideJpaEntity> findByStatusNot(GuideStatusJpa status);

    List<GuideJpaEntity> findByCarrierNameAndDispatchDateAndStatusNot(
            String carrierName, LocalDate dispatchDate, GuideStatusJpa status);
}
