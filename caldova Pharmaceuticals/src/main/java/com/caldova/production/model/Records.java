package com.caldova.production.model;

import java.util.Collections;
import java.util.List;

public final class Records {
    private Records() { }

    public static final class Plant {
        public final String code;
        public final String name;
        public Plant(String code, String name) { this.code = code; this.name = name; }
        public String getCode() { return code; }
        public String getName() { return name; }
    }

    public static final class Line {
        public final long id;
        public final String code;
        public final String plantCode;
        public final String plantName;
        public final String process;
        public final String equipmentReference;
        public final LineStatus status;
        public final String snapshotTime;
        public final String lastIssue;

        public Line(long id, String code, String plantCode, String plantName, String process,
                    String equipmentReference, LineStatus status, String snapshotTime, String lastIssue) {
            this.id = id;
            this.code = code;
            this.plantCode = plantCode;
            this.plantName = plantName;
            this.process = process;
            this.equipmentReference = equipmentReference;
            this.status = status;
            this.snapshotTime = snapshotTime;
            this.lastIssue = lastIssue;
        }
        public long getId() { return id; }
        public String getCode() { return code; }
        public String getPlantCode() { return plantCode; }
        public String getPlantName() { return plantName; }
        public String getProcess() { return process; }
        public String getEquipmentReference() { return equipmentReference; }
        public LineStatus getStatus() { return status; }
        public String getSnapshotTime() { return snapshotTime; }
        public String getLastIssue() { return lastIssue; }
    }

    public static final class Alarm {
        public final String recordedTime;
        public final String code;
        public final String severity;
        public final String message;
        public Alarm(String recordedTime, String code, String severity, String message) {
            this.recordedTime = recordedTime; this.code = code;
            this.severity = severity; this.message = message;
        }
        public String getRecordedTime() { return recordedTime; }
        public String getCode() { return code; }
        public String getSeverity() { return severity; }
        public String getMessage() { return message; }
    }

    public static final class Signal {
        public final String name;
        public final String value;
        public final String unit;
        public final String recordedTime;
        public Signal(String name, String value, String unit, String recordedTime) {
            this.name = name; this.value = value; this.unit = unit; this.recordedTime = recordedTime;
        }
        public String getName() { return name; }
        public String getValue() { return value; }
        public String getUnit() { return unit; }
        public String getRecordedTime() { return recordedTime; }
    }

    public static final class Check {
        public final String name;
        public final String result;
        public final String recordedTime;
        public Check(String name, String result, String recordedTime) {
            this.name = name; this.result = result; this.recordedTime = recordedTime;
        }
        public String getName() { return name; }
        public String getResult() { return result; }
        public String getRecordedTime() { return recordedTime; }
    }

    public static final class Manual {
        public final String reference;
        public final String title;
        public final String revision;
        public final String excerpt;
        public Manual(String reference, String title, String revision, String excerpt) {
            this.reference = reference; this.title = title; this.revision = revision; this.excerpt = excerpt;
        }
        public String getReference() { return reference; }
        public String getTitle() { return title; }
        public String getRevision() { return revision; }
        public String getExcerpt() { return excerpt; }
    }

    public static final class Detail {
        public final Line line;
        public final List<Alarm> alarms;
        public final List<Signal> signals;
        public final List<Check> checks;
        public final List<Manual> manuals;
        public Detail(Line line, List<Alarm> alarms, List<Signal> signals,
                      List<Check> checks, List<Manual> manuals) {
            this.line = line;
            this.alarms = Collections.unmodifiableList(alarms);
            this.signals = Collections.unmodifiableList(signals);
            this.checks = Collections.unmodifiableList(checks);
            this.manuals = Collections.unmodifiableList(manuals);
        }
        public Line getLine() { return line; }
        public List<Alarm> getAlarms() { return alarms; }
        public List<Signal> getSignals() { return signals; }
        public List<Check> getChecks() { return checks; }
        public List<Manual> getManuals() { return manuals; }
    }
}
