package com.dispatchflow.guides.domain.services;

import com.dispatchflow.guides.domain.valueobjects.GuideNumber;

import java.time.Year;

public class GuideNumberGenerator {

    public GuideNumber generate(long sequence) {
        int year = Year.now().getValue();
        String formatted = String.format("GD-%d-%06d", year, sequence);
        return GuideNumber.create(formatted);
    }
}
