package org.pulse.npsservice.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pulse.npsservice.domain.FeedbackType;
import org.pulse.npsservice.dto.ErrorResponse;
import org.pulse.npsservice.dto.FeedbackRequestDto;
import org.pulse.npsservice.dto.FeedbackResponseDto;
import org.pulse.npsservice.service.FeedbackService;

import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class FeedbackFunctionTest {

    @Inject
    FeedbackFunction function;

    @InjectMock
    FeedbackService feedbackService;

    @Test
    void testFeedbackFunctionWithValidRequest() {
        // Given
        @SuppressWarnings("unchecked") final HttpRequestMessage<Optional<FeedbackRequestDto>> req = mock(HttpRequestMessage.class);
        final FeedbackRequestDto requestDto = new FeedbackRequestDto(8, "Great service!");
        final Optional<FeedbackRequestDto> queryBody = Optional.of(requestDto);
        doReturn(queryBody).when(req).getBody();
        final FeedbackResponseDto mockResponse = new FeedbackResponseDto(
                "507f1f77bcf86cd799439011",
                8,
                "Great service!",
                FeedbackType.NEUTRAL,
                "2025-11-17T22:49:20.389Z"
        );
        when(this.feedbackService.create(any(FeedbackRequestDto.class))).thenReturn(Uni.createFrom().item(mockResponse));
        doAnswer((Answer<HttpResponseMessage.Builder>) invocation -> {
            HttpStatus status = (HttpStatus) invocation.getArguments()[0];
            return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
        }).when(req).createResponseBuilder(any(HttpStatus.class));
        final ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();

        // When
        final HttpResponseMessage ret = function.create(req, context);

        // Then
        assertEquals(HttpStatus.CREATED, ret.getStatus());
        assertNotNull(ret.getBody());
        FeedbackResponseDto responseDto = (FeedbackResponseDto) ret.getBody();
        assertEquals("507f1f77bcf86cd799439011", responseDto.id());
        assertEquals(8, responseDto.score());
        assertEquals("Great service!", responseDto.comment());
        assertEquals("2025-11-17T22:49:20.389Z", responseDto.createdAt());
    }

    @Test
    void testFeedbackFunctionWithEmptyBody() {
        // Given
        @SuppressWarnings("unchecked") final HttpRequestMessage<Optional<FeedbackRequestDto>> req = mock(HttpRequestMessage.class);
        final Optional<FeedbackRequestDto> queryBody = Optional.empty();
        doReturn(queryBody).when(req).getBody();
        doAnswer(new Answer<HttpResponseMessage.Builder>() {
            @Override
            public HttpResponseMessage.Builder answer(InvocationOnMock invocation) {
                HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
            }
        }).when(req).createResponseBuilder(any(HttpStatus.class));
        final ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();

        // When
        final HttpResponseMessage ret = function.create(req, context);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ret.getStatus());
        assertNotNull(ret.getBody());
        assertInstanceOf(ErrorResponse.class, ret.getBody());
        ErrorResponse errorResponse = (ErrorResponse) ret.getBody();
        assertEquals("Request body is required", errorResponse.message());
        assertEquals(400, errorResponse.status());
    }

    @Test
    void testFeedbackFunctionWithValidationError() {
        // Given
        @SuppressWarnings("unchecked") final HttpRequestMessage<Optional<FeedbackRequestDto>> req = mock(HttpRequestMessage.class);
        final FeedbackRequestDto requestDto = new FeedbackRequestDto(8, "Great service!");
        final Optional<FeedbackRequestDto> queryBody = Optional.of(requestDto);
        doReturn(queryBody).when(req).getBody();

        // Mock validation exception from service
        when(this.feedbackService.create(any(FeedbackRequestDto.class)))
                .thenReturn(Uni.createFrom().failure(mock(ConstraintViolationException.class)));

        doAnswer((Answer<HttpResponseMessage.Builder>) invocation -> {
            HttpStatus status = (HttpStatus) invocation.getArguments()[0];
            return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
        }).when(req).createResponseBuilder(any(HttpStatus.class));
        final ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();

        // When
        final HttpResponseMessage ret = function.create(req, context);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ret.getStatus());
        assertNotNull(ret.getBody());
        assertInstanceOf(ErrorResponse.class, ret.getBody());
    }

    @Test
    void testFeedbackFunctionWithInternalError() {
        // Given
        @SuppressWarnings("unchecked") final HttpRequestMessage<Optional<FeedbackRequestDto>> req = mock(HttpRequestMessage.class);
        final FeedbackRequestDto requestDto = new FeedbackRequestDto(8, "Great service!");
        final Optional<FeedbackRequestDto> queryBody = Optional.of(requestDto);
        doReturn(queryBody).when(req).getBody();

        // Mock unexpected exception from service
        when(this.feedbackService.create(any(FeedbackRequestDto.class)))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database connection failed")));

        doAnswer((Answer<HttpResponseMessage.Builder>) invocation -> {
            HttpStatus status = (HttpStatus) invocation.getArguments()[0];
            return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
        }).when(req).createResponseBuilder(any(HttpStatus.class));
        final ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();

        // When
        final HttpResponseMessage ret = function.create(req, context);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ret.getStatus());
        assertNotNull(ret.getBody());
        assertInstanceOf(ErrorResponse.class, ret.getBody());
        ErrorResponse errorResponse = (ErrorResponse) ret.getBody();
        assertEquals("Internal server error", errorResponse.message());
        assertEquals(500, errorResponse.status());
    }
}
