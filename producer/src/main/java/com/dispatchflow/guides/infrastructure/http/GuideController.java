package com.dispatchflow.guides.infrastructure.http;

import com.dispatchflow.guides.application.AcceptGuideRequestUseCase;
import com.dispatchflow.guides.application.DeleteGuideUseCase;
import com.dispatchflow.guides.application.DownloadGuidePdfUseCase;
import com.dispatchflow.guides.application.GetGuideUseCase;
import com.dispatchflow.guides.application.ListGuidesUseCase;
import com.dispatchflow.guides.application.SearchGuidesUseCase;
import com.dispatchflow.guides.application.UpdateGuideUseCase;
import com.dispatchflow.guides.application.dto.CreateGuideCommand;
import com.dispatchflow.guides.application.dto.GuideAcceptedResponse;
import com.dispatchflow.guides.application.dto.GuidePdfDownload;
import com.dispatchflow.guides.application.dto.GuideResponse;
import com.dispatchflow.guides.application.dto.UpdateGuideCommand;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/guides")
public class GuideController {

    private final AcceptGuideRequestUseCase acceptGuideRequestUseCase;
    private final GetGuideUseCase getGuideUseCase;
    private final ListGuidesUseCase listGuidesUseCase;
    private final UpdateGuideUseCase updateGuideUseCase;
    private final DeleteGuideUseCase deleteGuideUseCase;
    private final SearchGuidesUseCase searchGuidesUseCase;
    private final DownloadGuidePdfUseCase downloadGuidePdfUseCase;

    public GuideController(
            AcceptGuideRequestUseCase acceptGuideRequestUseCase,
            GetGuideUseCase getGuideUseCase,
            ListGuidesUseCase listGuidesUseCase,
            UpdateGuideUseCase updateGuideUseCase,
            DeleteGuideUseCase deleteGuideUseCase,
            SearchGuidesUseCase searchGuidesUseCase,
            DownloadGuidePdfUseCase downloadGuidePdfUseCase) {
        this.acceptGuideRequestUseCase = acceptGuideRequestUseCase;
        this.getGuideUseCase = getGuideUseCase;
        this.listGuidesUseCase = listGuidesUseCase;
        this.updateGuideUseCase = updateGuideUseCase;
        this.deleteGuideUseCase = deleteGuideUseCase;
        this.searchGuidesUseCase = searchGuidesUseCase;
        this.downloadGuidePdfUseCase = downloadGuidePdfUseCase;
    }

    @PostMapping
    public ResponseEntity<GuideAcceptedResponse> create(@Valid @RequestBody CreateGuideRequest request) {
        GuideAcceptedResponse response = acceptGuideRequestUseCase.execute(toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}")
    public GuideResponse getById(@PathVariable String id) {
        return getGuideUseCase.execute(id);
    }

    @GetMapping
    public List<GuideResponse> list() {
        return listGuidesUseCase.execute();
    }

    @PutMapping("/{id}")
    public GuideResponse update(@PathVariable String id, @Valid @RequestBody UpdateGuideRequest request) {
        return updateGuideUseCase.execute(id, toUpdateCommand(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteGuideUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<GuideResponse> search(
            @RequestParam String carrierName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return searchGuidesUseCase.execute(carrierName, date);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String id) {
        GuidePdfDownload download = downloadGuidePdfUseCase.execute(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.fileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(download.content());
    }

    private CreateGuideCommand toCreateCommand(CreateGuideRequest request) {
        return new CreateGuideCommand(
                request.carrierName(),
                request.recipientName(),
                request.originAddress(),
                request.destinationAddress(),
                request.description(),
                request.dispatchDate(),
                request.ownerEmail());
    }

    private UpdateGuideCommand toUpdateCommand(UpdateGuideRequest request) {
        return new UpdateGuideCommand(
                request.carrierName(),
                request.recipientName(),
                request.originAddress(),
                request.destinationAddress(),
                request.description(),
                request.dispatchDate(),
                request.ownerEmail());
    }
}
