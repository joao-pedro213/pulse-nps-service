package org.pulse.npsservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.pulse.npsservice.dto.FeedbackDto;

@ApplicationScoped
public class DetractorConsumer {
    private static final Logger LOGGER = Logger.getLogger(DetractorConsumer.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    EmailService emailService;

    public void processMessage(String message) {
        try {
            FeedbackDto feedback = this.objectMapper.readValue(message, FeedbackDto.class);
            this.emailService.sendDetractorNotification(feedback);

        } catch (Exception exception) {
            LOGGER.error("Failed to process detractor message", exception);
            throw new RuntimeException("Error processing detractor message", exception);
        }
    }
}
