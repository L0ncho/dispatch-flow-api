package com.dispatchflow.consumer.infrastructure.http;

import com.dispatchflow.consumer.application.ProcessNextQueuedGuideUseCase;
import com.dispatchflow.consumer.application.dto.ProcessQueuedGuideResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/guides")
public class ProcessQueuedGuideController {

    private final ProcessNextQueuedGuideUseCase processNextQueuedGuideUseCase;

    public ProcessQueuedGuideController(ProcessNextQueuedGuideUseCase processNextQueuedGuideUseCase) {
        this.processNextQueuedGuideUseCase = processNextQueuedGuideUseCase;
    }

    @PostMapping("/process-next")
    public ResponseEntity<ProcessQueuedGuideResponse> processNext() {
        Optional<String> trackingId = processNextQueuedGuideUseCase.execute();
        if (trackingId.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ProcessQueuedGuideResponse.processed(trackingId.get()));
    }
}
