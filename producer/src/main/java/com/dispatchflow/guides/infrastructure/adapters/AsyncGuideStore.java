package com.dispatchflow.guides.infrastructure.adapters;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.valueobjects.GuideId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsyncGuideStore {

    Optional<DispatchGuide> findByGuideId(GuideId id);

    boolean existsByGuideId(GuideId id);

    List<DispatchGuide> findAllActive();

    List<DispatchGuide> findActiveByCarrierAndDispatchDate(String carrierName, LocalDate dispatchDate);

    void save(DispatchGuide guide);
}
