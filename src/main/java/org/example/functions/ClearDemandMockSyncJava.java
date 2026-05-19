package org.example.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mock onboarding flow:
 * - reads product/price rows from an on-prem-like H2 database
 * - POSTs each row to a public API endpoint (mock ClearDemand API)
 * - waits for each response and returns a summary
 */
public class ClearDemandMockSyncJava {

    private static final String DEFAULT_H2_URL = "jdbc:h2:mem:onpremdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final String DEFAULT_H2_USER = "sa";
    private static final String DEFAULT_H2_PASSWORD = "";
    private static final String DEFAULT_API_URL = "https://httpbin.org/post";

    @FunctionName("ClearDemandMockSyncJava")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET, HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "cleardemand/sync-mock") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        final String h2Url = readEnv("OnPremH2JdbcUrl", DEFAULT_H2_URL);
        final String h2User = readEnv("OnPremH2User", DEFAULT_H2_USER);
        final String h2Password = readEnv("OnPremH2Password", DEFAULT_H2_PASSWORD);
        final String apiUrl = readEnv("MockClearDemandApiUrl", DEFAULT_API_URL);

        int maxRecords = parsePositiveInt(
                request.getQueryParameters().get("maxRecords"),
                parseBodyAsInt(request.getBody().orElse(null)),
                5,
                100);

        context.getLogger().info("Starting mock sync. maxRecords=" + maxRecords);

        try (Connection connection = DriverManager.getConnection(h2Url, h2User, h2Password)) {
            initializeSchemaAndSeed(connection);

            List<ProductPriceRow> rows = readRows(connection, maxRecords);
            if (rows.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.OK)
                        .body("No product/price rows found to sync.")
                        .build();
            }

            int successCount = 0;
            List<String> failures = new ArrayList<String>();
            List<String> transmissionDetails = new ArrayList<String>();

            for (ProductPriceRow row : rows) {
                String payload = toPayloadJson(row);
                ApiCallResult result = postJson(apiUrl, payload);

                transmissionDetails.add("sku=" + row.sku
                        + " | sent=" + payload
                        + " | receivedStatus=" + result.statusCode
                        + " | receivedBody=" + result.bodySnippet);

                if (result.statusCode >= 200 && result.statusCode < 300) {
                    successCount++;
                } else {
                    failures.add("sku=" + row.sku + " status=" + result.statusCode + " body=" + result.bodySnippet);
                }
            }

            StringBuilder summary = new StringBuilder();
            summary.append("Mock sync completed. total=").append(rows.size())
                    .append(", success=").append(successCount)
                    .append(", failed=").append(failures.size())
                    .append(". API=").append(apiUrl);

            if (!failures.isEmpty()) {
                summary.append(" Failures: ").append(String.join(" | ", failures));
            }

            summary.append(System.lineSeparator())
                    .append("Details:")
                    .append(System.lineSeparator())
                    .append(String.join(System.lineSeparator(), transmissionDetails));

            return request.createResponseBuilder(HttpStatus.OK)
                    .body(summary.toString())
                    .build();
        } catch (Exception ex) {
            context.getLogger().severe("Mock sync failed: " + ex.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Mock sync failed: " + ex.getMessage())
                    .build();
        }
    }

    static void initializeSchemaAndSeed(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS products (id INT PRIMARY KEY, sku VARCHAR(64) NOT NULL, name VARCHAR(255) NOT NULL)");
            statement.execute("CREATE TABLE IF NOT EXISTS prices (id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, product_id INT NOT NULL, amount DECIMAL(10,2) NOT NULL, currency VARCHAR(3) NOT NULL, updated_at TIMESTAMP NOT NULL)");
        }

        int productCount = countRows(connection, "products");
        if (productCount == 0) {
            try (PreparedStatement insertProducts = connection.prepareStatement("INSERT INTO products (id, sku, name) VALUES (?, ?, ?)")) {
                insertProducts.setInt(1, 1);
                insertProducts.setString(2, "MILK-1L");
                insertProducts.setString(3, "Milk 1L");
                insertProducts.executeUpdate();

                insertProducts.setInt(1, 2);
                insertProducts.setString(2, "BREAD-WHT");
                insertProducts.setString(3, "White Bread");
                insertProducts.executeUpdate();

                insertProducts.setInt(1, 3);
                insertProducts.setString(2, "APPLE-1KG");
                insertProducts.setString(3, "Apple 1kg");
                insertProducts.executeUpdate();
            }
        }

        int priceCount = countRows(connection, "prices");
        if (priceCount == 0) {
            try (PreparedStatement insertPrices = connection.prepareStatement("INSERT INTO prices (product_id, amount, currency, updated_at) VALUES (?, ?, ?, ?)") ) {
                insertPrices.setInt(1, 1);
                insertPrices.setBigDecimal(2, new BigDecimal("2.49"));
                insertPrices.setString(3, "EUR");
                insertPrices.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                insertPrices.executeUpdate();

                insertPrices.setInt(1, 2);
                insertPrices.setBigDecimal(2, new BigDecimal("1.99"));
                insertPrices.setString(3, "EUR");
                insertPrices.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                insertPrices.executeUpdate();

                insertPrices.setInt(1, 3);
                insertPrices.setBigDecimal(2, new BigDecimal("3.79"));
                insertPrices.setString(3, "EUR");
                insertPrices.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                insertPrices.executeUpdate();
            }
        }
    }

    static List<ProductPriceRow> readRows(Connection connection, int maxRecords) throws SQLException {
        List<ProductPriceRow> rows = new ArrayList<ProductPriceRow>();
        String sql = "SELECT p.sku, p.name, pr.amount, pr.currency, pr.updated_at "
                + "FROM products p JOIN prices pr ON pr.product_id = p.id "
                + "ORDER BY pr.updated_at DESC LIMIT ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, maxRecords);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ProductPriceRow(
                            rs.getString("sku"),
                            rs.getString("name"),
                            rs.getBigDecimal("amount"),
                            rs.getString("currency"),
                            rs.getTimestamp("updated_at")));
                }
            }
        }
        return rows;
    }

    static ApiCallResult postJson(String apiUrl, String payload) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            byte[] body = payload.getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(body);
            }

            int statusCode = connection.getResponseCode();
            InputStream stream = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String responseBody = stream == null ? "" : readStream(stream);

            String snippet = responseBody.length() > 180 ? responseBody.substring(0, 180) + "..." : responseBody;
            return new ApiCallResult(statusCode, snippet);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    static String toPayloadJson(ProductPriceRow row) {
        return "{"
                + "\"sku\":\"" + escapeJson(row.sku) + "\","
                + "\"name\":\"" + escapeJson(row.name) + "\","
                + "\"price\":" + row.amount + ","
                + "\"currency\":\"" + escapeJson(row.currency) + "\","
                + "\"updatedAt\":\"" + row.updatedAt.toInstant().toString() + "\""
                + "}";
    }

    static int parsePositiveInt(String first, Integer fallbackValue, int defaultValue, int maxValue) {
        Integer parsedFirst = tryParse(first);
        int candidate;
        if (parsedFirst != null) {
            candidate = parsedFirst.intValue();
        } else if (fallbackValue != null) {
            candidate = fallbackValue.intValue();
        } else {
            candidate = defaultValue;
        }

        if (candidate <= 0) {
            return defaultValue;
        }
        if (candidate > maxValue) {
            return maxValue;
        }
        return candidate;
    }

    static Integer parseBodyAsInt(String requestBody) {
        return tryParse(requestBody);
    }

    private static Integer tryParse(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String readEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    private static int countRows(Connection connection, String tableName) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String readStream(InputStream stream) throws Exception {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }

    static final class ProductPriceRow {
        final String sku;
        final String name;
        final BigDecimal amount;
        final String currency;
        final Timestamp updatedAt;

        ProductPriceRow(String sku, String name, BigDecimal amount, String currency, Timestamp updatedAt) {
            this.sku = sku;
            this.name = name;
            this.amount = amount;
            this.currency = currency;
            this.updatedAt = updatedAt;
        }
    }

    static final class ApiCallResult {
        final int statusCode;
        final String bodySnippet;

        ApiCallResult(int statusCode, String bodySnippet) {
            this.statusCode = statusCode;
            this.bodySnippet = bodySnippet;
        }
    }
}

