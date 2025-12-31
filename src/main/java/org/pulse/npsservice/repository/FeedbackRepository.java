package org.pulse.npsservice.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.pulse.npsservice.model.FeedbackModel;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class FeedbackRepository implements ReactivePanacheMongoRepository<FeedbackModel> {
    public Uni<List<FeedbackModel>> findByDateRange(Instant startDate, Instant endDate) {
        return find("createdAt >= ?1 and createdAt < ?2", startDate, endDate).list();
    }
}
