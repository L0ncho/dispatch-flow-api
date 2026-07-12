package com.dispatchflow.guides.application.dto;

public record GuideAcceptedResponse(
        String status,
        String message,
        String trackingId) {

    public static GuideAcceptedResponse accepted(String trackingId) {
        return new GuideAcceptedResponse(
                "ACCEPTED",
                "La guía fue enviada a procesamiento asíncrono",
                trackingId);
    }
}
