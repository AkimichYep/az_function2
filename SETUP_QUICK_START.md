# Complete Setup: PostgreSQL Trigger Learning

## 📋 What You Now Have

Your project now includes:

| File | Type | Purpose |
|------|------|---------|
| `PostgreSQLTriggerJava.java` | **NEW** | Timer trigger that polls PostgreSQL every 5 minutes |
| `pom.xml` | **UPDATED** | Added PostgreSQL JDBC driver dependency |
| `POSTGRESQL_TRIGGER_GUIDE.md` | **NEW** | Detailed setup guide |

---

## 🚀 Quick Start

### Step 1: Build the Project
```powershell
cd C:\Users\Maksym_Yepaneshnikov\azure-function-examples
mvn clean install
```

Expected output:
```
[INFO] Building jar: C:\...\target\classes\...
[INFO] BUILD SUCCESS
```

### Step 2: Set Up `local.settings.json`

Update your `local.settings.json` with your PostgreSQL connection:

```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "DefaultEndpointsProtocol=https;AccountName=...;AccountKey=...;EndpointSuffix=core.windows.net",
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "PostgreSQLConnection": "jdbc:postgresql://c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com:5432/postgres?user=citus&password=YOUR_PASSWORD&sslmode=require"
  }
}
```

**Get your password from Azure Portal:**
1. Go to your `az-cosmos-db` resource
2. Settings → Connection Strings
3. Copy the password from the JDBC connection string

### Step 3: Run Locally
```powershell
mvn azure-functions:run
```

Expected console output:
```
PostgreSQL polling trigger executed.
Table 'example_data' ready.
No recent changes detected.
```

The function will:
- ✅ Start automatically
- ✅ Run every 5 minutes (on schedule)
- ✅ Keep polling your PostgreSQL database
- ✅ Create the `example_data` table if it doesn't exist
- ✅ Log any changes found

---

## 🧪 Test It

### Test 1: Verify Connection
If you see this error:
```
Database error: Connection to c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com:5432 refused
```
- Check your password is correct
- Verify your IP is whitelisted in PostgreSQL Firewall (Azure Portal)

If you see:
```
Table 'example_data' ready.
No recent changes detected.
```
✅ **Success!** Connection works.

### Test 2: Insert Data While Running

Open a new terminal and connect to PostgreSQL:

**Option A: Using Portal (easiest)**
1. Azure Portal → `az-cosmos-db` → Query Editor
2. Run:
```sql
INSERT INTO public.example_data (name, description) 
VALUES ('My First Record', 'Testing the trigger');
```

**Option B: Using psql CLI**
```bash
psql -h c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com -U citus -d postgres -c "INSERT INTO public.example_data (name, description) VALUES ('My First Record', 'Testing the trigger');"
```

### Test 3: Watch the Magic ✨

Within 5 minutes, your function will log:
```
PostgreSQL polling trigger executed.
Table 'example_data' ready.
Change detected - ID: 1, Name: My First Record, Description: Testing the trigger, Updated: 2026-05-18 10:15:30.123456
Total changes detected: 1
```

🎉 **You just learned polling triggers!**

---

## 📚 Understanding the Code

### How `PostgreSQLTriggerJava` Works

```java
// 1. Timer triggers every 5 minutes
@TimerTrigger(name = "timerInfo", schedule = "0 */5 * * * *")

// 2. Get PostgreSQL connection from environment variable
String connectionString = System.getenv("PostgreSQLConnection");

// 3. Auto-create tracking table (first run only)
CREATE TABLE IF NOT EXISTS public.example_data (...)

// 4. Query for recent changes
SELECT * FROM public.example_data WHERE updated_at > NOW() - INTERVAL '5 minutes'

// 5. Log each change
context.getLogger().info("Change detected - ID: ..., Name: ...")
```

### CRON Schedule Breakdown
```
"0 */5 * * * *"
 ↓  ↓   ↓ ↓ ↓ ↓
 │  │   │ │ │ └─ Day of week (0-6, 0=Sunday)
 │  │   │ │ └─── Month (1-12)
 │  │   │ └───── Day of month (1-31)
 │  │   └─────── Hour (0-23)
 │  └─────────── Minute (0-59) - every 5 min "*/ 5"
 └───────────── Second (0-59)

= Run at: 00:00, 00:05, 00:10, 00:15, ... every 5 minutes
```

---

## 🎯 Next Learning Steps

1. **✅ Current**: Polling trigger (Timer + Query) - You are here
2. **Next**: HTTP trigger that manually inserts data
3. **Then**: Combine them (HTTP endpoint + polling trigger)
4. **Advanced**: Change to other trigger types (Blob, Queue, Service Bus)

---

## 🆘 Troubleshooting

### Connection Fails
```
Connection refused
```
→ Check password, firewall, and that PostgreSQL is running

### No changes ever logged
```
No recent changes detected. (every 5 minutes)
```
→ Try inserting data manually (see Test 2 above)

### Function doesn't start
```
ERROR: Cannot find class PostgreSQLTriggerJava
```
→ Run `mvn clean install` to recompile with new JDBC dependency

### Port 5432 blocked
→ Use Azure Query Editor in Portal instead of local psql

---

## 📝 Database Created

The function automatically creates this table on first run:

```sql
CREATE TABLE public.example_data (
    id SERIAL PRIMARY KEY,              -- Auto-incrementing ID
    name VARCHAR(255) NOT NULL,         -- Name field
    description TEXT,                   -- Long text
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- Auto-set on insert
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP   -- Auto-set on update
);
```

**Note**: `updated_at` is set to `CURRENT_TIMESTAMP`, but PostgreSQL won't auto-update it. To test updates, manually run:
```sql
UPDATE public.example_data SET name = 'Updated' WHERE id = 1;
```

---

## 🌐 Deployment to Azure (After Learning Locally)

Once comfortable locally:

```powershell
# Authenticate to Azure
az login

# Create resource group
az group create --name rg_az_func --location westeurope

# Deploy function app
mvn azure-functions:deploy
```

Your function will then:
- Run every 5 minutes in the cloud
- Connect to your PostgreSQL database
- Send logs to Application Insights

---

## 📞 Key Resources

- **Azure Cosmos DB for PostgreSQL**: https://learn.microsoft.com/en-us/azure/cosmos-db/postgresql/
- **Azure Functions Timer Triggers**: https://learn.microsoft.com/en-us/azure/azure-functions/functions-bindings-timer
- **PostgreSQL JDBC**: https://jdbc.postgresql.org/
- **CRON Expression Format**: https://en.wikipedia.org/wiki/Cron

Happy learning! 🚀

