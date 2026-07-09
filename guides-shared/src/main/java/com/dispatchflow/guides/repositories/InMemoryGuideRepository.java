package com.dispatchflow.guides.domain.repositories;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.valueobjects.GuideId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryGuideRepository implements GuideRepository {

    private final Map<String, DispatchGuide> guides = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public void save(DispatchGuide guide) {
        guides.put(guide.getId().value(), guide);
    }

    @Override
    public Optional<DispatchGuide> findById(GuideId id) {
        return Optional.ofNullable(guides.get(id.value()));
    }

    @Override
    public List<DispatchGuide> findAllActive() {
        return guides.values().stream()
                .filter(guide -> !guide.isDeleted())
                .toList();
    }

    @Override
    public List<DispatchGuide> findByCarrierAndDispatchDate(String carrierName, LocalDate dispatchDate) {
        List<DispatchGuide> matches = new ArrayList<>();
        for (DispatchGuide guide : guides.values()) {
            if (guide.isDeleted()) {
                continue;
            }
            if (guide.getCarrierName().equals(carrierName) && guide.getDispatchDate().equals(dispatchDate)) {
                matches.add(guide);
            }
        }
        return matches;
    }

    @Override
    public long nextSequence() {
        return sequence.incrementAndGet();
    }
}
