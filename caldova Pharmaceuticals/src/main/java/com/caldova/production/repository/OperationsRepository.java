package com.caldova.production.repository;

import com.caldova.production.model.OperationsRecords;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OperationsRepository {
    private static final String MAINTENANCE_SELECT = "SELECT w.work_id,l.line_id,l.line_code,p.plant_code,p.plant_name,"
            + "w.title,w.work_status,TO_CHAR(w.planned_date,'YYYY-MM-DD'),"
            + "TO_CHAR(w.completed_date,'YYYY-MM-DD'),w.work_note "
            + "FROM CALDOVA_OWNER.maintenance_work w JOIN CALDOVA_OWNER.production_line l ON l.line_id=w.line_id "
            + "JOIN CALDOVA_OWNER.plant p ON p.plant_id=l.plant_id ";
    private final NamedParameterJdbcTemplate jdbc;

    public OperationsRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<OperationsRecords.PlantSummary> plantSummaries() {
        return jdbc.query("SELECT p.plant_code,p.plant_name,COUNT(l.line_id),"
                        + "SUM(CASE WHEN l.line_status='RUNNING' THEN 1 ELSE 0 END),"
                        + "SUM(CASE WHEN l.line_status='ATTENTION' THEN 1 ELSE 0 END),"
                        + "SUM(CASE WHEN l.line_status='STOPPED' THEN 1 ELSE 0 END) "
                        + "FROM CALDOVA_OWNER.plant p LEFT JOIN CALDOVA_OWNER.production_line l ON l.plant_id=p.plant_id "
                        + "GROUP BY p.plant_code,p.plant_name ORDER BY p.plant_code",
                (rs, row) -> new OperationsRecords.PlantSummary(rs.getString(1),rs.getString(2),
                        rs.getInt(3),rs.getInt(4),rs.getInt(5),rs.getInt(6)));
    }

    public List<OperationsRecords.Maintenance> maintenance(String plant, String status) {
        Map<String,Object> values = new HashMap<>();
        values.put("plant",plant); values.put("status",status);
        return jdbc.query(MAINTENANCE_SELECT
                        + "WHERE (:plant IS NULL OR p.plant_code=:plant) AND (:status IS NULL OR w.work_status=:status) "
                        + "ORDER BY w.planned_date,w.work_id", values,
                (rs,row) -> new OperationsRecords.Maintenance(rs.getLong(1),rs.getLong(2),rs.getString(3),
                        rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8),
                        rs.getString(9),rs.getString(10)));
    }

    public List<OperationsRecords.RotaEntry> rota(String plant, LocalDate date, String shift) {
        Map<String,Object> values = new HashMap<>();
        values.put("plant",plant); values.put("date",Date.valueOf(date)); values.put("shift",shift);
        return jdbc.query("SELECT e.employee_code,e.employee_name,e.employee_role,p.plant_code,p.plant_name,"
                        + "TO_CHAR(a.shift_date,'YYYY-MM-DD'),a.shift_code "
                        + "FROM CALDOVA_OWNER.shift_assignment a JOIN CALDOVA_OWNER.employee e ON e.employee_id=a.employee_id "
                        + "JOIN CALDOVA_OWNER.plant p ON p.plant_id=e.plant_id "
                        + "WHERE (:plant IS NULL OR p.plant_code=:plant) AND a.shift_date=:date "
                        + "AND (:shift IS NULL OR a.shift_code=:shift) ORDER BY p.plant_code,a.shift_code,e.employee_code",
                values,(rs,row) -> new OperationsRecords.RotaEntry(rs.getString(1),rs.getString(2),rs.getString(3),
                        rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7)));
    }

    public List<OperationsRecords.ShiftSummary> shiftSummaries(LocalDate date) {
        return jdbc.query("SELECT p.plant_code,p.plant_name,a.shift_code,COUNT(*) "
                        + "FROM CALDOVA_OWNER.shift_assignment a JOIN CALDOVA_OWNER.employee e ON e.employee_id=a.employee_id "
                        + "JOIN CALDOVA_OWNER.plant p ON p.plant_id=e.plant_id WHERE a.shift_date=:date "
                        + "GROUP BY p.plant_code,p.plant_name,a.shift_code ORDER BY p.plant_code,a.shift_code",
                java.util.Collections.singletonMap("date",Date.valueOf(date)),
                (rs,row) -> new OperationsRecords.ShiftSummary(rs.getString(1),rs.getString(2),rs.getString(3),rs.getInt(4)));
    }
}
