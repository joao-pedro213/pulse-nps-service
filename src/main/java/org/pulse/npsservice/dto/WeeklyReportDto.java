package org.pulse.npsservice.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyReportDto(
        String description,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate reportGeneratedAt,
        long totalFeedbacks,
        List<DailyFeedbackCountDto> feedbackCountByDay,
        List<FeedbackTypeCountDto> feedbackCountByType
) {
}
