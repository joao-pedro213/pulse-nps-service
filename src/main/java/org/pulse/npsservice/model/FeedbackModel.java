package org.pulse.npsservice.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;
import org.pulse.npsservice.domain.FeedbackType;

import java.time.Instant;

@MongoEntity(collection = "feedbacks")
public class FeedbackModel {
    private ObjectId id;
    private int score;
    private String comment;
    private FeedbackType type;
    private Instant createdAt;

    public FeedbackModel() {
    }

    public FeedbackModel(int score, String comment, FeedbackType type, Instant createdAt) {
        this.score = score;
        this.comment = comment;
        this.type = type;
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

    public FeedbackType getType() {
        return type;
    }

    public void setType(FeedbackType type) {
        this.type = type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
