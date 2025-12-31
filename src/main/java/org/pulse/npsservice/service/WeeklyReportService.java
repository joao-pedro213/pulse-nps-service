package org.pulse.npsservice.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.pulse.npsservice.domain.FeedbackType;
import org.pulse.npsservice.dto.DailyFeedbackCountDto;
import org.pulse.npsservice.dto.FeedbackTypeCountDto;
import org.pulse.npsservice.dto.WeeklyReportDto;
import org.pulse.npsservice.model.FeedbackModel;
import org.pulse.npsservice.repository.FeedbackRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@ApplicationScoped
public class WeeklyReportService {
    private FeedbackRepository feedbackRepository;

    @Inject
    public WeeklyReportService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public Uni<WeeklyReportDto> generateLastWeekReport() {
        LocalDate today = LocalDate.now();
        LocalDate startOfLastWeek = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfLastWeek = today.minusWeeks(1).with(java.time.DayOfWeek.SUNDAY);
        return this.generateWeeklyReport(startOfLastWeek, endOfLastWeek);
    }

    private Uni<WeeklyReportDto> generateWeeklyReport(LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        return this.feedbackRepository
                .findByDateRange(startInstant, endInstant)
                .map(feedbacks -> this.buildWeeklyReport(feedbacks, startDate, endDate));
    }

    private WeeklyReportDto buildWeeklyReport(List<FeedbackModel> feedbacks, LocalDate startDate, LocalDate endDate) {
        long totalFeedbacks = feedbacks.size();
        double averageScore = this.calculateAverageScore(feedbacks);
        List<DailyFeedbackCountDto> feedbackCountByDay = this.aggregateFeedbacksByDay(feedbacks, startDate, endDate);
        List<FeedbackTypeCountDto> feedbackCountByType = this.aggregateFeedbacksByType(feedbacks);
        String description = String.format("Weekly Feedback Report - %s a %s", startDate, endDate);
        return new WeeklyReportDto(
                description,
                startDate,
                endDate,
                LocalDate.now(),
                totalFeedbacks,
                averageScore,
                feedbackCountByDay,
                feedbackCountByType);
    }

    private List<DailyFeedbackCountDto> aggregateFeedbacksByDay(
            List<FeedbackModel> feedbacks,
            LocalDate startDate,
            LocalDate endDate) {
        Map<LocalDate, Long> feedbacksByDate = feedbacks
                .stream()
                .collect(
                        Collectors.groupingBy(
                                feedback -> feedback.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                                Collectors.counting()));
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return LongStream
                .range(0, daysBetween)
                .mapToObj(i -> {
                    LocalDate date = startDate.plusDays(i);
                    long count = feedbacksByDate.getOrDefault(date, 0L);
                    return new DailyFeedbackCountDto(date, count);
                })
                .collect(Collectors.toList());
    }

    private List<FeedbackTypeCountDto> aggregateFeedbacksByType(List<FeedbackModel> feedbacks) {
        Map<FeedbackType, Long> feedbacksByType = feedbacks
                .stream()
                .collect(Collectors.groupingBy(FeedbackModel::getType, Collectors.counting()));
        return List.of(
                new FeedbackTypeCountDto(FeedbackType.DETRACTOR, feedbacksByType.getOrDefault(FeedbackType.DETRACTOR, 0L)),
                new FeedbackTypeCountDto(FeedbackType.NEUTRAL, feedbacksByType.getOrDefault(FeedbackType.NEUTRAL, 0L)),
                new FeedbackTypeCountDto(FeedbackType.PROMOTER, feedbacksByType.getOrDefault(FeedbackType.PROMOTER, 0L)));
    }

    private double calculateAverageScore(List<FeedbackModel> feedbacks) {
        if (feedbacks.isEmpty()) {
            return 0.0;
        }
        return feedbacks.stream().mapToInt(FeedbackModel::getScore).average().orElse(0.0);
    }
}
