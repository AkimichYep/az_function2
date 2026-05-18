# PostgreSQL Polling Trigger - Code Pattern & Examples

## The PostgreSQL Trigger Pattern

Your new `PostgreSQLTriggerJava` demonstrates the **polling pattern** - ideal for databases that don't have native change feed triggers (like PostgreSQL).

---

## 🔍 Code Breakdown

### Part 1: Timer Configuration
```java
@TimerTrigger(name = "timerInfo", schedule = "0 */5 * * * *") String timerInfo
```
- Runs every 5 minutes
- Schedule: `0 */5 * * * *` means `:00`, `:05`, `:10`, `:15`, etc.

### Part 2: Get Connection String
```java
String connectionString = System.getenv("PostgreSQLConnection");
if (connectionString == null || connectionString.isEmpty()) {
    context.getLogger().warning("PostgreSQLConnection environment variable not set!");
    return;
}
```
- Read from `local.settings.json`
- Connection string format:
  ```
  jdbc:postgresql://HOST:5432/DATABASE?user=USER&password=PASSWORD&sslmode=require
  ```

### Part 3: Connect to Database
```java
Connection conn = DriverManager.getConnection(connectionString);
createExampleTable(conn, context);
queryRecentChanges(conn, context);
conn.close();
```

### Part 4: Auto-Create Tracking Table
```java
private void createExampleTable(Connection conn, ExecutionContext context) throws SQLException {
    String createTableSQL = "CREATE TABLE IF NOT EXISTS public.example_data (" +
            "id SERIAL PRIMARY KEY," +
            "name VARCHAR(255) NOT NULL," +
            "description TEXT," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ");";

    Statement stmt = conn.createStatement();
    stmt.execute(createTableSQL);
    stmt.close();
    context.getLogger().info("Table 'example_data' ready.");
}
```

### Part 5: Query for Recent Changes
```java
private void queryRecentChanges(Connection conn, ExecutionContext context) throws SQLException {
    String query = "SELECT id, name, description, updated_at " +
            "FROM public.example_data " +
            "WHERE updated_at > NOW() - INTERVAL '5 minutes' " +
            "ORDER BY updated_at DESC;";

    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(query);

    int changeCount = 0;
    while (rs.next()) {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        context.getLogger().info(String.format(
                "Change detected - ID: %d, Name: %s, Description: %s, Updated: %s",
                id, name, description, updatedAt));
        changeCount++;
    }

    rs.close();
    stmt.close();

    if (changeCount == 0) {
        context.getLogger().info("No recent changes detected.");
    } else {
        context.getLogger().info(String.format("Total changes detected: %d", changeCount));
    }
}
```

---

## 📚 Modifying for Your Needs

### Example 1: Track Only New Orders

**Goal:** Every 5 minutes, find orders placed in the last 5 minutes

```java
private void queryRecentOrders(Connection conn, ExecutionContext context) throws SQLException {
    String query = "SELECT order_id, customer_name, total, created_at " +
            "FROM public.orders " +
            "WHERE created_at > NOW() - INTERVAL '5 minutes' " +
            "AND status = 'PENDING'" +
            "ORDER BY created_at DESC;";

    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(query);

    while (rs.next()) {
        int orderId = rs.getInt("order_id");
        String customer = rs.getString("customer_name");
        double total = rs.getDouble("total");
        Timestamp createdAt = rs.getTimestamp("created_at");

        context.getLogger().info(String.format(
                "New Order - ID: %d, Customer: %s, Total: $%.2f, Time: %s",
                orderId, customer, total, createdAt));

        // Maybe send to queue or call API
        // sendToServiceBus(orderId);
    }

    rs.close();
    stmt.close();
}
```

### Example 2: Batch Updates

**Goal:** Mark processed changes so we don't repeat them

```java
private void processAndMarkChanges(Connection conn, ExecutionContext context) throws SQLException {
    // 1. Get unprocessed changes
    String selectQuery = "SELECT id, data FROM public.example_data WHERE processed = false;";
    Statement selectStmt = conn.createStatement();
    ResultSet rs = selectStmt.executeQuery(selectQuery);

    while (rs.next()) {
        int id = rs.getInt("id");
        String data = rs.getString("data");

        // 2. Process the change
        context.getLogger().info("Processing: " + data);

        // 3. Mark as processed
        String updateQuery = "UPDATE public.example_data SET processed = true WHERE id = " + id + ";";
        Statement updateStmt = conn.createStatement();
        updateStmt.execute(updateQuery);
        updateStmt.close();
    }

    rs.close();
    selectStmt.close();
}
```

### Example 3: Different Schedule (Every Hour)

```java
@FunctionName("HourlyPollingTrigger")
public void runHourly(
        @TimerTrigger(name = "timerInfo", schedule = "0 0 * * * *") String timerInfo,  // Every hour
        final ExecutionContext context) {
    // Same code but runs hourly
    // 0 0 * * * * = every hour at :00
}
```

### Example 4: Multiple Tables

```java
@FunctionName("MultiTablePollingTrigger")
public void run(
        @TimerTrigger(name = "timerInfo", schedule = "0 */5 * * * *") String timerInfo,
        final ExecutionContext context) {

    String connectionString = System.getenv("PostgreSQLConnection");
    try {
        Connection conn = DriverManager.getConnection(connectionString);

        // Check multiple tables
        checkOrdersTable(conn, context);
        checkUsersTable(conn, context);
        checkInvoicesTable(conn, context);

        conn.close();
    } catch (SQLException e) {
        context.getLogger().severe("Database error: " + e.getMessage());
    }
}

private void checkOrdersTable(Connection conn, ExecutionContext context) throws SQLException {
    // Query orders table
}

private void checkUsersTable(Connection conn, ExecutionContext context) throws SQLException {
    // Query users table
}

private void checkInvoicesTable(Connection conn, ExecutionContext context) throws SQLException {
    // Query invoices table
}
```

---

## 🔄 Integration Patterns

### Pattern 1: Poll → Queue → Process

```
Timer Trigger (polls every 5 min)
  ↓ (finds new data)
Put message in Azure Queue
  ↓
Queue Trigger (processes message)
  ↓
Mark row as processed in database
```

**Code:**
```java
// In PostgreSQL trigger
// After finding changes, queue them:
sendToQueue(changeId);  // Send to Azure Queue

// Then in Queue trigger (different function):
// it will process the queued items
```

### Pattern 2: Poll → Send Email

```
Timer Trigger (polls every 5 min)
  ↓ (finds new data)
Call email API
  ↓
Log result
```

### Pattern 3: Poll → Update Cosmos DB

```
Timer Trigger (polls PostgreSQL every 5 min)
  ↓ (finds new data)
Write to Cosmos DB (NoSQL)
  ↓
Mirror/analytics database
```

---

## 🧪 Test Queries

### Insert Test Data
```sql
INSERT INTO public.example_data (name, description) 
VALUES ('Test Item', 'This was inserted at ' || NOW());
```

### View All Data
```sql
SELECT * FROM public.example_data;
```

### View Recent Changes (Last 5 Min)
```sql
SELECT id, name, description, updated_at 
FROM public.example_data 
WHERE updated_at > NOW() - INTERVAL '5 minutes' 
ORDER BY updated_at DESC;
```

### Update Data (to trigger detection)
```sql
UPDATE public.example_data 
SET description = 'Updated: ' || NOW()::text 
WHERE id = 1;
```

### Delete Old Records (cleanup)
```sql
DELETE FROM public.example_data 
WHERE created_at < NOW() - INTERVAL '30 days';
```

---

## ⚡ Performance Tips

### Tip 1: Add an Index for Speed
```sql
CREATE INDEX idx_example_data_updated_at 
ON public.example_data(updated_at);
```
This makes the query much faster on large tables.

### Tip 2: Archive Old Data
```sql
-- Keep only last 7 days
DELETE FROM public.example_data 
WHERE created_at < NOW() - INTERVAL '7 days';
```

### Tip 3: Use Connection Pooling (Advanced)
Instead of creating new connection each time:
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>
```

### Tip 4: Adjust Schedule Based on Load
```
High-frequency updates? → "0 * * * * *"     (every minute)
Medium frequency?       → "0 */5 * * * *"    (every 5 min)
Low frequency?          → "0 0 * * * *"      (every hour)
```

---

## 🛠️ Debugging

### Enable SQL Logging
```java
// Add to your connection code:
context.getLogger().info("Query: " + query);
context.getLogger().info("Found " + changeCount + " changes");
```

### Test Connection Separately
```powershell
# Use psql to verify connection works
psql -h c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com \
     -U citus \
     -d postgres \
     -c "SELECT NOW();"
```

### Monitor Log Stream
```powershell
# If deployed to Azure
az functionapp log tail --name <function-name> --resource-group <rg-name>
```

---

## 📋 Checklist: From Template to Production

- [ ] PostgreSQL connection string works locally
- [ ] Function creates table automatically
- [ ] Insert test data, watch function detect it
- [ ] Modify query for your actual table/columns
- [ ] Add error handling (try/catch)
- [ ] Test with high-volume data
- [ ] Add indexes to queries
- [ ] Deploy to Azure
- [ ] Monitor with Application Insights
- [ ] Set data retention policy (archive old records)

---

## 🚀 Next Steps

1. **Learn**: Run this locally and insert test data
2. **Modify**: Change query to your actual table
3. **Test**: Verify it detects all your changes
4. **Integrate**: Combine with other triggers (Queue, Blob)
5. **Deploy**: Run in Azure
6. **Monitor**: Check logs and performance
7. **Scale**: Add more tables or reduce polling interval

---

*Happy polling! 📊*

