package org.pulse.npsservice.service;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.pulse.npsservice.dto.FeedbackDto;

@ApplicationScoped
public class DetractorProducer {
    @Inject
    private ServiceBusClientBuilder clientBuilder;

    private ServiceBusSenderClient senderClient;

    private static final Logger LOGGER = Logger.getLogger(DetractorProducer.class);

    private static final String QUEUE_NAME = "detractors";

    @PostConstruct
    void initialize() {
        this.senderClient = this.clientBuilder.sender().queueName(QUEUE_NAME).buildClient();
    }

    @PreDestroy
    void cleanup() {
        if (this.senderClient != null) {
            this.senderClient.close();
        }
    }

    public void sendMessage(FeedbackDto feedbackDto) {
        try {
            String feedbackJson = new ObjectMapper().writeValueAsString(feedbackDto);
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(feedbackJson);
            this.senderClient.sendMessage(serviceBusMessage);
        } catch (JsonProcessingException exception) {
            LOGGER.error("Failed to parse Dto", exception);
        }
    }
}
