package org.example.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import java.sql.*;

/**
 * Azure Functions with PostgreSQL polling trigger (Timer-based).
 * Demonstrates how to query Cosmos DB for PostgreSQL periodically to detect changes.
 *
 * Prerequisites:
 *   - Azure Cosmos DB for PostgreSQL (formerly Hyperscale) with FQDN: c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com
 *   - Default user: citus
 *   - Set "PostgreSQLConnection" in local.settings.json
 *   - PostgreSQL JDBC driver (already in pom.xml)
 *
 * local.settings.json example:
 * {
 *   "Values": {
 *     "PostgreSQLConnection": "jdbc:postgresql://c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com:5432/postgres?user=citus&password=<password>&sslmode=require"
 *   }
 * }
 *
 * How it works: Runs every 5 minutes, connects to PostgreSQL, creates/checks example_data table, and queries for recent changes.
 */
public class PostgreSQLTriggerJava {

    @FunctionName("PostgreSQLTriggerJava")
    public void run(
            @TimerTrigger(name = "timerInfo", schedule = "0 */5 * * * *") String timerInfo,
            final ExecutionContext context) {

        context.getLogger().info("PostgreSQL polling trigger executed.");

        // Get connection string from environment variables
        String connectionString = System.getenv("PostgreSQLConnection");
        if (connectionString == null || connectionString.isEmpty()) {
            context.getLogger().warning("PostgreSQLConnection environment variable not set!");
            return;
        }

        try {
            Connection conn = DriverManager.getConnection(connectionString);

            // Create example table if it doesn't exist
            createExampleTable(conn, context);

            // Poll for recent changes
            queryRecentChanges(conn, context);

            conn.close();

        } catch (SQLException e) {
            context.getLogger().severe("Database error: " + e.getMessage());
        }
    }

    /**
     * Creates an example table with a timestamp column for tracking changes.
     */
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

    /**
     * Queries the table for recent changes (last 5 minutes).
     */
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
}

