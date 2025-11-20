package org.pulse.npsservice.handler;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.pulse.npsservice.dto.ErrorResponse;
import org.pulse.npsservice.dto.FeedbackResponseDto;
import org.pulse.npsservice.function.HttpResponseMessageMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ResponseBuilderTest {
    @Inject
    ResponseBuilder responseBuilder;

    @InjectMock
    ExceptionMapper exceptionMapper;

    @Test
    void testCreatedWithBody() {
        // Given
        String feedbackId = "507f1f77bcf86cd799439011";
        int feedbackScore = 8;
        String feedbackComment = "Great service!";
        String feedbackCreatedAt = "2025-11-17T22:49:20.389Z";
        @SuppressWarnings("unchecked")
        HttpRequestMessage<Object> request = mock(HttpRequestMessage.class);
        FeedbackResponseDto responseDto = new FeedbackResponseDto(
                feedbackId,
                feedbackScore,
                feedbackComment,
                feedbackCreatedAt);

        doAnswer(invocation -> {
            HttpStatus status = invocation.getArgument(0);
            return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
        }).when(request).createResponseBuilder(any(HttpStatus.class));

        // When
        HttpResponseMessage response = this.responseBuilder.created(request, responseDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.getBody());
        assertInstanceOf(FeedbackResponseDto.class, response.getBody());
        FeedbackResponseDto body = (FeedbackResponseDto) response.getBody();
        assertEquals(feedbackId, body.id());
        assertEquals(feedbackScore, body.score());
        assertEquals(feedbackComment, body.comment());
        verify(request).createResponseBuilder(HttpStatus.CREATED);
    }

    @Test
    void testErrorWithRuntimeException() {
        // Given
        String exceptionMessage = "Internal error";
        String expectedErrorMessage = "Internal server error";
        int expectedStatusCode = 500;
        @SuppressWarnings("unchecked")
        HttpRequestMessage<Object> request = mock(HttpRequestMessage.class);
        RuntimeException exception = new RuntimeException(exceptionMessage);
        ErrorResponse errorResponse = new ErrorResponse(expectedErrorMessage, expectedStatusCode);
        when(this.exceptionMapper.mapToErrorResponse(exception)).thenReturn(errorResponse);
        doAnswer(invocation -> {
            HttpStatus status = invocation.getArgument(0);
            return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
        }).when(request).createResponseBuilder(any(HttpStatus.class));

        // When
        HttpResponseMessage response = this.responseBuilder.error(request, exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(expectedErrorMessage, body.message());
        assertEquals(expectedStatusCode, body.status());
        verify(this.exceptionMapper).mapToErrorResponse(exception);
        verify(request).createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
