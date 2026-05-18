package org.example.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Service Bus Queue trigger.
 * Triggered when a message arrives on an Azure Service Bus queue.
 *
 * Prerequisites:
 *   - Azure Service Bus namespace with a queue named "myqueue"
 *   - Set "AzureServiceBusConnection" in local.settings.json with your connection string
 *
 * local.settings.json example:
 * {
 *   "Values": {
 *     "AzureServiceBusConnection": "Endpoint=sb://<namespace>.servicebus.windows.net/;SharedAccessKeyName=...;SharedAccessKey=..."
 *   }
 * }
 *
 * To also listen on a Topic subscription, change:
 *   @ServiceBusQueueTrigger -> @ServiceBusTopicTrigger(topicName="mytopic", subscriptionName="mysub", ...)
 */
public class ServiceBusTriggerJava {

    @FunctionName("ServiceBusTriggerJava")
    public void run(
            @ServiceBusQueueTrigger(
                name = "message",
                queueName = "myqueue",
                connection = "AzureServiceBusConnection"
            ) String message,
            final ExecutionContext context) {

        context.getLogger().info("Java Service Bus Queue trigger function executed.");
        context.getLogger().info("Message received: " + message);
    }
}

