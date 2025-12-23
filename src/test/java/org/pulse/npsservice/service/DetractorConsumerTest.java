package org.pulse.npsservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.pulse.npsservice.domain.FeedbackType;
import org.pulse.npsservice.dto.FeedbackDto;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@QuarkusTest
class DetractorConsumerTest {

    @Inject
    DetractorConsumer detractorConsumer;

    @Inject
    ObjectMapper objectMapper;

    @InjectMock
    EmailService emailService;

    @Test
    void testProcessMessageSuccessfully() throws Exception {
        // Given
        int score = 5;
        String comment = "Poor service";
        FeedbackDto feedbackDto = new FeedbackDto(score, comment, FeedbackType.DETRACTOR);
        String message = this.objectMapper.writeValueAsString(feedbackDto);
        doNothing().when(this.emailService).sendDetractorNotification(any(FeedbackDto.class));

        // When
        this.detractorConsumer.processMessage(message);

        // Then - verify email service was called
        verify(this.emailService).sendDetractorNotification(any(FeedbackDto.class));
    }

    @Test
    void testProcessMessageWithInvalidJson() {
        // Given
        String invalidMessage = "{ invalid json }";

        // When/Then
        assertThrows(RuntimeException.class, () -> this.detractorConsumer.processMessage(invalidMessage));
    }

    @Test
    void testProcessMessageWithEmptyString() {
        // Given
        String emptyMessage = "";

        // When/Then
        assertThrows(RuntimeException.class, () -> this.detractorConsumer.processMessage(emptyMessage));
    }
}
