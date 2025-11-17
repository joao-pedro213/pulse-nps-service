package org.pulse.npsservice.dto;

public record FeedbackRequestDto(
        int score,
        String comment
) {
}
