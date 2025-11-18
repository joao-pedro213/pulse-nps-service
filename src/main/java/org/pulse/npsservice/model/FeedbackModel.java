package org.pulse.npsservice.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

import java.time.Instant;

@MongoEntity(collection = "feedbacks")
public class FeedbackModel {

    public ObjectId id;
    public int score;
    public String comment;
    public Instant createdAt;

    public FeedbackModel() {
    }

    public FeedbackModel(int score, String comment, Instant createdAt) {
        this.score = score;
        this.comment = comment;
        this.createdAt = createdAt;
    }

}
