package com.dispatchflow.guides.e2e;

import com.dispatchflow.dispatch_flow_api.DispatchFlowApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DispatchFlowApiApplication.class)
@AutoConfigureMockMvc
class GuideControllerE2ETest {

    private static Path efsBasePath;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @DynamicPropertySource
    static void configureTestStorage(DynamicPropertyRegistry registry) throws Exception {
        efsBasePath = Files.createTempDirectory("efs-e2e");
        registry.add("efs.base-path", () -> efsBasePath.toString());
        registry.add("dispatch.storage.s3.enabled", () -> "false");
    }

    @Test
    void createsGuideWithPdfOnEfsAndAllowsDownload() throws Exception {
        String createJson = """
                {
                  "carrierName": "Transportes Rápidos",
                  "recipientName": "María González",
                  "originAddress": "Av. Providencia 1234, Santiago",
                  "destinationAddress": "Calle Huérfanos 567, Santiago",
                  "description": "Electrónicos",
                  "dispatchDate": "2026-06-02",
                  "ownerEmail": "responsable@empresa.cl"
                }
                """;

        String createResponse = mockMvc.perform(post("/api/guides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.guideNumber").value(startsWith("GD-")))
                .andExpect(jsonPath("$.status").value("UPLOADED_TO_S3"))
                .andExpect(jsonPath("$.efsPath").exists())
                .andExpect(jsonPath("$.s3Key").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = extractJsonField(createResponse, "id");
        String efsPath = extractJsonField(createResponse, "efsPath");

        assertTrue(Files.exists(Path.of(efsPath)));

        mockMvc.perform(get("/api/guides/" + id + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"guide-" + id + ".pdf\""))
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    void createsListsGetsUpdatesSearchesAndDeletesGuide() throws Exception {
        String createJson = """
                {
                  "carrierName": "Transportes Rápidos",
                  "recipientName": "María González",
                  "originAddress": "Av. Providencia 1234, Santiago",
                  "destinationAddress": "Calle Huérfanos 567, Santiago",
                  "description": "Electrónicos",
                  "dispatchDate": "2026-06-02",
                  "ownerEmail": "responsable@empresa.cl"
                }
                """;

        String createResponse = mockMvc.perform(post("/api/guides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("UPLOADED_TO_S3"))
                .andExpect(jsonPath("$.s3Key").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = extractJsonField(createResponse, "id");

        mockMvc.perform(get("/api/guides/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carrierName").value("Transportes Rápidos"));

        mockMvc.perform(get("/api/guides"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/guides/search")
                        .param("carrierName", "Transportes Rápidos")
                        .param("date", "2026-06-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        String updateJson = """
                {
                  "carrierName": "Transportes Norte",
                  "recipientName": "Juan Pérez",
                  "originAddress": "Av. Libertador 99, Santiago",
                  "destinationAddress": "Calle Estado 100, Santiago",
                  "description": "Despacho actualizado",
                  "dispatchDate": "2026-06-03",
                  "ownerEmail": "nuevo.responsable@empresa.cl"
                }
                """;

        mockMvc.perform(put("/api/guides/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carrierName").value("Transportes Norte"))
                .andExpect(jsonPath("$.status").value("UPLOADED_TO_S3"))
                .andExpect(jsonPath("$.efsPath").exists())
                .andExpect(jsonPath("$.s3Key").exists());

        mockMvc.perform(delete("/api/guides/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/guides/" + id))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/guides"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void returnsBadRequestWhenRequiredFieldsMissing() throws Exception {
        mockMvc.perform(post("/api/guides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void returnsNotFoundForUnknownGuide() throws Exception {
        mockMvc.perform(get("/api/guides/unknown-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    private String extractJsonField(String json, String field) {
        String pattern = "\"" + field + "\":\"";
        int start = json.indexOf(pattern);
        if (start < 0) {
            throw new IllegalArgumentException("Field not found: " + field);
        }
        start += pattern.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }
}