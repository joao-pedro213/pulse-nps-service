package org.pulse.npsservice.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.pulse.npsservice.service.DetractorConsumer;

@ApplicationScoped
public class DetractorNotificationFunction {

    private DetractorConsumer detractorConsumer;

    @Inject
    public DetractorNotificationFunction(DetractorConsumer detractorConsumer) {
        this.detractorConsumer = detractorConsumer;
    }

    @FunctionName("detractor-notification")
    public void notify(
            @ServiceBusQueueTrigger(name = "message", queueName = "detractors", connection = "AzureWebJobsServiceBus")
            String message,
            final ExecutionContext context) {
        this.detractorConsumer.processMessage(message);
    }
}
