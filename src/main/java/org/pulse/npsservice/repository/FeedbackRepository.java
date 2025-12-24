package org.pulse.npsservice.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.pulse.npsservice.model.FeedbackModel;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class FeedbackRepository implements ReactivePanacheMongoRepository<FeedbackModel> {

    /**
     * Finds all feedbacks created within the specified date range.
     *
     * @param startDate the start of the date range (inclusive)
     * @param endDate the end of the date range (exclusive)
     * @return Uni containing a list of feedbacks
     */
    public Uni<List<FeedbackModel>> findByDateRange(Instant startDate, Instant endDate) {
        return find("createdAt >= ?1 and createdAt < ?2", startDate, endDate).list();
    }
}
