# 🎓 Azure Functions Triggers - Complete Learning Guide

Your project now demonstrates **7 different Azure Functions triggers**. Here's what each does:

---

## 📊 All Triggers at a Glance

| Trigger | Filename | What Triggers It | Complexity | Learns |
|---------|----------|------------------|-----------|--------|
| **HTTP** | `HttpTriggerJava.java` | HTTP GET/POST request | ⭐ Easy | Request/response handling |
| **Timer** | `TimerTriggerJava.java` | CRON schedule | ⭐ Easy | Scheduling patterns |
| **PostgreSQL Poll** | `PostgreSQLTriggerJava.java` | Timer + Database query | ⭐⭐ Medium | Database integration |
| **Blob Storage** | `BlobTriggerJava.java` | File uploaded to blob | ⭐⭐ Medium | Cloud storage events |
| **Queue** | `QueueTriggerJava.java` | Message added to queue | ⭐⭐ Medium | Async messaging |
| **Service Bus** | `ServiceBusTriggerJava.java` | Message on Service Bus | ⭐⭐⭐ Hard | Enterprise messaging |
| **Event Hubs** | `EventHubTriggerJava.java` | Event arrives on hub | ⭐⭐⭐ Hard | Real-time telemetry |
| **Event Grid** | `EventGridTriggerJava.java` | Event Grid event fires | ⭐⭐⭐ Hard | Event routing |
| **Cosmos DB** | `CosmosDBTriggerJava.java` | Document changes (NoSQL) | ⭐⭐⭐ Hard | Change feed patterns |

---

## 🚀 Recommended Learning Path

### Phase 1: Basics (No Azure Services Needed)
```
1. HttpTriggerJava     ← Test in browser/Postman immediately
2. TimerTriggerJava    ← Observe scheduled execution
```

**Run locally:**
```powershell
mvn azure-functions:run
```

---

### Phase 2: Cloud Storage & Messaging (Need Azure Resources)
```
3. PostgreSQLTriggerJava  ← Polling pattern (NEW!)
4. QueueTriggerJava       ← Async messaging pattern
5. BlobTriggerJava        ← File storage events
```

**Requires:** Storage account + local.settings.json config

---

### Phase 3: Advanced Messaging (Enterprise Patterns)
```
6. ServiceBusTriggerJava  ← Topics, queues, sessions
7. EventHubTriggerJava    ← High-throughput telemetry
8. EventGridTriggerJava   ← Event-driven architecture
```

**Requires:** Service Bus, Event Hubs, Event Grid resources

---

### Phase 4: Database Triggers (Change Feeds)
```
9. CosmosDBTriggerJava    ← NoSQL change feeds (use for learning)
```

**Note:** Your Cosmos DB for PostgreSQL uses polling (Phase 2) instead

---

## 🎯 What To Learn From Each

### 1️⃣ HTTP Trigger - Entry Point
```java
@HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST})
HttpRequestMessage<Optional<String>> request
```
Learn: Reading request body, query params, returning HTTP responses

### 2️⃣ Timer Trigger - Scheduling
```java
@TimerTrigger(name = "timerInfo", schedule = "0 */5 * * * *") String timerInfo
```
Learn: CRON expressions, periodic tasks, background jobs

### 3️⃣ PostgreSQL Poll - Database Integration (NEW!)
```java
Connection conn = DriverManager.getConnection(connectionString);
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery(query);
```
Learn: JDBC connections, polling patterns, data retrieval

### 4️⃣ Queue Trigger - Async Processing
```java
@QueueTrigger(name = "message", queueName = "myqueue", connection = "AzureWebJobsStorage")
String queueMessage
```
Learn: Message-driven architecture, decoupling services

### 5️⃣ Blob Trigger - Event-Driven Files
```java
@BlobTrigger(name = "file", path = "samples/{name}", dataType = "binary")
byte[] content
```
Learn: Cloud event handling, file processing

### 6️⃣ Service Bus - Enterprise Messaging
```java
@ServiceBusTrigger(name = "message", queueName = "myqueue", connection = "ServiceBusConnection")
String message
```
Learn: AMQP protocol, topics, subscriptions, sessions

### 7️⃣ Event Hubs - High-Throughput Telemetry
```java
@EventHubTrigger(name = "message", eventHubName = "myhub", connection = "EventHubConnection")
List<String> messages
```
Learn: Batch processing, consumer groups, checkpointing

### 8️⃣ Event Grid - Event Routing
```java
@EventGridTrigger(name = "eventGridEvent")
EventGridEvent[] events
```
Learn: Event subscriptions, filtering, multi-source events

### 9️⃣ Cosmos DB - Change Feeds
```java
@CosmosDBTrigger(name = "documents", databaseName = "mydb", 
                 containerName = "mycontainer", connection = "AzureCosmosDBConnection")
String[] documents
```
Learn: NoSQL change feeds, denormalization patterns

---

## 📁 Project Structure

```
azure-function-examples/
├── src/main/java/org/example/functions/
│   ├── HttpTriggerJava.java
│   ├── TimerTriggerJava.java
│   ├── PostgreSQLTriggerJava.java        ← NEW!
│   ├── QueueTriggerJava.java
│   ├── BlobTriggerJava.java
│   ├── ServiceBusTriggerJava.java
│   ├── EventHubTriggerJava.java
│   ├── EventGridTriggerJava.java
│   └── CosmosDBTriggerJava.java
│
├── pom.xml                               ← UPDATED (PostgreSQL driver added)
├── local.settings.json                   ← Configure here
├── SETUP_QUICK_START.md                 ← START HERE (NEW!)
└── POSTGRESQL_TRIGGER_GUIDE.md          ← Detailed setup (NEW!)
```

---

## ⚙️ Configuration Template

### `local.settings.json` - Full Template

```json
{
  "IsEncrypted": false,
  "Values": {
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "AzureWebJobsStorage": "DefaultEndpointsProtocol=https;AccountName=<name>;AccountKey=<key>;EndpointSuffix=core.windows.net",
    "PostgreSQLConnection": "jdbc:postgresql://c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com:5432/postgres?user=citus&password=<PASSWORD>&sslmode=require",
    "ServiceBusConnection": "Endpoint=sb://<name>.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=<key>",
    "EventHubConnection": "Endpoint=sb://<name>.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=<key>",
    "AzureCosmosDBConnection": "AccountEndpoint=https://<account>.documents.azure.com:443/;AccountKey=<key>"
  }
}
```

---

## 🔄 Execution Flow Examples

### HTTP Trigger Flow
```
Client
  ↓ (HTTP GET/POST)
HttpTrigger
  ↓ (receives request)
Your Java code
  ↓ (processes)
Response
  ↓ (returns to client)
✓ Done
```

### Timer Trigger Flow
```
Scheduler (every 5 min)
  ↓
TimerTrigger fires
  ↓
Your Java code
  ↓ (e.g., query database)
Process results
  ↓
Log/Send data
  ↓
Wait 5 minutes
  ↓ (repeat)
```

### Queue Trigger Flow
```
Service
  ↓ (enqueue message)
Azure Queue Storage
  ↓
QueueTrigger detects new message
  ↓
Your Java code
  ↓ (processes message)
Complete/Dead-letter
  ↓
✓ Message removed from queue
```

### Blob Trigger Flow
```
Upload file to blob
  ↓
Azure Blob Storage
  ↓
BlobTrigger detects change
  ↓
Your Java code
  ↓ (reads file content)
Process/Store data
  ↓
✓ Blob processing complete
```

---

## 💡 Pro Tips

### Tip 1: Combine Triggers
Start with HTTP to insert data, then use Timer to process it:
```
HTTP Trigger (insert test data)
  ↓
Timer Trigger (processes every 5 min)
  ↓
Blob Trigger (stores results)
```

### Tip 2: Use Dependency Injection
All triggers can use:
- `ExecutionContext` - logging, invocation ID
- `Logger` - custom logging
- `Optional<T>` - handle missing bindings

### Tip 3: Local Testing Strategy
1. Start with HTTP (test immediately in browser)
2. Add Timer (watch logs for scheduled execution)
3. Add Database (insert test data, watch polling)
4. Scale to cloud services

### Tip 4: Deployment Ready
Once learned locally:
```powershell
mvn azure-functions:deploy
# Your function will run in Azure cloud automatically
```

---

## 📖 Recommended Reading Before Each Phase

**Phase 1:** No prerequisites (HTTP/Timer just work)

**Phase 2:** Read before PostgreSQL
- JDBC basics: https://www.oracle.com/java/technologies/getstarted/overview/

**Phase 3:** Read before messaging
- Azure Queue Storage: https://learn.microsoft.com/en-us/azure/storage/queues/
- Azure Service Bus: https://learn.microsoft.com/en-us/azure/service-bus-messaging/

**Phase 4:** Read before Event Hubs
- Event Hubs concepts: https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-about

---

## ✨ Quick Wins

Try these in order for immediate success:

1. **First 5 min**: Run `mvn azure-functions:run`, test HTTP trigger in browser
2. **Next 15 min**: See Timer trigger execute
3. **Next 30 min**: Configure PostgreSQL, watch polling trigger query data
4. **Next hour**: Insert test data, see trigger detect it
5. **Next session**: Deploy to Azure, see it run in cloud

---

## 🆘 Quick Troubleshooting

| Issue | Solution |
|-------|----------|
| `mvn: command not found` | Install Maven: https://maven.apache.org/ |
| `No main class found` | Run `mvn clean install` first |
| Function doesn't trigger | Check connection string in `local.settings.json` |
| PostgreSQL connection fails | Verify IP whitelisted in firewall + correct password |
| Port 5432 blocked locally | Use Azure Portal Query Editor instead |
| Out of sync with tutorial | Run `mvn clean install` to rebuild |

---

## 🎓 You Now Know

✅ HTTP triggers (request/response)
✅ Timer triggers (scheduling)
✅ Database polling patterns
✅ Queue-based async messaging
✅ Blob storage events
✅ Event-driven architecture concepts

Next: Pick one, learn it deeply, deploy to Azure! 🚀

---

**Last updated:** May 2026  
**Active functions:** 9 trigger types  
**Status:** ✅ All locally testable

