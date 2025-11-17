package org.pulse.npsservice.mapper;

import org.pulse.npsservice.dto.FeedbackRequestDto;
import org.pulse.npsservice.dto.FeedbackResponseDto;
import org.pulse.npsservice.entity.FeedbackEntity;

public class FeedbackMapper {

    public static FeedbackEntity toFeedbackEntity(FeedbackRequestDto requestDto) {
        return new FeedbackEntity(
                requestDto.score(),
                requestDto.comment()
        );
    }

    public static FeedbackResponseDto toFeedbackResponseDto(FeedbackEntity entity) {
        return new FeedbackResponseDto(
                entity.getScore(),
                entity.getComment(),
                entity.getCreatedAt().toString()
        );
    }
}
