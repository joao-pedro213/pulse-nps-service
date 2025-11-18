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
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import org.pulse.npsservice.dto.ErrorResponse;
import org.pulse.npsservice.dto.FeedbackRequestDto;
import org.pulse.npsservice.service.FeedbackService;

import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class FeedbackFunction {

    private final FeedbackService feedbackService;

    @Inject
    public FeedbackFunction(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @FunctionName("feedback")
    public HttpResponseMessage createFeedbackHandler(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<FeedbackRequestDto>> request,
            final ExecutionContext context) {

        context.getLogger().info("Processing feedback creation request");

        Optional<FeedbackRequestDto> requestDto = request.getBody();
        return requestDto
                .map(dto -> this.createFeedback(dto, request))
                .orElseGet(() -> this.createErrorResponse(request, "Request body is required", HttpStatus.BAD_REQUEST));
    }

    private HttpResponseMessage createFeedback(FeedbackRequestDto dto, HttpRequestMessage<?> request) {
        return feedbackService.create(dto)
                .map(responseDto -> request.createResponseBuilder(HttpStatus.CREATED).body(responseDto).build())
                .onFailure(ConstraintViolationException.class)
                .recoverWithItem(exception -> this.handleValidationError(request, exception))
                .onFailure()
                .recoverWithItem(ex -> this.createErrorResponse(request, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR))
                .await().indefinitely();
    }

    private HttpResponseMessage handleValidationError(HttpRequestMessage<?> request, Throwable throwable) {
        ConstraintViolationException exception = (ConstraintViolationException) throwable;
        String errors = exception
                .getConstraintViolations()
                .stream()
                .map(jakarta.validation.ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return this.createErrorResponse(request, errors, HttpStatus.BAD_REQUEST);
    }

    private HttpResponseMessage createErrorResponse(HttpRequestMessage<?> request, String message, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(message, status.value());
        return request.createResponseBuilder(status).body(errorResponse).build();
    }
}
