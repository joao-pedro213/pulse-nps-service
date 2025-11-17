package org.pulse.npsservice.dto;

public record FeedbackResponseDto(
        int score,
        String comment,
        String createdAt
) {
}
