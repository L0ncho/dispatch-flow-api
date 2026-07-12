package com.dispatchflow.consumer.application.dto;

public record ProcessQueuedGuideResponse(String status, String trackingId, String message) {

    public static ProcessQueuedGuideResponse processed(String trackingId) {
        return new ProcessQueuedGuideResponse(
                "PROCESSED",
                trackingId,
                "Guide message processed from queue");
    }
}
