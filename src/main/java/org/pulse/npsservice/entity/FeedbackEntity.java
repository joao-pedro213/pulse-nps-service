package org.pulse.npsservice.entity;

import java.time.Instant;

public class FeedbackEntity {
    private int score;
    private String comment;
    private Instant createdAt;

    public FeedbackEntity() {
    }

    public FeedbackEntity(int score, String comment) {
        this.score = score;
        this.comment = comment;
        this.createdAt = Instant.now();
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
