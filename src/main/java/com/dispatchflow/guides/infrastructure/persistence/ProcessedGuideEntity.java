package com.dispatchflow.guides.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_guides")
public class ProcessedGuideEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String guideId;
    private String guideNumber;
    private LocalDateTime processedAt;

    public ProcessedGuideEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGuideId() { return guideId; }
    public void setGuideId(String guideId) { this.guideId = guideId; }

    public String getGuideNumber() { return guideNumber; }
    public void setGuideNumber(String guideNumber) { this.guideNumber = guideNumber; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}