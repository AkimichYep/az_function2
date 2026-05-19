package org.example.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Blob trigger.
 * Triggered when a blob is created or updated in the specified container.
 *
 * Prerequisites:
 *   - Azure Storage Account
 *   - Set "AzureWebJobsStorage" connection string in local.settings.json
 *   - Create a container named "mycontainer" (or change the path below)
 *
 * Blob path pattern:
 *   "mycontainer/{name}"  -> triggers on any blob in 'mycontainer'
 *   "images/{name}.png"   -> triggers only on .png files in 'images'
 */
public class BlobTriggerJava {

    @FunctionName("BlobTriggerJava")
    @StorageAccount("AzureWebJobsStorage")
    public void run(
            @BlobTrigger(name = "content", path = "mycontainer/{name}", dataType = "binary") byte[] content,
            @BindingName("name") String name,
            final ExecutionContext context) {

        context.getLogger().info("Java Blob trigger function executed.");
        context.getLogger().info("Blob name: " + name);
        context.getLogger().info("Blob size: " + content.length + " bytes");
    }
}

