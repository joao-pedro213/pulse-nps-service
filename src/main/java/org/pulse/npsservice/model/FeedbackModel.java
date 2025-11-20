package org.pulse.npsservice.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

import java.time.Instant;

@MongoEntity(collection = "feedbacks")
public class FeedbackModel {
    private ObjectId id;
    private int score;
    private String comment;
    private Instant createdAt;

    public FeedbackModel() {
    }

    public FeedbackModel(int score, String comment, Instant createdAt) {
        this.score = score;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
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
