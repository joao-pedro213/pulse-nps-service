package org.pulse.npsservice.service;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pulse.npsservice.domain.FeedbackType;
import org.pulse.npsservice.dto.FeedbackDto;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class EmailServiceTest {

    @InjectSpy
    EmailService emailService;

    private EmailClient emailClient;

    @BeforeEach
    void setUp() throws Exception {
        this.emailClient = mock(EmailClient.class);
        Field emailClientField = EmailService.class.getDeclaredField("emailClient");
        emailClientField.setAccessible(true);
        emailClientField.set(this.emailService, this.emailClient);
    }

    @Test
    void testSendDetractorNotificationSuccessfully() {
        // Given
        FeedbackDto feedback = new FeedbackDto(4, "Service was below expectations", FeedbackType.DETRACTOR);
        @SuppressWarnings("unchecked")
        SyncPoller<EmailSendResult, EmailSendResult> mockPoller = mock(SyncPoller.class);
        PollResponse<EmailSendResult> mockResponse = mock(PollResponse.class);
        EmailSendResult mockResult = new EmailSendResult("test-message-id", EmailSendStatus.SUCCEEDED, null);
        when(this.emailClient.beginSend(any())).thenReturn(mockPoller);
        when(mockPoller.waitForCompletion()).thenReturn(mockResponse);
        when(mockResponse.getValue()).thenReturn(mockResult);

        // When
        this.emailService.sendDetractorNotification(feedback);

        // Then
        ArgumentCaptor<com.azure.communication.email.models.EmailMessage> messageCaptor =
                ArgumentCaptor.forClass(com.azure.communication.email.models.EmailMessage.class);
        verify(this.emailClient).beginSend(messageCaptor.capture());
        com.azure.communication.email.models.EmailMessage capturedMessage = messageCaptor.getValue();
        assertTrue(capturedMessage.getBodyPlainText().contains("Score: 4/10"));
        assertTrue(capturedMessage.getBodyPlainText().contains("Type: DETRACTOR"));
        assertTrue(capturedMessage.getBodyPlainText().contains("Service was below expectations"));
    }

    @Test
    void testEmailBodyContainsExpectedContent() {
        // Given
        FeedbackDto feedback = new FeedbackDto(2, "Terrible experience", FeedbackType.DETRACTOR);
        @SuppressWarnings("unchecked")
        SyncPoller<EmailSendResult, EmailSendResult> mockPoller = mock(SyncPoller.class);
        PollResponse<EmailSendResult> mockResponse = mock(PollResponse.class);
        EmailSendResult mockResult = new EmailSendResult("test-message-id", EmailSendStatus.SUCCEEDED, null);
        when(this.emailClient.beginSend(any())).thenReturn(mockPoller);
        when(mockPoller.waitForCompletion()).thenReturn(mockResponse);
        when(mockResponse.getValue()).thenReturn(mockResult);

        // When
        this.emailService.sendDetractorNotification(feedback);

        // Then
        ArgumentCaptor<com.azure.communication.email.models.EmailMessage> messageCaptor =
                ArgumentCaptor.forClass(com.azure.communication.email.models.EmailMessage.class);
        verify(this.emailClient).beginSend(messageCaptor.capture());
        com.azure.communication.email.models.EmailMessage capturedMessage = messageCaptor.getValue();
        String body = capturedMessage.getBodyPlainText();
        assertTrue(body.contains("A detractor feedback has been received"));
        assertTrue(body.contains("Score: 2/10"));
        assertTrue(body.contains("Type: DETRACTOR"));
        assertTrue(body.contains("Comment: Terrible experience"));
        assertTrue(body.contains("Please follow up with this customer"));
        assertTrue(body.contains("Pulse NPS Service"));
    }
}
