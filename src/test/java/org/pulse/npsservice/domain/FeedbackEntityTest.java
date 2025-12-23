package org.pulse.npsservice.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedbackEntityTest {

    @Test
    void testCreateEntity() {
        // Given
        int score = 5;
        String comment = "Poor service";

        // When
        FeedbackEntity entity = new FeedbackEntity(score, comment);

        // Then
        assertNotNull(entity);
        assertEquals(5, entity.getScore());
        assertEquals("Poor service", entity.getComment());
        assertNotNull(entity.getCreatedAt());
        assertEquals(FeedbackType.DETRACTOR, entity.getType());
    }

    @Test
    void testIsDetractor() {
        // Test boundary values
        assertTrue(new FeedbackEntity(1, "Bad").isDetractor());
        assertTrue(new FeedbackEntity(6, "Below average").isDetractor());
        assertFalse(new FeedbackEntity(7, "Okay").isDetractor());
        assertFalse(new FeedbackEntity(9, "Great").isDetractor());
    }

    @Test
    void testIsNeutral() {
        // Test boundary values
        assertFalse(new FeedbackEntity(6, "Below average").isNeutral());
        assertTrue(new FeedbackEntity(7, "Okay").isNeutral());
        assertTrue(new FeedbackEntity(8, "Good").isNeutral());
        assertFalse(new FeedbackEntity(9, "Great").isNeutral());
    }

    @Test
    void testIsPromoter() {
        // Test boundary values
        assertFalse(new FeedbackEntity(8, "Good").isPromoter());
        assertTrue(new FeedbackEntity(9, "Great").isPromoter());
        assertTrue(new FeedbackEntity(10, "Perfect!").isPromoter());
    }

    @Test
    void testInvalidScoreBelowRange() {
        assertThrows(IllegalArgumentException.class, () -> new FeedbackEntity(0, "Test"));
        assertThrows(IllegalArgumentException.class, () -> new FeedbackEntity(-1, "Test"));
    }

    @Test
    void testInvalidScoreAboveRange() {
        assertThrows(IllegalArgumentException.class, () -> new FeedbackEntity(11, "Test"));
        assertThrows(IllegalArgumentException.class, () -> new FeedbackEntity(100, "Test"));
    }

    @Test
    void testEquality() {
        // Given
        FeedbackEntity entity1 = new FeedbackEntity(5, "Test");
        FeedbackEntity entity2 = new FeedbackEntity(5, "Test");

        // Then - entities with same score and comment should be equal
        assertEquals(entity1.getScore(), entity2.getScore());
        assertEquals(entity1.getComment(), entity2.getComment());
        assertEquals(entity1.getType(), entity2.getType());
    }

    @Test
    void testAllClassificationTypes() {
        // Detractor
        FeedbackEntity detractor = new FeedbackEntity(5, "Bad");
        assertTrue(detractor.isDetractor());
        assertFalse(detractor.isNeutral());
        assertFalse(detractor.isPromoter());
        assertEquals(FeedbackType.DETRACTOR, detractor.getType());

        // Neutral
        FeedbackEntity neutral = new FeedbackEntity(7, "Okay");
        assertFalse(neutral.isDetractor());
        assertTrue(neutral.isNeutral());
        assertFalse(neutral.isPromoter());
        assertEquals(FeedbackType.NEUTRAL, neutral.getType());

        // Promoter
        FeedbackEntity promoter = new FeedbackEntity(10, "Excellent");
        assertFalse(promoter.isDetractor());
        assertFalse(promoter.isNeutral());
        assertTrue(promoter.isPromoter());
        assertEquals(FeedbackType.PROMOTER, promoter.getType());
    }

    @Test
    void testBoundaryValues() {
        // Test critical boundary transitions
        assertEquals(FeedbackType.DETRACTOR, new FeedbackEntity(6, "Test").getType());
        assertEquals(FeedbackType.NEUTRAL, new FeedbackEntity(7, "Test").getType());
        assertEquals(FeedbackType.NEUTRAL, new FeedbackEntity(8, "Test").getType());
        assertEquals(FeedbackType.PROMOTER, new FeedbackEntity(9, "Test").getType());
    }
}
