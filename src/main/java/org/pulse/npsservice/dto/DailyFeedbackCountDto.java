package org.pulse.npsservice.dto;

import java.time.LocalDate;

public record DailyFeedbackCountDto(
        LocalDate date,
        long count
) {
}
