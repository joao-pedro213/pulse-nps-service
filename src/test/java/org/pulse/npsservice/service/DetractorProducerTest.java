package org.pulse.npsservice.service;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pulse.npsservice.dto.FeedbackRequestDto;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@QuarkusTest
class DetractorProducerTest {
    @InjectSpy
    DetractorProducer detractorProducer;

    private ServiceBusSenderClient senderClient;

    @BeforeEach
    void setUp() throws Exception {
        senderClient = mock(ServiceBusSenderClient.class);
        Field senderClientField = DetractorProducer.class.getDeclaredField("senderClient");
        senderClientField.setAccessible(true);
        senderClientField.set(this.detractorProducer, this.senderClient);
    }

    @Test
    void testSendMessageSuccessfully() throws JsonProcessingException {
        // Given
        int score = 5;
        String comment = "Poor service";
        FeedbackRequestDto requestDto = new FeedbackRequestDto(score, comment);
        String expectedJson = new ObjectMapper().writeValueAsString(requestDto);

        // When
        this.detractorProducer.sendMessage(requestDto);

        // Then
        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(this.senderClient).sendMessage(messageCaptor.capture());
        ServiceBusMessage capturedMessage = messageCaptor.getValue();
        assertEquals(expectedJson, capturedMessage.getBody().toString());
    }
}