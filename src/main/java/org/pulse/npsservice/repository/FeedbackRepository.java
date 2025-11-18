package org.pulse.npsservice.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.pulse.npsservice.model.FeedbackModel;

@ApplicationScoped
public class FeedbackRepository implements ReactivePanacheMongoRepository<FeedbackModel> {
}
