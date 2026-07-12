package com.dispatchflow.guides.application;

import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.domain.repositories.GuideRepository;
import com.dispatchflow.shared.domain.DomainError;

import java.time.LocalDate;
import java.util.List;

public class SearchGuidesUseCase {

    private final GuideRepository guideRepository;

    public SearchGuidesUseCase(GuideRepository guideRepository) {
        this.guideRepository = guideRepository;
    }

    public List<GuideResponse> execute(String carrierName, LocalDate dispatchDate) {
        if (carrierName == null || carrierName.isBlank()) {
            throw DomainError.validation("Carrier name is required for search");
        }
        if (dispatchDate == null) {
            throw DomainError.validation("Dispatch date is required for search");
        }

        return guideRepository.findByCarrierAndDispatchDate(carrierName.trim(), dispatchDate).stream()
                .map(GuideResponse::from)
                .toList();
    }
}
