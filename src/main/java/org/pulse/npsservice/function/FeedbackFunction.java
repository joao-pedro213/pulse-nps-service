package org.pulse.npsservice.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import jakarta.enterprise.context.ApplicationScoped;
import org.pulse.npsservice.dto.FeedbackRequestDto;
import org.pulse.npsservice.dto.FeedbackResponseDto;
import org.pulse.npsservice.entity.FeedbackEntity;
import org.pulse.npsservice.mapper.FeedbackMapper;

import java.util.Optional;

@ApplicationScoped
public class FeedbackFunction {
    @FunctionName("feedback")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<FeedbackRequestDto>> request,
            final ExecutionContext context) {
        Optional<FeedbackRequestDto> requestDto = request.getBody();
        if (requestDto.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Request body is required").build();
        }
        FeedbackEntity entity = FeedbackMapper.toFeedbackEntity(requestDto.get());
        FeedbackResponseDto responseDto = FeedbackMapper.toFeedbackResponseDto(entity);
        return request.createResponseBuilder(HttpStatus.CREATED).body(responseDto).build();
    }
}
