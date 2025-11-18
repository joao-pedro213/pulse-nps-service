package org.pulse.npsservice.dto;

public record FeedbackResponseDto(
        String id,
        int score,
        String comment,
        String createdAt
) {
}
