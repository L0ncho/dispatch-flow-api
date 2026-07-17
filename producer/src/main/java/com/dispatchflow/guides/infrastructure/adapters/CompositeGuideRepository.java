package com.dispatchflow.guides.infrastructure.adapters;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.repositories.GuideRepository;
import com.dispatchflow.guides.domain.valueobjects.GuideId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CompositeGuideRepository implements GuideRepository {

    private final GuideRepository legacyRepository;
    private final AsyncGuideStore asyncGuideStore;

    public CompositeGuideRepository(GuideRepository legacyRepository, AsyncGuideStore asyncGuideStore) {
        this.legacyRepository = legacyRepository;
        this.asyncGuideStore = asyncGuideStore;
    }

    @Override
    public void save(DispatchGuide guide) {
        GuideId id = guide.getId();
        if (legacyRepository.findById(id).isPresent()) {
            legacyRepository.save(guide);
            return;
        }
        if (asyncGuideStore.existsByGuideId(id)) {
            asyncGuideStore.save(guide);
            return;
        }
        legacyRepository.save(guide);
    }

    @Override
    public Optional<DispatchGuide> findById(GuideId id) {
        Optional<DispatchGuide> legacy = legacyRepository.findById(id);
        if (legacy.isPresent()) {
            return legacy;
        }
        return asyncGuideStore.findByGuideId(id);
    }

    @Override
    public List<DispatchGuide> findAllActive() {
        return mergePreferringLegacy(asyncGuideStore.findAllActive(), legacyRepository.findAllActive());
    }

    @Override
    public List<DispatchGuide> findByCarrierAndDispatchDate(String carrierName, LocalDate dispatchDate) {
        return mergePreferringLegacy(
                asyncGuideStore.findActiveByCarrierAndDispatchDate(carrierName, dispatchDate),
                legacyRepository.findByCarrierAndDispatchDate(carrierName, dispatchDate));
    }

    @Override
    public long nextSequence() {
        return legacyRepository.nextSequence();
    }

    private static List<DispatchGuide> mergePreferringLegacy(
            List<DispatchGuide> asyncGuides,
            List<DispatchGuide> legacyGuides) {
        Map<String, DispatchGuide> merged = new LinkedHashMap<>();
        for (DispatchGuide guide : asyncGuides) {
            merged.put(guide.getId().value(), guide);
        }
        for (DispatchGuide guide : legacyGuides) {
            merged.put(guide.getId().value(), guide);
        }
        return new ArrayList<>(merged.values());
    }
}
