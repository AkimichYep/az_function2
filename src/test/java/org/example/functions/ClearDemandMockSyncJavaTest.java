package org.example.functions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.List;

class ClearDemandMockSyncJavaTest {

    @Test
    void parsePositiveIntFallsBackAndCaps() {
        Assertions.assertEquals(5, ClearDemandMockSyncJava.parsePositiveInt(null, null, 5, 100));
        Assertions.assertEquals(5, ClearDemandMockSyncJava.parsePositiveInt("0", null, 5, 100));
        Assertions.assertEquals(12, ClearDemandMockSyncJava.parsePositiveInt("12", null, 5, 100));
        Assertions.assertEquals(100, ClearDemandMockSyncJava.parsePositiveInt("250", null, 5, 100));
        Assertions.assertEquals(7, ClearDemandMockSyncJava.parsePositiveInt(null, Integer.valueOf(7), 5, 100));
    }

    @Test
    void toPayloadJsonEscapesDoubleQuotes() {
        ClearDemandMockSyncJava.ProductPriceRow row = new ClearDemandMockSyncJava.ProductPriceRow(
                "A\"B",
                "Milk",
                new BigDecimal("2.49"),
                "EUR",
                Timestamp.valueOf("2026-05-19 12:00:00"));

        String payload = ClearDemandMockSyncJava.toPayloadJson(row);
        Assertions.assertTrue(payload.contains("\\\""));
        Assertions.assertTrue(payload.contains("\"price\":2.49"));
    }

    @Test
    void schemaSeedProvidesRows() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")) {
            ClearDemandMockSyncJava.initializeSchemaAndSeed(connection);
            List<ClearDemandMockSyncJava.ProductPriceRow> rows = ClearDemandMockSyncJava.readRows(connection, 10);
            Assertions.assertFalse(rows.isEmpty());
        }
    }
}

