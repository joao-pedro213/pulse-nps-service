package org.pulse.npsservice.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.pulse.npsservice.dto.FeedbackRequestDto;
import org.pulse.npsservice.handler.ResponseBuilder;
import org.pulse.npsservice.service.FeedbackService;

import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class FeedbackFunction {
    private FeedbackService feedbackService;
    private ResponseBuilder responseBuilder;
    private Validator validator;

    @Inject
    public FeedbackFunction(FeedbackService feedbackService, ResponseBuilder responseBuilder, Validator validator) {
        this.feedbackService = feedbackService;
        this.responseBuilder = responseBuilder;
        this.validator = validator;
    }

    @FunctionName("feedback")
    public HttpResponseMessage create(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ADMIN)
            HttpRequestMessage<Optional<FeedbackRequestDto>> request,
            final ExecutionContext context) {
        return request
                .getBody()
                .map(feedbackRequestDto -> {
                    Set<ConstraintViolation<FeedbackRequestDto>> violations = this.validator.validate(feedbackRequestDto);
                    if (!violations.isEmpty()) {
                        return this.responseBuilder.error(request, new ConstraintViolationException(violations));
                    }
                    return this.feedbackService
                            .create(feedbackRequestDto)
                            .map(responseDto -> this.responseBuilder.created(request, responseDto))
                            .onFailure()
                            .recoverWithItem(throwable -> this.responseBuilder.error(request, throwable))
                            .await()
                            .indefinitely();
                })
                .orElseGet(() -> this.responseBuilder.error(request, new IllegalArgumentException("Request body is required")));
    }
}
