package org.pulse.npsservice.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.pulse.npsservice.dto.FeedbackRequestDto;
import org.pulse.npsservice.service.DetractorConsumer;

import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@QuarkusTest
class DetractorQueueFunctionTest {

    @Inject
    DetractorNotificationFunction function;

    @InjectMock
    DetractorConsumer detractorConsumer;

    @Inject
    ObjectMapper objectMapper;

    @Test
    void testQueueTriggerSuccessfully() throws Exception {
        // Given
        FeedbackRequestDto requestDto = new FeedbackRequestDto(5, "Poor service");
        String message = this.objectMapper.writeValueAsString(requestDto);
        ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();

        // When
        this.function.notify(message, context);

        // Then
        verify(this.detractorConsumer).processMessage(message);
    }

    @Test
    void testQueueTriggerWithError() {
        // Given
        String invalidMessage = "{ invalid json }";
        ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();
        doThrow(new RuntimeException("Processing failed")).when(this.detractorConsumer).processMessage(anyString());

        // When/Then - should propagate exception
        try {
            this.function.notify(invalidMessage, context);
        } catch (RuntimeException e) {
            // Expected behavior - exception should propagate
            verify(this.detractorConsumer).processMessage(invalidMessage);
        }
    }
}
