package org.pulse.npsservice.service;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailMessage;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.pulse.npsservice.dto.FeedbackRequestDto;

@ApplicationScoped
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class);

    @ConfigProperty(name = "azure.communication.email.connection-string")
    String connectionString;

    @ConfigProperty(name = "email.detractor.sender")
    String senderAddress;

    @ConfigProperty(name = "email.detractor.recipient")
    String detractorRecipient;

    private EmailClient emailClient;

    @PostConstruct
    public void init() {
        this.emailClient = new EmailClientBuilder().connectionString(this.connectionString).buildClient();
    }

    public void sendDetractorNotification(FeedbackRequestDto feedback) {
        try {
            EmailMessage message = new EmailMessage()
                    .setSenderAddress(this.senderAddress)
                    .setToRecipients(new EmailAddress(this.detractorRecipient))
                    .setSubject("Detractor Alert - Low NPS Score Received")
                    .setBodyPlainText(this.buildEmailBody(feedback));
            this.emailClient.beginSend(message);
        } catch (Exception exception) {
            LOGGER.error("Failed to send detractor notification email", exception);
            throw new RuntimeException("Error sending detractor notification email", exception);
        }
    }

    private String buildEmailBody(FeedbackRequestDto feedback) {
        return String.format("""
                A detractor feedback has been received:
                
                Score: %d/10
                Comment: %s
                
                Please follow up with this customer as soon as possible.
                
                ---
                This is an automated notification from the Pulse NPS Service.
                """, feedback.score(), feedback.comment());
    }
}
