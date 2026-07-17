package com.dispatchflow.consumer.application.dto;

public record ProcessQueuedGuideResponse(String status, String trackingId, String guideId, String message) {

    public static ProcessQueuedGuideResponse processed(String trackingId, String guideId) {
        return new ProcessQueuedGuideResponse(
                "PROCESSED",
                trackingId,
                guideId,
                "Guide message processed from queue");
    }
}
