package com.dispatchflow.guides.application;

import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.repositories.GuideRepository;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import com.dispatchflow.shared.domain.DomainError;

public class GetGuideUseCase {

    private final GuideRepository guideRepository;

    public GetGuideUseCase(GuideRepository guideRepository) {
        this.guideRepository = guideRepository;
    }

    public GuideResponse execute(String id) {
        DispatchGuide guide = guideRepository.findById(GuideId.create(id))
                .orElseThrow(() -> DomainError.notFound("Guide " + id + " not found"));

        if (guide.isDeleted()) {
            throw DomainError.notFound("Guide " + id + " not found");
        }

        return GuideResponse.from(guide);
    }
}
