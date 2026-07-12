package com.dispatchflow.guides.e2e;

import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.guides.domain.repositories.GuideRepository;
import com.dispatchflow.guides.domain.services.GuideNumberGenerator;
import com.dispatchflow.guides.domain.valueobjects.Email;
import com.dispatchflow.guides.domain.valueobjects.GuideId;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

@Component
class GuideE2eDataSeeder {

    private final GuideRepository guideRepository;
    private final GuideNumberGenerator guideNumberGenerator;
    private final Clock clock;

    GuideE2eDataSeeder(GuideRepository guideRepository, GuideNumberGenerator guideNumberGenerator, Clock clock) {
        this.guideRepository = guideRepository;
        this.guideNumberGenerator = guideNumberGenerator;
        this.clock = clock;
    }

    SeededGuide seedSampleGuide() {
        long sequence = guideRepository.nextSequence();
        GuideId guideId = GuideId.generate();
        DispatchGuide guide = DispatchGuide.create(
                guideId,
                guideNumberGenerator.generate(sequence),
                "Transportes Rápidos",
                "María González",
                "Av. Providencia 1234, Santiago",
                "Calle Huérfanos 567, Santiago",
                "Electrónicos",
                LocalDate.of(2026, 6, 2),
                Email.create("responsable@empresa.cl"),
                Instant.now(clock));
        guide.markPdfGenerated("/efs/guides/2026-06-02/transportes-rapidos/guide-" + guideId.value() + ".pdf", Instant.now(clock));
        guide.markUploadedToS3("guides/2026-06-02/transportes-rapidos/guide-" + guideId.value() + ".pdf", Instant.now(clock));
        guideRepository.save(guide);
        return new SeededGuide(guideId.value(), guide.getEfsPath());
    }

    record SeededGuide(String id, String efsPath) {
    }
}
