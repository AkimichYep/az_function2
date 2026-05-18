# Configuration Instructions

## ✅ Updated: local.settings.json

Your `local.settings.json` has been updated with PostgreSQL connection settings.

---

## 🔐 Required Changes (Before Running)

### 1. PostgreSQL Password
```json
"PostgreSQLConnection": "jdbc:postgresql://c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com:5432/postgres?user=citus&password=YOUR_PASSWORD_HERE&sslmode=require"
                                                                                                                              ↑
                                                                                                                Replace with actual password
```

**Where to get it:**
1. Go to Azure Portal
2. Navigate to your `az-cosmos-db` resource (Cosmos DB for PostgreSQL)
3. Settings → Connection Strings
4. Copy the password from the JDBC connection string

**Example after update:**
```json
"PostgreSQLConnection": "jdbc:postgresql://c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com:5432/postgres?user=citus&password=MySecurePassword123!&sslmode=require"
```

---

### 2. Azure Storage Connection (Optional but Recommended)

```json
"AzureWebJobsStorage": "DefaultEndpointsProtocol=https;AccountName=<storage-account-name>;AccountKey=<storage-account-key>;EndpointSuffix=core.windows.net"
                                                                      ↑                        ↑
                                                        Replace with your storage details
```

**Where to get it:**
1. Go to Azure Portal
2. Find your Storage Account (or create one in the same resource group)
3. Settings → Access Keys
4. Copy the "Connection string" or build it from Account name and Account key

**Or leave empty for local testing** (it will still work for PostgreSQL trigger)

---

## 📋 Configuration Checklist

- [ ] Updated PostgreSQL password
- [ ] (Optional) Updated Storage Account connection
- [ ] Saved the file
- [ ] Ready to run!

---

## 🚀 Quick Start After Configuration

```powershell
# Build and run
mvn clean install
mvn azure-functions:run

# Expected output:
# PostgreSQL polling trigger executed.
# Table 'example_data' ready.
# No recent changes detected.
```

---

## ✨ Connection String Breakdown

```
jdbc:postgresql://HOST:PORT/DATABASE?user=USER&password=PASSWORD&sslmode=SSL
        ↓              ↓     ↓    ↓        ↓    ↓          ↓
     Protocol       FQDN     Port   DB     User Password  Force SSL

Your Resource Details:
├─ Host: c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com
├─ Port: 5432 (PostgreSQL default)
├─ Database: postgres
├─ User: citus (default admin user)
├─ Password: [FILL IN - from Azure Portal]
└─ SSL: require (secure connection)
```

---

## 🔍 Verify Connection Works

After updating the password, you can test:

```bash
# Using psql command line
psql -h c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com \
     -U citus \
     -d postgres \
     -c "SELECT NOW();"

# If successful, you'll see current timestamp
```

Or just run the function:

```powershell
mvn azure-functions:run
# If connection works, you'll see:
# "Table 'example_data' ready."
```

---

## 🛡️ Security Note

- **local.settings.json** is already in `.gitignore` (won't be committed)
- Keep your password confidential
- In Azure deployment, use Key Vault for secrets (not shown here)

---

## 📝 Current State

```
✅ PostgreSQL connection configured with:
   - Correct host/port/database/user
   - Password placeholder (needs your actual password)
   - SSL enabled for security

✅ Local testing ready after password update

✅ All 9 triggers available:
   - HTTP, Timer, Queue, Blob, Service Bus
   - Event Hub, Event Grid, Cosmos DB
   - PostgreSQL Polling (NEW!)
```

---

**Next Step:** Add your PostgreSQL password and run `mvn azure-functions:run` 🚀

