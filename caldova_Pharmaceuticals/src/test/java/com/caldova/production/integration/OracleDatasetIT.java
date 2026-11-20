package com.caldova.production.integration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OracleDatasetIT {
    @Autowired private DataSource datasource;

    @Test
    void datasetHasExactlyFourPlants160LinesAndTheAgreedStatusDistribution() throws Exception {
        try (Connection connection = datasource.getConnection()) {
            assertEquals(4, count(connection, "SELECT COUNT(*) FROM CALDOVA_OWNER.plant"));
            assertEquals(160, count(connection, "SELECT COUNT(*) FROM CALDOVA_OWNER.production_line"));
            assertEquals(4, count(connection, "SELECT COUNT(*) FROM (SELECT plant_id FROM "
                    + "CALDOVA_OWNER.production_line GROUP BY plant_id HAVING COUNT(*)=40)"));
            assertEquals(160, count(connection,
                    "SELECT COUNT(*) FROM CALDOVA_OWNER.production_line WHERE shifts_per_day=2"));
            assertEquals(124, count(connection,
                    "SELECT COUNT(*) FROM CALDOVA_OWNER.production_line WHERE line_status='RUNNING'"));
            assertEquals(24, count(connection,
                    "SELECT COUNT(*) FROM CALDOVA_OWNER.production_line WHERE line_status='ATTENTION'"));
            assertEquals(12, count(connection,
                    "SELECT COUNT(*) FROM CALDOVA_OWNER.production_line WHERE line_status='STOPPED'"));
            for (String table : new String[]{"alarm", "equipment_signal", "readiness_check", "manual_excerpt"}) {
                assertEquals(480, count(connection, "SELECT COUNT(*) FROM CALDOVA_OWNER." + table));
            }
        }
    }

    @Test
    void runtimeAccountCanReadButCannotModifyRecordedData() throws Exception {
        try (Connection connection = datasource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                SQLException denied = assertThrows(SQLException.class, () ->
                        connection.createStatement().executeUpdate(
                                "UPDATE CALDOVA_OWNER.production_line SET last_issue=last_issue WHERE line_id=1"));
                assertTrue(denied.getErrorCode() == 41900 || denied.getErrorCode() == 1031,
                        "Expected Oracle's missing-object-privilege error.");
            } finally { connection.rollback(); }
        }
    }

    private static int count(Connection connection, String sql) throws SQLException {
        try (java.sql.Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            if (!rs.next()) { throw new SQLException("No count was returned"); }
            return rs.getInt(1);
        }
    }
}
