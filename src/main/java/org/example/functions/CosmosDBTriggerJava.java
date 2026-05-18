package org.example.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Cosmos DB trigger.
 * Triggered when documents are inserted or updated in a Cosmos DB collection (via change feed).
 *
 * Prerequisites:
 *   - Azure Cosmos DB (NoSQL) account with database "mydb" and container "mycontainer"
 *   - A leases container named "leases" in the same database (Cosmos DB only, not PostgreSQL)
 *   - Set "AzureCosmosDBConnection" in local.settings.json with your Cosmos DB connection string
 *
 * IMPORTANT: This uses Azure Cosmos DB (NoSQL), NOT Cosmos DB for PostgreSQL.
 * The PostgreSQL variant requires different trigger mechanisms (polling or webhooks).
 *
 * local.settings.json example:
 * {
 *   "Values": {
 *     "AzureCosmosDBConnection": "AccountEndpoint=https://az-cosmos-db.documents.azure.com:443/;AccountKey=<primary-key>"
 *   }
 * }
 *
 * Note: The trigger fires with a batch of changed documents (List<String> as JSON strings).
 */
public class CosmosDBTriggerJava {

    @FunctionName("CosmosDBTriggerJava")
    public void run(
            @CosmosDBTrigger(
                name = "documents",
                databaseName = "mydb",
                containerName = "mycontainer",
                connection = "AzureCosmosDBConnection",
                leaseContainerName = "leases",
                createLeaseContainerIfNotExists = true
            ) String[] documents,
            final ExecutionContext context) {

        context.getLogger().info("Java Cosmos DB trigger function executed.");
        context.getLogger().info("Number of documents modified: " + documents.length);

        for (String document : documents) {
            context.getLogger().info("Document: " + document);
        }
    }
}

