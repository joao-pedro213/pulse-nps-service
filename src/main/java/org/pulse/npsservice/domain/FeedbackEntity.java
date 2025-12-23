package org.pulse.npsservice.domain;

import java.time.Instant;

public class FeedbackEntity {
    private int score;
    private String comment;
    private FeedbackType type;
    private Instant createdAt;

    public FeedbackEntity(int score, String comment) {
        this.score = score;
        this.comment = comment;
        this.createdAt = Instant.now();
        this.type = calculateType(score);
    }

    private static FeedbackType calculateType(int score) {
        if (score >= 1 && score <= 6) {
            return FeedbackType.DETRACTOR;
        } else if (score >= 7 && score <= 8) {
            return FeedbackType.NEUTRAL;
        } else if (score >= 9 && score <= 10) {
            return FeedbackType.PROMOTER;
        }
        throw new IllegalArgumentException("Score must be between 1 and 10, got: " + score);
    }

    public boolean isDetractor() {
        return this.type == FeedbackType.DETRACTOR;
    }

    public boolean isNeutral() {
        return this.type == FeedbackType.NEUTRAL;
    }

    public boolean isPromoter() {
        return this.type == FeedbackType.PROMOTER;
    }

    public int getScore() {
        return score;
    }

    public String getComment() {
        return comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public FeedbackType getType() {
        return type;
    }
}
