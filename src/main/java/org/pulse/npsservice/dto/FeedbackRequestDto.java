package org.pulse.npsservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record FeedbackRequestDto(
        @Min(value = 1, message = "Score must be at least 1")
        @Max(value = 10, message = "Score must be at most 10")
        int score,

        @NotBlank(message = "Comment is required and cannot be blank")
        String comment
) {
}
