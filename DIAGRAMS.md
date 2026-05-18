# 🎨 Visual Architecture & Diagrams

## PostgreSQL Polling Trigger Architecture

```
┌─────────────────────────────────────────────────────────┐
│                  Azure Functions                         │
│  ┌───────────────────────────────────────────────────┐  │
│  │         PostgreSQLTriggerJava                     │  │
│  │  ┌──────────────────────────────────────────┐    │  │
│  │  │ Timer Trigger                            │    │  │
│  │  │ Schedule: 0 */5 * * * * (every 5 min)   │    │  │
│  │  └──────────────────────────────────────────┘    │  │
│  │                    ↓                              │  │
│  │  ┌──────────────────────────────────────────┐    │  │
│  │  │ Get Connection String                    │    │  │
│  │  │ From: local.settings.json                │    │  │
│  │  │ Env: PostgreSQLConnection               │    │  │
│  │  └──────────────────────────────────────────┘    │  │
│  │                    ↓                              │  │
│  │  ┌──────────────────────────────────────────┐    │  │
│  │  │ DriverManager.getConnection()             │    │  │
│  │  │ Opens JDBC Connection                    │    │  │
│  │  └──────────────────────────────────────────┘    │  │
│  │                    ↓                              │  │
│  │  ┌──────────────────────────────────────────┐    │  │
│  │  │ Create Table if Not Exists               │    │  │
│  │  │ Table: public.example_data               │    │  │
│  │  └──────────────────────────────────────────┘    │  │
│  │                    ↓                              │  │
│  │  ┌──────────────────────────────────────────┐    │  │
│  │  │ Query: SELECT * WHERE updated > NOW-5min│    │  │
│  │  │ Check for recent changes                 │    │  │
│  │  └──────────────────────────────────────────┘    │  │
│  │                    ↓                              │  │
│  │  ┌──────────────────────────────────────────┐    │  │
│  │  │ Log Results                              │    │  │
│  │  │ context.getLogger().info()               │    │  │
│  │  └──────────────────────────────────────────┘    │  │
│  │                    ↓                              │  │
│  │  ┌──────────────────────────────────────────┐    │  │
│  │  │ Close Connection                         │    │  │
│  │  │ Wait 5 minutes...                        │    │  │
│  │  └──────────────────────────────────────────┘    │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
         │
         │ JDBC Connection
         ↓
┌─────────────────────────────────────────────────────────┐
│   Azure Cosmos DB for PostgreSQL                        │
│   Server: c-az-cosmos-db                                │
│   Location: westeurope                                  │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Database: postgres                               │  │
│  │  ┌─────────────────────────────────────────────┐  │  │
│  │  │ Table: public.example_data                 │  │  │
│  │  │ Columns:                                   │  │  │
│  │  │  • id (Serial PK)                          │  │  │
│  │  │  • name (VARCHAR)                          │  │  │
│  │  │  • description (TEXT)                      │  │  │
│  │  │  • created_at (TIMESTAMP)                  │  │  │
│  │  │  • updated_at (TIMESTAMP)                  │  │  │
│  │  └─────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## Execution Timeline (5-Second Snapshots)

```
┌─────────────────────────────────────────────────────────────────┐
│ Time: 10:00:00                                                  │
│ Status: Timer fires (first execution)                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ⏱️  10:00:00  Timer triggered                                 │
│  🔌 10:00:01  Connected to PostgreSQL                          │
│  📋 10:00:02  Table created: public.example_data               │
│  🔍 10:00:03  Query: SELECT... WHERE updated > NOW()-5min      │
│  📊 10:00:04  Result: 0 rows                                   │
│  📝 10:00:05  Log: "No recent changes detected."                │
│  🔌 10:00:06  Connection closed                                │
│                                                                 │
│ ⏳ Wait 5 minutes...                                            │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│ Time: 10:03:00                                                  │
│ User inserts data:                                              │
│ INSERT INTO public.example_data (name, desc)                   │
│ VALUES ('Test', 'Learning')                                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│ ⏳ Wait 2 more minutes...                                       │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│ Time: 10:05:00                                                  │
│ Status: Second timer execution (NOW has the inserted row)       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ⏱️  10:05:00  Timer triggered                                 │
│  🔌 10:05:01  Connected to PostgreSQL                          │
│  📋 10:05:02  Table exists (skip creation)                     │
│  🔍 10:05:03  Query: SELECT... WHERE updated > NOW()-5min      │
│  📊 10:05:04  Result: 1 row found! 🎯                          │
│  📝 10:05:05  Log: "Change detected - ID: 1, Name: Test..."    │
│     10:05:06  Log: "Total changes detected: 1"                 │
│  🔌 10:05:07  Connection closed                                │
│                                                                 │
│ ✅ SUCCESS!                                                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Code Flow Diagram

```
PostgreSQLTriggerJava.run()
│
├─> Get PostgreSQLConnection from env
│   │
│   ├─> null/empty? → WARNING, return
│   │
│   └─> Have value ✓
│
├─> DriverManager.getConnection(connectionString)
│   │
│   ├─> Connection fails? → SEVERE log, catch SQLException  
│   │
│   └─> Connection OK ✓
│
├─> createExampleTable(conn, context)
│   │
│   ├─> "CREATE TABLE IF NOT EXISTS public.example_data"
│   │   ├─> id (SERIAL PRIMARY KEY)
│   │   ├─> name (VARCHAR)
│   │   ├─> description (TEXT)
│   │   ├─> created_at (TIMESTAMP DEFAULT NOW)
│   │   └─> updated_at (TIMESTAMP DEFAULT NOW)
│   │
│   ├─> Execute SQL
│   │
│   └─> Log: "Table 'example_data' ready."
│
├─> queryRecentChanges(conn, context)
│   │
│   ├─> SELECT id, name, description, updated_at
│   │        FROM public.example_data
│   │        WHERE updated_at > NOW() - '5 min'
│   │        ORDER BY updated_at DESC
│   │
│   ├─> Execute query
│   │
│   ├─> Loop through ResultSet
│   │   │
│   │   ├─> Get id, name, description, updated_at
│   │   │
│   │   ├─> Log: "Change detected - ID: X, Name: Y..."
│   │   │
│   │   └─> changeCount++
│   │
│   ├─> If changeCount == 0
│   │   └─> Log: "No recent changes detected."
│   │
│   └─> Else
│       └─> Log: "Total changes detected: N"
│
├─> conn.close()
│
└─> Done, wait 5 minutes for next timer trigger
```

---

## Integration Patterns

### Pattern 1: Poll → Queue → Process

```
PostgreSQL Polling Trigger        Queue Trigger           Next Function
(Every 5 min)                      (Real-time)             (On-demand)
┌──────────────┐                 ┌──────────────┐          ┌────────┐
│ Timer fires  │                 │ Message in   │          │Keep    │
│ Query DB     │─────select───→  │ Azure Queue  │─────msg──→Process │
│              │                 │              │          │ it     │
└──────────────┘                 └──────────────┘          └────────┘
                                                                │
                                                                ↓
                                                         Mark as processed
                                                         in database
```

### Pattern 2: Poll → Email Notification

```
PostgreSQL Polling          Azure Functions          External Service
┌──────────────┐                                    ┌──────────────┐
│ Timer fires  │                                    │ Email API    │
│ Query DB     │                                    │ (SendGrid,   │
│              │─ Find changes ──→                  │  Twilio...)  │
│ Build email  │                                    │              │
│              │─ Send email ────────────────────→  │              │
└──────────────┘                                    └──────────────┘
     Every 5 min                                       Async send
```

### Pattern 3: Poll → Mirror to Cosmos DB

```
PostgreSQL Polling                 Azure Functions              Cosmos DB (NoSQL)
┌──────────────┐                  ┌──────────────┐              ┌──────────────┐
│ Relational   │                  │ Transform    │              │ NoSQL Mirror │
│ Table        │                  │ data in      │              │              │
│              │─ Read rows ─→    │ function     │─ Insert ──→  │              │
│ example_data │                  │              │              │ documents    │
└──────────────┘                  └──────────────┘              └──────────────┘
    PostgreSQL                          Java                      Analytics DB
```

---

## Deployment Lifecycle

```
Development                    Testing                    Production
┌────────────┐                ┌────────────┐               ┌────────────┐
│ Local PC   │                │ Azure      │               │ Azure      │
│            │                │ Function   │               │ Functions  │
│ ┌────────┐ │                │ Premium    │               │ (scaled)   │
│ │PostgreS│ │                │            │               │            │
│ │QL Dev  │ │                │ ┌────────┐ │               │ ┌────────┐ │
│ └────────┘ │                │ │Test    │ │               │ │Prod    │ │
│            │                │ │Trigger │ │               │ │Trigger │ │
│ ┌────────┐ │ deploy ─────→  │ └────────┘ │ verify ────→  │ └────────┘ │
│ │Java    │ │ (mvn clean    │ ┌────────┐ │  (az           │ ┌────────┐ │
│ │Source  │ │  install      │ │App     │ │   functionapp │ │CI/CD   │ │
│ └────────┘ │  + az deploy) │ │Insights│ │   log)       │ │Deploy  │ │
│            │                │ └────────┘ │               │ └────────┘ │
│ ┌────────┐ │                │            │               │ ┌────────┐ │
│ │Maven   │ │                │            │               │ │Monitor │ │
│ │build   │ │                │            │               │ │Metrics │ │
│ └────────┘ │                │            │               │ └────────┘ │
└────────────┘                └────────────┘               └────────────┘
```

---

## Comparison: Polling vs Native Change Feeds

```
                    PostgreSQL (This Project)     Cosmos DB (NoSQL)
                    ┌──────────────────────┐      ┌─────────────────┐
Trigger Type        │ Polling (Timer)      │      │ Change Feed     │
                    │ ┌───────────────────┐│      │ ┌────────────┐  │
Frequency           │ │Every 5 minutes    ││      │ │Real-time   │  │
                    │ │(configurable)     ││      │ │(sub-second)│  │
                    │ └───────────────────┘│      │ └────────────┘  │
                    └──────────────────────┘      └─────────────────┘

Scalability         │ Good (SQL query)     │      │ Excellent       │
                    │ + index optimization │      │ (native feature)│

Binding Syntax      │ Timer trigger        │      │ @CosmosDBTrigger│
                    │ + manual query       │      │ (built-in)       │

Complexity          │ ⭐⭐⭐ Medium        │      │ ⭐ Easy         │
                    │ (JDBC, connections) │      │ (just binding)   │

Why We Use Polling  │ PostgreSQL doesn't   │      │ Would use if DB │
                    │ have native CDC      │      │ was Cosmos (NoSQL)
                    │ (Change Data Capture)│      │                 │
```

---

## CRON Schedule Visualization

```
"0 */5 * * * *"

  Position: 1  2  3  4  5  6
  ────────────────────────────
  Value:    0  */5 * * * *
            ↓  ↓   ↓ ↓ ↓ ↓
            │  │   │ │ │ └─ Day of week (0-6, 0=Sun)
            │  │   │ │ └─── Month (1-12)
            │  │   │ └───── Day of month (1-31)
            │  │   └─────── Hour (0-23)
            │  └─────────── Minute (0-59)
            └───────────── Second (0-59)

*/5 in minute = every 5 minutes

Example times it fires:
┌─────────────────────────────────┐
│ :00 (every hour)                │
│ :05                             │
│ :10                             │
│ :15                             │
│ :20                             │
│ :25                             │
│ :30                             │
│ :35                             │
│ :40                             │
│ :45                             │
│ :50                             │
│ :55                             │
└─────────────────────────────────┘
```

---

## Data Flow: From Insert to Detection

```
Step 1: User Action
┌─────────────────────────────────────┐
│ INSERT INTO example_data            │
│ (name, description)                 │
│ VALUES ('Test', 'Learning')        │
└─────────┬───────────────────────────┘
          │
          ↓ Immediately stored

Step 2: PostgreSQL State
┌────────────────────────────────────────────────┐
│ ID │ Name │ Description │ updated_at           │
├────┼──────┼─────────────┼──────────────────────┤
│ 1  │ Test │ Learning    │ 2026-05-18 10:03:45 │ ← Just inserted
└────────────────────────────────────────────────┘
          │
          ↓ Wait for next timer trigger

Step 3: Timer Trigger at 10:05:00
┌─────────────────────────────────────────────────────┐
│ Timer fires → Query:                                │
│ SELECT... FROM example_data                         │
│ WHERE updated_at > NOW() - INTERVAL '5 minutes'    │
│                                                     │
│ 10:05:00 - 5 min = 10:00:00                         │
│ 10:03:45 is AFTER 10:00:00 ✓ MATCH!                │
└─────────────────────────────────────────────────────┘
          │
          ↓

Step 4: Function Logs
┌──────────────────────────────────────────────────┐
│ Change detected - ID: 1, Name: Test,             │
│ Description: Learning,                           │
│ Updated: 2026-05-18 10:03:45                     │
│ Total changes detected: 1                        │
└──────────────────────────────────────────────────┘
```

---

## Connection String Breakdown

```
jdbc:postgresql://HOST:PORT/DATABASE?user=USER&password=PASS&sslmode=SETTING

        ↓               ↓     ↓    ↓        ↓    ↓          ↓
        
    Protocol        Server  Port  DB      User Password    SSL Config
    
Example:
┌────────────────────────────────────────────────────────────────────────┐
│ jdbc:postgresql://c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure  │
│                   ↑                                                     │
│                   Fully Qualified Domain Name (FQDN)                   │
│
│ :5432/postgres?user=citus&password=YOUR_PASSWORD&sslmode=require      │
│  ↓    ↓               ↓             ↓                    ↓             │
│ Port  DB Name      Username    Your Password      Force SSL encrypted │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Component Dependency Graph

```
        PostgreSQLTriggerJava.java
                    │
        ┌───────────┼───────────┐
        │           │           │
        ↓           ↓           ↓
    Timer Trigger  JDBC API   ExecutionContext
        │           │           │
        │      ┌────┴────┐      │
        │      │         │      │
        ↓      ↓         ↓      ↓
    Scheduler  Driver  Connection  Logger
              Manager  Pool
                │
                ↓
        PostgreSQL JDBC
        Driver (42.7.1)
                │
                ↓
        PostgreSQL Server
        (Cosmos DB for PostgreSQL)
```

---

*Diagrams created with ASCII art for clarity and simplicity* 📊

