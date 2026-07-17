package com.dispatchflow.guides.e2e;

import com.dispatchflow.dispatch_flow_api.DispatchFlowApiApplication;
import com.dispatchflow.guides.application.ports.GuideMessagePublisher;
import com.dispatchflow.guides.infrastructure.persistence.AsyncDispatchGuideJpaEntity;
import com.dispatchflow.guides.infrastructure.persistence.SpringDataAsyncDispatchGuideRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DispatchFlowApiApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class AsyncGuideDeleteE2ETest {

    private static Path efsBasePath;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataAsyncDispatchGuideRepository asyncRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private GuideMessagePublisher guideMessagePublisher;

    @DynamicPropertySource
    static void configureTestStorage(DynamicPropertyRegistry registry) throws Exception {
        efsBasePath = Files.createTempDirectory("efs-async-delete-e2e");
        registry.add("efs.base-path", () -> efsBasePath.toString());
        registry.add("dispatch.storage.s3.enabled", () -> "false");
    }

    @Test
    void deletesAsyncOnlyGuideAndMarksStatusDeleted() throws Exception {
        String guideId = "445f3c8f-6e9f-46b5-aa94-3e6891e95b83";
        AsyncDispatchGuideJpaEntity entity = new AsyncDispatchGuideJpaEntity();
        entity.setTrackingId("track-async-delete-1");
        entity.setGuideId(guideId);
        entity.setGuideNumber("GD-2026-009901");
        entity.setCarrierName("Transportes Rápidos");
        entity.setRecipientName("María González");
        entity.setOriginAddress("Origen");
        entity.setDestinationAddress("Destino");
        entity.setDescription("Electrónicos");
        entity.setDispatchDate(LocalDate.of(2026, 6, 2));
        entity.setOwnerEmail("responsable@empresa.cl");
        entity.setReceivedAt(LocalDateTime.of(2026, 6, 2, 9, 0));
        entity.setProcessedAt(LocalDateTime.of(2026, 6, 2, 10, 0));
        entity.setStatus(AsyncDispatchGuideJpaEntity.ProcessingStatus.PROCESSED);
        entity.setS3Bucket("dispatch-flow-local");
        entity.setS3Key("guides/async/" + guideId + ".pdf");
        entity.setEfsPath(efsBasePath.resolve("guide-" + guideId + ".pdf").toString());
        asyncRepository.save(entity);

        mockMvc.perform(delete("/api/guides/" + guideId))
                .andExpect(status().isNoContent());

        AsyncDispatchGuideJpaEntity stored = asyncRepository.findByGuideId(guideId).orElseThrow();
        assertEquals(AsyncDispatchGuideJpaEntity.ProcessingStatus.DELETED, stored.getStatus());
    }
}
