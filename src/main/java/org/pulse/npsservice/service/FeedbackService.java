package org.pulse.npsservice.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.pulse.npsservice.domain.FeedbackEntity;
import org.pulse.npsservice.dto.FeedbackDto;
import org.pulse.npsservice.dto.FeedbackRequestDto;
import org.pulse.npsservice.dto.FeedbackResponseDto;
import org.pulse.npsservice.model.FeedbackModel;
import org.pulse.npsservice.repository.FeedbackRepository;

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
        FeedbackEntity feedbackEntity = new FeedbackEntity(requestDto.score(), requestDto.comment());
        this.handleDetractor(feedbackEntity);
        FeedbackModel feedbackModel = new FeedbackModel(
                feedbackEntity.getScore(),
                feedbackEntity.getComment(),
                feedbackEntity.getType(),
                feedbackEntity.getCreatedAt());
        return this.feedbackRepository
                .persist(feedbackModel)
                .map(persistedFeedbackModel -> new FeedbackResponseDto(
                        persistedFeedbackModel.getId().toString(),
                        persistedFeedbackModel.getScore(),
                        persistedFeedbackModel.getComment(),
                        persistedFeedbackModel.getType(),
                        persistedFeedbackModel.getCreatedAt().toString()));
    }

    private void handleDetractor(FeedbackEntity feedbackEntity) {
        if (feedbackEntity.isDetractor()) {
            FeedbackDto feedbackDto = new FeedbackDto(
                    feedbackEntity.getScore(),
                    feedbackEntity.getComment(),
                    feedbackEntity.getType());
            this.detractorProducer.sendMessage(feedbackDto);
        }
    }
}
