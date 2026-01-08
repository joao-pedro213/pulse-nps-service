package org.pulse.npsservice.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import org.pulse.npsservice.model.FeedbackModel;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class FeedbackRepository implements ReactivePanacheMongoRepository<FeedbackModel> {
    public Uni<List<FeedbackModel>> findByDateRange(Instant startDate, Instant endDate) {
        return find(new Document("createdAt", new Document("$gte", Date.from(startDate)).append("$lt", Date.from(endDate)))).list();
    }
}
