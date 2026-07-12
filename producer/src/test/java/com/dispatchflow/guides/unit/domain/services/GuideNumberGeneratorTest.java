package com.dispatchflow.guides.unit.domain.services;

import com.dispatchflow.guides.domain.services.GuideNumberGenerator;
import com.dispatchflow.guides.domain.valueobjects.GuideNumber;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuideNumberGeneratorTest {

    @Test
    void generatesGuideNumberWithYearAndSequence() {
        GuideNumberGenerator generator = new GuideNumberGenerator();

        GuideNumber guideNumber = generator.generate(42);

        int year = Year.now().getValue();
        assertEquals("GD-" + year + "-000042", guideNumber.value());
    }

    @Test
    void padsSequenceToSixDigits() {
        GuideNumberGenerator generator = new GuideNumberGenerator();

        GuideNumber guideNumber = generator.generate(1);

        assertTrue(guideNumber.value().endsWith("-000001"));
    }
}
