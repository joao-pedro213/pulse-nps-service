package org.pulse.npsservice.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.pulse.npsservice.domain.FeedbackType;
import org.pulse.npsservice.dto.FeedbackDto;
import org.pulse.npsservice.dto.FeedbackRequestDto;
import org.pulse.npsservice.dto.FeedbackResponseDto;
import org.pulse.npsservice.model.FeedbackModel;
import org.pulse.npsservice.repository.FeedbackRepository;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class FeedbackServiceTest {
    @Inject
    FeedbackService feedbackService;

    @InjectMock
    FeedbackRepository feedbackRepository;

    @InjectMock
    DetractorProducer detractorProducer;

    @Test
    void testCreate() {
        // Given
        int validScore = 8;
        String validComment = "Great service!";
        String expectedFeedbackId = "507f1f77bcf86cd799439011";
        String expectedCreatedAt = "2025-11-17T22:49:20.389Z";
        FeedbackRequestDto requestDto = new FeedbackRequestDto(validScore, validComment);
        FeedbackModel mockModel = new FeedbackModel(validScore, validComment, FeedbackType.NEUTRAL, Instant.parse(expectedCreatedAt));
        mockModel.setId(new ObjectId(expectedFeedbackId));
        when(this.feedbackRepository.persist(any(FeedbackModel.class))).thenReturn(Uni.createFrom().item(mockModel));

        // When
        FeedbackResponseDto result = this.feedbackService
                .create(requestDto)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(expectedFeedbackId, result.id());
        assertEquals(validScore, result.score());
        assertEquals(validComment, result.comment());
        assertEquals(expectedCreatedAt, result.createdAt());
        assertEquals(FeedbackType.NEUTRAL, result.type());
        verify(this.feedbackRepository).persist(any(FeedbackModel.class));
        verify(this.detractorProducer, never()).sendMessage(any(FeedbackDto.class));
    }

    @Test
    void testCreateWithDetractor() {
        // Given
        int detractorScore = 5;
        String validComment = "Poor service";
        String expectedFeedbackId = "507f1f77bcf86cd799439012";
        String expectedCreatedAt = "2025-11-17T22:49:20.389Z";
        FeedbackRequestDto requestDto = new FeedbackRequestDto(detractorScore, validComment);
        FeedbackModel mockModel = new FeedbackModel(detractorScore, validComment, FeedbackType.DETRACTOR, Instant.parse(expectedCreatedAt));
        mockModel.setId(new ObjectId(expectedFeedbackId));
        when(this.feedbackRepository.persist(any(FeedbackModel.class))).thenReturn(Uni.createFrom().item(mockModel));

        // When
        FeedbackResponseDto result = this.feedbackService
                .create(requestDto)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(expectedFeedbackId, result.id());
        assertEquals(detractorScore, result.score());
        assertEquals(validComment, result.comment());
        assertEquals(expectedCreatedAt, result.createdAt());
        assertEquals(FeedbackType.DETRACTOR, result.type());
        verify(this.feedbackRepository).persist(any(FeedbackModel.class));
        verify(this.detractorProducer).sendMessage(any(FeedbackDto.class));
    }

    @Test
    void testCreateWithBoundaryDetractorScore() {
        // Given - Score 6 is the highest detractor score
        int boundaryDetractorScore = 6;
        String validComment = "Below expectations";
        String expectedFeedbackId = "507f1f77bcf86cd799439013";
        String expectedCreatedAt = "2025-11-17T22:49:20.389Z";
        FeedbackRequestDto requestDto = new FeedbackRequestDto(boundaryDetractorScore, validComment);
        FeedbackModel mockModel = new FeedbackModel(boundaryDetractorScore, validComment, FeedbackType.DETRACTOR, Instant.parse(expectedCreatedAt));
        mockModel.setId(new ObjectId(expectedFeedbackId));
        when(this.feedbackRepository.persist(any(FeedbackModel.class))).thenReturn(Uni.createFrom().item(mockModel));

        // When
        FeedbackResponseDto result = this.feedbackService
                .create(requestDto)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(expectedFeedbackId, result.id());
        assertEquals(boundaryDetractorScore, result.score());
        assertEquals(validComment, result.comment());
        assertEquals(FeedbackType.DETRACTOR, result.type());
        verify(this.feedbackRepository).persist(any(FeedbackModel.class));
        verify(this.detractorProducer).sendMessage(any(FeedbackDto.class));
    }

    @Test
    void testCreateWithBoundaryNeutralScore() {
        // Given - Score 7 is the lowest neutral score (should NOT trigger detractor)
        int boundaryNeutralScore = 7;
        String validComment = "Acceptable service";
        String expectedFeedbackId = "507f1f77bcf86cd799439014";
        String expectedCreatedAt = "2025-11-17T22:49:20.389Z";
        FeedbackRequestDto requestDto = new FeedbackRequestDto(boundaryNeutralScore, validComment);
        FeedbackModel mockModel = new FeedbackModel(boundaryNeutralScore, validComment, FeedbackType.NEUTRAL, Instant.parse(expectedCreatedAt));
        mockModel.setId(new ObjectId(expectedFeedbackId));
        when(this.feedbackRepository.persist(any(FeedbackModel.class))).thenReturn(Uni.createFrom().item(mockModel));

        // When
        FeedbackResponseDto result = this.feedbackService
                .create(requestDto)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(expectedFeedbackId, result.id());
        assertEquals(boundaryNeutralScore, result.score());
        assertEquals(validComment, result.comment());
        assertEquals(FeedbackType.NEUTRAL, result.type());
        verify(this.feedbackRepository).persist(any(FeedbackModel.class));
        verify(this.detractorProducer, never()).sendMessage(any(FeedbackDto.class));
    }

    @Test
    void testCreateWithPromoterScore() {
        // Given - Score 9-10 are promoter scores
        int promoterScore = 9;
        String validComment = "Outstanding service!";
        String expectedFeedbackId = "507f1f77bcf86cd799439015";
        String expectedCreatedAt = "2025-11-17T22:49:20.389Z";
        FeedbackRequestDto requestDto = new FeedbackRequestDto(promoterScore, validComment);
        FeedbackModel mockModel = new FeedbackModel(promoterScore, validComment, FeedbackType.PROMOTER, Instant.parse(expectedCreatedAt));
        mockModel.setId(new ObjectId(expectedFeedbackId));
        when(this.feedbackRepository.persist(any(FeedbackModel.class))).thenReturn(Uni.createFrom().item(mockModel));

        // When
        FeedbackResponseDto result = this.feedbackService
                .create(requestDto)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(expectedFeedbackId, result.id());
        assertEquals(promoterScore, result.score());
        assertEquals(validComment, result.comment());
        assertEquals(FeedbackType.PROMOTER, result.type());
        verify(this.feedbackRepository).persist(any(FeedbackModel.class));
        verify(this.detractorProducer, never()).sendMessage(any(FeedbackDto.class));
    }
}
