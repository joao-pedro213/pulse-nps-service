package org.pulse.npsservice.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.pulse.npsservice.dto.FeedbackRequestDto;
import org.pulse.npsservice.dto.FeedbackResponseDto;
import org.pulse.npsservice.model.FeedbackModel;
import org.pulse.npsservice.repository.FeedbackRepository;

import java.time.Instant;

@ApplicationScoped
public class FeedbackService {

    @Inject
    FeedbackRepository feedbackRepository;

    public Uni<FeedbackResponseDto> create(@Valid FeedbackRequestDto requestDto) {
        FeedbackModel model = new FeedbackModel(
                requestDto.score(),
                requestDto.comment(),
                Instant.now()
        );
        return this.feedbackRepository
                .persist(model)
                .map(persistedModel -> new FeedbackResponseDto(
                        persistedModel.id.toString(),
                        persistedModel.score,
                        persistedModel.comment,
                        persistedModel.createdAt.toString()
                ));
    }
}
