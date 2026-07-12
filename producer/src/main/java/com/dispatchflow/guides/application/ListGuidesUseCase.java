package com.dispatchflow.guides.application;

import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.domain.repositories.GuideRepository;

import java.util.List;

public class ListGuidesUseCase {

    private final GuideRepository guideRepository;

    public ListGuidesUseCase(GuideRepository guideRepository) {
        this.guideRepository = guideRepository;
    }

    public List<GuideResponse> execute() {
        return guideRepository.findAllActive().stream()
                .map(GuideResponse::from)
                .toList();
    }
}
