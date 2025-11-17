package org.pulse.npsservice.mapper;

import org.junit.jupiter.api.Test;
import org.pulse.npsservice.dto.FeedbackRequestDto;
import org.pulse.npsservice.dto.FeedbackResponseDto;
import org.pulse.npsservice.entity.FeedbackEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedbackMapperTest {

    @Test
    void testToFeedbackEntityShouldMapRequestDtoToEntity() {
        // Given
        FeedbackRequestDto requestDto = new FeedbackRequestDto(9, "Excellent experience!");

        // When
        FeedbackEntity entity = FeedbackMapper.toFeedbackEntity(requestDto);

        // Then
        assertNotNull(entity);
        assertEquals(9, entity.getScore());
        assertEquals("Excellent experience!", entity.getComment());
        assertNotNull(entity.getCreatedAt());
        assertTrue(entity.getCreatedAt().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void testToFeedbackEntityShouldSetCreatedAtAutomatically() {
        // Given
        FeedbackRequestDto requestDto = new FeedbackRequestDto(7, "Good service");
        Instant beforeCreation = Instant.now();

        // When
        FeedbackEntity entity = FeedbackMapper.toFeedbackEntity(requestDto);

        // Then
        assertNotNull(entity.getCreatedAt());
        assertTrue(entity.getCreatedAt().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(entity.getCreatedAt().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void testToFeedbackResponseDtoShouldMapEntityToResponseDto() {
        // Given
        FeedbackEntity entity = new FeedbackEntity(8, "Great product!");
        Instant createdAt = entity.getCreatedAt();

        // When
        FeedbackResponseDto responseDto = FeedbackMapper.toFeedbackResponseDto(entity);

        // Then
        assertNotNull(responseDto);
        assertEquals(8, responseDto.score());
        assertEquals("Great product!", responseDto.comment());
        assertEquals(createdAt.toString(), responseDto.createdAt());
    }

    @Test
    void testToFeedbackResponseDtoShouldPreserveAllFields() {
        // Given
        FeedbackEntity entity = new FeedbackEntity(10, "Perfect!");

        // When
        FeedbackResponseDto responseDto = FeedbackMapper.toFeedbackResponseDto(entity);

        // Then
        assertEquals(entity.getScore(), responseDto.score());
        assertEquals(entity.getComment(), responseDto.comment());
        assertEquals(entity.getCreatedAt().toString(), responseDto.createdAt());
    }

    @Test
    void testToFeedbackEntityWithMinimumScore() {
        // Given
        FeedbackRequestDto requestDto = new FeedbackRequestDto(0, "Poor experience");

        // When
        FeedbackEntity entity = FeedbackMapper.toFeedbackEntity(requestDto);

        // Then
        assertEquals(0, entity.getScore());
        assertEquals("Poor experience", entity.getComment());
    }

    @Test
    void testToFeedbackEntityWithMaximumScore() {
        // Given
        FeedbackRequestDto requestDto = new FeedbackRequestDto(10, "Outstanding!");

        // When
        FeedbackEntity entity = FeedbackMapper.toFeedbackEntity(requestDto);

        // Then
        assertEquals(10, entity.getScore());
        assertEquals("Outstanding!", entity.getComment());
    }

    @Test
    void testToFeedbackEntityWithEmptyComment() {
        // Given
        FeedbackRequestDto requestDto = new FeedbackRequestDto(5, "");

        // When
        FeedbackEntity entity = FeedbackMapper.toFeedbackEntity(requestDto);

        // Then
        assertEquals(5, entity.getScore());
        assertEquals("", entity.getComment());
    }
}
