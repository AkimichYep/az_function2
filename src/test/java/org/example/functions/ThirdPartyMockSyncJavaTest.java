package org.example.functions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.List;

class ThirdPartyMockSyncJavaTest {

    @Test
    void parsePositiveIntFallsBackAndCaps() {
        Assertions.assertEquals(5, ThirdPartyMockSyncJava.parsePositiveInt(null, null, 5, 100));
        Assertions.assertEquals(5, ThirdPartyMockSyncJava.parsePositiveInt("0", null, 5, 100));
        Assertions.assertEquals(12, ThirdPartyMockSyncJava.parsePositiveInt("12", null, 5, 100));
        Assertions.assertEquals(100, ThirdPartyMockSyncJava.parsePositiveInt("250", null, 5, 100));
        Assertions.assertEquals(7, ThirdPartyMockSyncJava.parsePositiveInt(null, Integer.valueOf(7), 5, 100));
    }

    @Test
    void toPayloadJsonEscapesDoubleQuotes() {
        ThirdPartyMockSyncJava.ProductPriceRow row = new ThirdPartyMockSyncJava.ProductPriceRow(
                "A\"B",
                "Milk",
                new BigDecimal("2.49"),
                "EUR",
                Timestamp.valueOf("2026-05-19 12:00:00"));

        String payload = ThirdPartyMockSyncJava.toPayloadJson(row);
        Assertions.assertTrue(payload.contains("\\\""));
        Assertions.assertTrue(payload.contains("\"price\":2.49"));
    }

    @Test
    void schemaSeedProvidesRows() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "")) {
            ThirdPartyMockSyncJava.initializeSchemaAndSeed(connection);
            List<ThirdPartyMockSyncJava.ProductPriceRow> rows = ThirdPartyMockSyncJava.readRows(connection, 10);
            Assertions.assertFalse(rows.isEmpty());
        }
    }
}


