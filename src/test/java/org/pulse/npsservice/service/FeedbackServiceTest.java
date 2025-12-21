package org.pulse.npsservice.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
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
        FeedbackModel mockModel = new FeedbackModel(validScore, validComment, Instant.parse(expectedCreatedAt));
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
        verify(this.feedbackRepository).persist(any(FeedbackModel.class));
        verify(this.detractorProducer, never()).sendMessage(any(FeedbackRequestDto.class));
    }

    @Test
    void testCreateWithDetractor() {
        // Given
        int detractorScore = 5;
        String validComment = "Poor service";
        String expectedFeedbackId = "507f1f77bcf86cd799439012";
        String expectedCreatedAt = "2025-11-17T22:49:20.389Z";
        FeedbackRequestDto requestDto = new FeedbackRequestDto(detractorScore, validComment);
        FeedbackModel mockModel = new FeedbackModel(detractorScore, validComment, Instant.parse(expectedCreatedAt));
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
        verify(this.feedbackRepository).persist(any(FeedbackModel.class));
        verify(this.detractorProducer).sendMessage(requestDto);
    }
}
