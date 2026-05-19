package org.example.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Queue trigger.
 * Triggered when a message is added to an Azure Storage Queue.
 *
 * Prerequisites:
 *   - Azure Storage Account
 *   - Set "AzureWebJobsStorage" connection string in local.settings.json
 *   - Create a queue named "myqueue" (or change queueName below)
 *
 * To test locally:
 *   Use Azure Storage Explorer to add a message to the queue, or
 *   Use Azure CLI: az storage message put --queue-name myqueue --content "hello" --connection-string <conn>
 */
public class QueueTriggerJava {

    @FunctionName("QueueTriggerJava")
    @StorageAccount("AzureWebJobsStorage")
    public void run(
            @QueueTrigger(name = "message", queueName = "myqueue") String message,
            final ExecutionContext context) {

        context.getLogger().info("Java Queue trigger function executed.");
        context.getLogger().info("Queue message received: " + message);
    }
}

