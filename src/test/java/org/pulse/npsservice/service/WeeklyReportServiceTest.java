package org.pulse.npsservice.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.pulse.npsservice.domain.FeedbackType;
import org.pulse.npsservice.dto.WeeklyReportDto;
import org.pulse.npsservice.model.FeedbackModel;
import org.pulse.npsservice.repository.FeedbackRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class WeeklyReportServiceTest {

    @Inject
    WeeklyReportService weeklyReportService;

    @InjectMock
    FeedbackRepository feedbackRepository;

    @Test
    void testGenerateLastWeekReportWithMultipleFeedbacks() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate startOfLastWeek = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfLastWeek = today.minusWeeks(1).with(java.time.DayOfWeek.SUNDAY);
        Instant day1 = startOfLastWeek.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant day2 = startOfLastWeek.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant day3 = startOfLastWeek.plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant();
        List<FeedbackModel> mockFeedbacks = List.of(
                new FeedbackModel(5, "Bad", FeedbackType.DETRACTOR, day1),
                new FeedbackModel(6, "Poor", FeedbackType.DETRACTOR, day1),
                new FeedbackModel(7, "Okay", FeedbackType.NEUTRAL, day2),
                new FeedbackModel(8, "Good", FeedbackType.NEUTRAL, day2),
                new FeedbackModel(9, "Great", FeedbackType.PROMOTER, day3),
                new FeedbackModel(10, "Excellent", FeedbackType.PROMOTER, day3)
        );
        when(this.feedbackRepository.findByDateRange(any(Instant.class), any(Instant.class)))
                .thenReturn(Uni.createFrom().item(mockFeedbacks));

        // When
        WeeklyReportDto report = this.weeklyReportService.generateLastWeekReport().await().indefinitely();

        // Then
        assertNotNull(report);
        assertEquals(startOfLastWeek, report.startDate());
        assertEquals(endOfLastWeek, report.endDate());
        assertEquals(6, report.totalFeedbacks());
        assertEquals(7.5, report.averageScore(), 0.01);
        assertEquals(7, report.feedbackCountByDay().size());
        assertEquals(2, report.feedbackCountByDay().get(0).count());
        assertEquals(2, report.feedbackCountByDay().get(1).count());
        assertEquals(2, report.feedbackCountByDay().get(2).count());
        assertEquals(0, report.feedbackCountByDay().get(3).count());
        assertEquals(0, report.feedbackCountByDay().get(4).count());
        assertEquals(0, report.feedbackCountByDay().get(5).count());
        assertEquals(0, report.feedbackCountByDay().get(6).count());
        assertEquals(3, report.feedbackCountByType().size());
        assertEquals(2, report.feedbackCountByType().get(0).count());
        assertEquals("HIGH", report.feedbackCountByType().get(0).urgency());
        assertEquals(2, report.feedbackCountByType().get(1).count());
        assertEquals("MEDIUM", report.feedbackCountByType().get(1).urgency());
        assertEquals(2, report.feedbackCountByType().get(2).count());
        assertEquals("LOW", report.feedbackCountByType().get(2).urgency());
    }

    @Test
    void testGenerateLastWeekReportWithNoFeedbacks() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate startOfLastWeek = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfLastWeek = today.minusWeeks(1).with(java.time.DayOfWeek.SUNDAY);
        when(this.feedbackRepository.findByDateRange(any(Instant.class), any(Instant.class)))
                .thenReturn(Uni.createFrom().item(List.of()));

        // When
        WeeklyReportDto report = this.weeklyReportService.generateLastWeekReport().await().indefinitely();

        // Then
        assertNotNull(report);
        assertEquals(startOfLastWeek, report.startDate());
        assertEquals(endOfLastWeek, report.endDate());
        assertEquals(0, report.totalFeedbacks());
        assertEquals(0.0, report.averageScore(), 0.01);
        assertEquals(7, report.feedbackCountByDay().size());
        report.feedbackCountByDay().forEach(day -> assertEquals(0, day.count()));
        assertEquals(3, report.feedbackCountByType().size());
        report.feedbackCountByType().forEach(type -> assertEquals(0, type.count()));
    }

    @Test
    void testGenerateLastWeekReportDateRangeCalculation() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate startOfLastWeek = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfLastWeek = today.minusWeeks(1).with(java.time.DayOfWeek.SUNDAY);
        when(this.feedbackRepository.findByDateRange(any(Instant.class), any(Instant.class)))
                .thenReturn(Uni.createFrom().item(List.of()));

        // When
        WeeklyReportDto report = this.weeklyReportService.generateLastWeekReport().await().indefinitely();

        // Then
        assertNotNull(report);
        assertEquals(startOfLastWeek, report.startDate());
        assertEquals(endOfLastWeek, report.endDate());
        assertEquals(LocalDate.now(), report.reportGeneratedAt());
    }
}
