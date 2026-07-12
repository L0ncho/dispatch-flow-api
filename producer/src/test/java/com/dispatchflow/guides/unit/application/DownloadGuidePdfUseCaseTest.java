package com.dispatchflow.guides.unit.application;

import com.dispatchflow.guides.application.DownloadGuidePdfUseCase;
import com.dispatchflow.guides.application.dto.GuidePdfDownload;
import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.domain.repositories.InMemoryGuideRepository;
import com.dispatchflow.guides.unit.application.support.GuideApplicationTestSupport;
import com.dispatchflow.shared.domain.DomainError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DownloadGuidePdfUseCaseTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-06-02T10:00:00Z"), ZoneOffset.UTC);
    private static final byte[] PDF_BYTES = GuideApplicationTestSupport.PDF_BYTES;

    private InMemoryGuideRepository repository;
    private GuideApplicationTestSupport.InMemoryObjectStorage objectStorage;
    private DownloadGuidePdfUseCase downloadGuidePdfUseCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryGuideRepository();
        objectStorage = GuideApplicationTestSupport.inMemoryObjectStorage();
        downloadGuidePdfUseCase = GuideApplicationTestSupport.downloadGuidePdfUseCase(repository, objectStorage);
    }

    @Test
    void downloadsPdfFromS3WhenS3KeyIsPresent() {
        GuideResponse created = GuideApplicationTestSupport.seedGuide(repository, FIXED_CLOCK, "Transportes Rápidos", objectStorage);

        GuidePdfDownload download = downloadGuidePdfUseCase.execute(created.id());

        assertArrayEquals(PDF_BYTES, download.content());
        assertEquals("guide-" + created.id() + ".pdf", download.fileName());
    }

    @Test
    void throwsNotFoundWhenGuideDoesNotExist() {
        assertThrows(DomainError.class, () -> downloadGuidePdfUseCase.execute("missing-id"));
    }

    @Test
    void throwsNotFoundWhenGuideIsDeleted() {
        GuideResponse created = GuideApplicationTestSupport.seedGuide(repository, FIXED_CLOCK);
        GuideApplicationTestSupport.deleteGuideUseCase(repository, FIXED_CLOCK, objectStorage)
                .execute(created.id());

        assertThrows(DomainError.class, () -> downloadGuidePdfUseCase.execute(created.id()));
    }
}
