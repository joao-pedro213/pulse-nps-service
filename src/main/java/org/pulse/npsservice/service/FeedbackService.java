package org.pulse.npsservice.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.pulse.npsservice.dto.FeedbackRequestDto;
import org.pulse.npsservice.dto.FeedbackResponseDto;
import org.pulse.npsservice.model.FeedbackModel;
import org.pulse.npsservice.repository.FeedbackRepository;

import java.time.Instant;

@ApplicationScoped
public class FeedbackService {
    private FeedbackRepository feedbackRepository;

    private DetractorProducer detractorProducer;

    @Inject
    public FeedbackService(FeedbackRepository feedbackRepository, DetractorProducer detractorProducer) {
        this.feedbackRepository = feedbackRepository;
        this.detractorProducer = detractorProducer;
    }

    public Uni<FeedbackResponseDto> create(FeedbackRequestDto requestDto) {
        this.handleDetractor(requestDto);
        FeedbackModel feedbackModel = new FeedbackModel(requestDto.score(), requestDto.comment(), Instant.now());
        return this.feedbackRepository
                .persist(feedbackModel)
                .map(persistedFeedbackModel -> new FeedbackResponseDto(
                        persistedFeedbackModel.getId().toString(),
                        persistedFeedbackModel.getScore(),
                        persistedFeedbackModel.getComment(),
                        persistedFeedbackModel.getCreatedAt().toString()));
    }

    private void handleDetractor(FeedbackRequestDto requestDto) {
        if (requestDto.score() < 7) {
            this.detractorProducer.sendMessage(requestDto);
        }
    }
}
