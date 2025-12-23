package org.pulse.npsservice.dto;

import org.pulse.npsservice.domain.FeedbackType;

public record FeedbackDto(
        int score,
        String comment,
        FeedbackType type
) {
}
