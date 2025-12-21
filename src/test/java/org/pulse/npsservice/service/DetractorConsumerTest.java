package org.pulse.npsservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.pulse.npsservice.dto.FeedbackRequestDto;

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
        FeedbackRequestDto requestDto = new FeedbackRequestDto(score, comment);
        String message = objectMapper.writeValueAsString(requestDto);
        doNothing().when(emailService).sendDetractorNotification(any(FeedbackRequestDto.class));

        // When
        detractorConsumer.processMessage(message);

        // Then - verify email service was called
        verify(emailService).sendDetractorNotification(any(FeedbackRequestDto.class));
    }

    @Test
    void testProcessMessageWithInvalidJson() {
        // Given
        String invalidMessage = "{ invalid json }";

        // When/Then
        assertThrows(RuntimeException.class, () -> detractorConsumer.processMessage(invalidMessage));
    }

    @Test
    void testProcessMessageWithEmptyString() {
        // Given
        String emptyMessage = "";

        // When/Then
        assertThrows(RuntimeException.class, () -> detractorConsumer.processMessage(emptyMessage));
    }
}
