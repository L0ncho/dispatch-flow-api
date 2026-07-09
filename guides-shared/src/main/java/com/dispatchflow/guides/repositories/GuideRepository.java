package com.dispatchflow.guides.domain.repositories;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.valueobjects.GuideId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GuideRepository {

    void save(DispatchGuide guide);

    Optional<DispatchGuide> findById(GuideId id);

    List<DispatchGuide> findAllActive();

    List<DispatchGuide> findByCarrierAndDispatchDate(String carrierName, LocalDate dispatchDate);

    long nextSequence();
}
