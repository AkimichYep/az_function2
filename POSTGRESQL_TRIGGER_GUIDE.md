# PostgreSQL Trigger Setup Guide

## What You Have
- **Resource**: `az-cosmos-db` (Cosmos DB for PostgreSQL)
- **Location**: westeurope
- **Coordinator**: `c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com`
- **Default User**: `citus`
- **Version**: PostgreSQL 16 + Citus 12.1

## New Code
`PostgreSQLTriggerJava.java` - A **Timer Trigger** that:
1. Runs every 5 minutes (CRON: `0 */5 * * * *`)
2. Connects to your PostgreSQL instance
3. Polls a table for recent changes
4. Logs the results

## How It Works
```
Timer Trigger (every 5 min)
    ↓
Create/Check "example_data" table
    ↓
Query for rows updated in last 5 minutes
    ↓
Log changes found
    ↓
Wait 5 minutes...
```

## Setup Steps

### 1. Get Your Connection String
From Azure Portal:
1. Go to your `az-cosmos-db` resource
2. Settings → Connection Strings
3. Copy the **JDBC Connection String** (looks like):
   ```
   jdbc:postgresql://c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com:5432/postgres?user=citus&password=<PASSWORD>&sslmode=require
   ```

### 2. Update `local.settings.json`
```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "DefaultEndpointsProtocol=https;...",
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "PostgreSQLConnection": "jdbc:postgresql://c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com:5432/postgres?user=citus&password=<YOUR_PASSWORD>&sslmode=require"
  }
}
```

### 3. Build & Run
```powershell
# Install dependencies
mvn clean install

# Run locally
mvn azure-functions:run
```

Watch the console for output like:
```
PostgreSQL polling trigger executed at: 2026-05-18T10:15:00Z
Table 'example_data' ready.
No recent changes detected.
```

### 4. Test It - Insert Data
While the function is running, insert test data:

**Option A: Using Azure Portal Query Editor**
1. Go to `az-cosmos-db` → Query Editor
2. Run:
   ```sql
   INSERT INTO public.example_data (name, description) 
   VALUES ('First Record', 'This is a test');
   ```

**Option B: Using psql command line**
```bash
psql -h c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com -U citus -d postgres
# Enter password when prompted
# Then:
INSERT INTO public.example_data (name, description) VALUES ('Test', 'Learning triggers');
```

### 5. Watch the Magic ✨
The function should detect your insert within 5 minutes and log:
```
Change detected - ID: 1, Name: Test, Description: Learning triggers, Updated: 2026-05-18 10:15:30
Total changes detected: 1
```

## Learning Path

| Trigger | Filename | Difficulty | Learns |
|---------|----------|-----------|--------|
| Timer | `PostgreSQLTriggerJava.java` | ⭐ Easy | Polling pattern, JDBC, scheduling |
| HTTP | `HttpTriggerJava.java` | ⭐ Easy | Request/response, testing |
| Blob | `BlobTriggerJava.java` | ⭐⭐ Medium | Cloud storage events |
| Queue | `QueueTriggerJava.java` | ⭐⭐ Medium | Async messaging |
| Service Bus | `ServiceBusTriggerJava.java` | ⭐⭐⭐ Hard | Enterprise messaging |

## Database Schema Created
The function auto-creates:
```sql
CREATE TABLE public.example_data (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Troubleshooting

**Connection timeout?**
- Verify sslmode=require in connection string
- Check if your IP is whitelisted in PostgreSQL Firewall (Azure Portal)
- Ensure password is correct

**No changes detected?**
- Manually verify data exists: Query → `SELECT * FROM public.example_data;`
- Check timestamps with: `SELECT updated_at FROM public.example_data;`

**JDBC driver not found?**
- Run: `mvn clean install` to download postgresql driver
- Check `.m2` folder for `postgresql-42.7.1.jar`

## Next Steps
1. ✅ Get this running locally
2. Deploy to Azure Functions
3. Add your own tables and queries
4. Combine with other triggers (HTTP + Timer for hybrid patterns)

