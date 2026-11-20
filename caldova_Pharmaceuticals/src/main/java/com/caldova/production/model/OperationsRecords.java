package com.caldova.production.model;

public final class OperationsRecords {
    private OperationsRecords() { }

    public static final class PlantSummary {
        public final String code;
        public final String name;
        public final int total;
        public final int running;
        public final int attention;
        public final int stopped;
        public PlantSummary(String code, String name, int total, int running, int attention, int stopped) {
            this.code = code; this.name = name; this.total = total;
            this.running = running; this.attention = attention; this.stopped = stopped;
        }
        public String getCode() { return code; }
        public String getName() { return name; }
        public int getTotal() { return total; }
        public int getRunning() { return running; }
        public int getAttention() { return attention; }
        public int getStopped() { return stopped; }
    }

    public static final class Maintenance {
        public final long id;
        public final long lineId;
        public final String lineCode;
        public final String plantCode;
        public final String plantName;
        public final String title;
        public final String status;
        public final String plannedDate;
        public final String completedDate;
        public final String note;
        public Maintenance(long id, long lineId, String lineCode, String plantCode, String plantName,
                           String title, String status, String plannedDate, String completedDate, String note) {
            this.id = id; this.lineId = lineId; this.lineCode = lineCode; this.plantCode = plantCode;
            this.plantName = plantName; this.title = title; this.status = status;
            this.plannedDate = plannedDate; this.completedDate = completedDate; this.note = note;
        }
        public long getId() { return id; }
        public long getLineId() { return lineId; }
        public String getLineCode() { return lineCode; }
        public String getPlantCode() { return plantCode; }
        public String getPlantName() { return plantName; }
        public String getTitle() { return title; }
        public String getStatus() { return status; }
        public String getStatusLabel() {
            switch (status) {
                case "SCHEDULED": return "Scheduled";
                case "IN_PROGRESS": return "In progress";
                case "COMPLETED": return "Completed";
                default: throw new IllegalStateException("Unknown recorded maintenance status");
            }
        }
        public String getCssClass() {
            switch (status) {
                case "SCHEDULED": return "scheduled";
                case "IN_PROGRESS": return "in-progress";
                case "COMPLETED": return "completed";
                default: throw new IllegalStateException("Unknown recorded maintenance status");
            }
        }
        public String getPlannedDate() { return plannedDate; }
        public String getCompletedDate() { return completedDate; }
        public String getNote() { return note; }
    }

    public static final class ShiftSummary {
        public final String plantCode;
        public final String plantName;
        public final String shiftCode;
        public final int employees;
        public ShiftSummary(String plantCode, String plantName, String shiftCode, int employees) {
            this.plantCode = plantCode; this.plantName = plantName;
            this.shiftCode = shiftCode; this.employees = employees;
        }
        public String getPlantCode() { return plantCode; }
        public String getPlantName() { return plantName; }
        public String getShiftCode() { return shiftCode; }
        public String getShiftName() { return shiftName(shiftCode); }
        public String getStartTime() { return startTime(shiftCode); }
        public String getEndTime() { return endTime(shiftCode); }
        public int getEmployees() { return employees; }
    }

    public static final class RotaEntry {
        public final String employeeCode;
        public final String employeeName;
        public final String role;
        public final String plantCode;
        public final String plantName;
        public final String date;
        public final String shiftCode;
        public RotaEntry(String employeeCode, String employeeName, String role, String plantCode,
                         String plantName, String date, String shiftCode) {
            this.employeeCode = employeeCode; this.employeeName = employeeName; this.role = role;
            this.plantCode = plantCode; this.plantName = plantName; this.date = date; this.shiftCode = shiftCode;
        }
        public String getEmployeeCode() { return employeeCode; }
        public String getEmployeeName() { return employeeName; }
        public String getRole() { return role; }
        public String getPlantCode() { return plantCode; }
        public String getPlantName() { return plantName; }
        public String getDate() { return date; }
        public String getShiftCode() { return shiftCode; }
        public String getShiftName() { return shiftName(shiftCode); }
        public String getStartTime() { return startTime(shiftCode); }
        public String getEndTime() { return endTime(shiftCode); }
    }

    private static String shiftName(String code) {
        if ("DAY".equals(code)) { return "Day"; }
        if ("NIGHT".equals(code)) { return "Night"; }
        throw new IllegalStateException("Unknown recorded shift");
    }
    private static String startTime(String code) {
        shiftName(code);
        return "DAY".equals(code) ? "06:00" : "18:00";
    }
    private static String endTime(String code) {
        shiftName(code);
        return "DAY".equals(code) ? "18:00" : "06:00 (+1 day)";
    }
}
