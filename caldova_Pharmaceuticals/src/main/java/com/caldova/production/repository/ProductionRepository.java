package com.caldova.production.repository;

import com.caldova.production.model.LineStatus;
import com.caldova.production.model.Records;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductionRepository {
    private static final String LINE_SELECT = "SELECT l.line_id, l.line_code, p.plant_code, p.plant_name, "
            + "l.process_name, l.equipment_ref, l.line_status, "
            + "TO_CHAR(l.snapshot_time, 'YYYY-MM-DD HH24:MI') AS snapshot_time, l.last_issue "
            + "FROM CALDOVA_OWNER.production_line l JOIN CALDOVA_OWNER.plant p ON p.plant_id=l.plant_id ";
    private static final RowMapper<Records.Line> LINE_MAPPER = ProductionRepository::mapLine;
    private final NamedParameterJdbcTemplate jdbc;

    public ProductionRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Records.Plant> plants() {
        return jdbc.query("SELECT plant_code, plant_name FROM CALDOVA_OWNER.plant ORDER BY plant_code",
                (rs, row) -> new Records.Plant(rs.getString(1), rs.getString(2)));
    }

    public List<Records.Line> lines(String plant, String status) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("plant", plant);
        parameters.put("status", status);
        return jdbc.query(LINE_SELECT
                + "WHERE (:plant IS NULL OR p.plant_code=:plant) "
                + "AND (:status IS NULL OR l.line_status=:status) ORDER BY p.plant_code, l.line_code",
                parameters, LINE_MAPPER);
    }

    public int lineCount() {
        Integer count = jdbc.getJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM CALDOVA_OWNER.production_line", Integer.class);
        if (count == null) { throw new IllegalStateException("Oracle returned no line count"); }
        return count;
    }

    public Optional<Records.Line> line(long id) {
        List<Records.Line> results = jdbc.query(LINE_SELECT + "WHERE l.line_id=:id",
                java.util.Collections.singletonMap("id", id), LINE_MAPPER);
        return results.stream().findFirst();
    }

    public List<Records.Alarm> alarms(long id) {
        return jdbc.query("SELECT TO_CHAR(recorded_time, 'YYYY-MM-DD HH24:MI:SS'), alarm_code, severity, "
                        + "alarm_message FROM CALDOVA_OWNER.alarm WHERE line_id=:id "
                        + "ORDER BY recorded_time DESC, alarm_id DESC",
                java.util.Collections.singletonMap("id", id),
                (rs, row) -> new Records.Alarm(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
    }

    public List<Records.Signal> signals(long id) {
        return jdbc.query("SELECT signal_name, recorded_value, unit, "
                        + "TO_CHAR(recorded_time, 'YYYY-MM-DD HH24:MI:SS') "
                        + "FROM CALDOVA_OWNER.equipment_signal WHERE line_id=:id ORDER BY signal_id",
                java.util.Collections.singletonMap("id", id),
                (rs, row) -> new Records.Signal(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
    }

    public List<Records.Check> checks(long id) {
        return jdbc.query("SELECT check_name, recorded_result, TO_CHAR(recorded_time, 'YYYY-MM-DD HH24:MI:SS') "
                        + "FROM CALDOVA_OWNER.readiness_check WHERE line_id=:id ORDER BY check_id",
                java.util.Collections.singletonMap("id", id),
                (rs, row) -> new Records.Check(rs.getString(1), rs.getString(2), rs.getString(3)));
    }

    public List<Records.Manual> manuals(long id) {
        return jdbc.query("SELECT document_ref, title, revision, excerpt "
                        + "FROM CALDOVA_OWNER.manual_excerpt WHERE line_id=:id ORDER BY excerpt_id",
                java.util.Collections.singletonMap("id", id),
                (rs, row) -> new Records.Manual(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
    }

    public void checkConnection() {
        jdbc.getJdbcTemplate().queryForObject("SELECT 1 FROM CALDOVA_OWNER.production_line WHERE line_id=1",
                Integer.class);
    }

    private static Records.Line mapLine(ResultSet rs, int row) throws SQLException {
        return new Records.Line(rs.getLong("line_id"), rs.getString("line_code"),
                rs.getString("plant_code"), rs.getString("plant_name"), rs.getString("process_name"),
                rs.getString("equipment_ref"), LineStatus.valueOf(rs.getString("line_status")),
                rs.getString("snapshot_time") + " UTC", rs.getString("last_issue"));
    }
}
