package org.pulse.npsservice.dto;

import org.pulse.npsservice.domain.FeedbackType;

public record FeedbackTypeCountDto(
        FeedbackType type,
        long count,
        String urgency
) {
    public FeedbackTypeCountDto(FeedbackType type, long count) {
        this(type, count, mapTypeToUrgency(type));
    }

    private static String mapTypeToUrgency(FeedbackType type) {
        return switch (type) {
            case DETRACTOR -> "HIGH";
            case NEUTRAL -> "MEDIUM";
            case PROMOTER -> "LOW";
        };
    }
}
