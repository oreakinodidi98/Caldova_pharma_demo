DECLARE
    PROCEDURE create_table_if_missing(table_name VARCHAR2, ddl VARCHAR2) IS
        table_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO table_count FROM all_tables
        WHERE owner = 'CALDOVA_OWNER' AND all_tables.table_name = create_table_if_missing.table_name;
        IF table_count = 0 THEN
            EXECUTE IMMEDIATE ddl;
        END IF;
    END;
BEGIN
    create_table_if_missing('PLANT',
        'CREATE TABLE CALDOVA_OWNER.plant (
            plant_id NUMBER(10) PRIMARY KEY,
            plant_code VARCHAR2(2) NOT NULL UNIQUE,
            plant_name VARCHAR2(80) NOT NULL)');
    create_table_if_missing('PRODUCTION_LINE',
        'CREATE TABLE CALDOVA_OWNER.production_line (
            line_id NUMBER(10) PRIMARY KEY,
            plant_id NUMBER(10) NOT NULL REFERENCES CALDOVA_OWNER.plant(plant_id),
            line_code VARCHAR2(12) NOT NULL UNIQUE,
            process_name VARCHAR2(80) NOT NULL,
            equipment_ref VARCHAR2(40) NOT NULL,
            line_status VARCHAR2(12) NOT NULL CHECK (line_status IN (''RUNNING'', ''ATTENTION'', ''STOPPED'')),
            snapshot_time TIMESTAMP NOT NULL,
            shifts_per_day NUMBER(1) DEFAULT 2 NOT NULL CHECK (shifts_per_day = 2),
            last_issue VARCHAR2(200) NOT NULL)');
    create_table_if_missing('ALARM',
        'CREATE TABLE CALDOVA_OWNER.alarm (
            alarm_id NUMBER(10) PRIMARY KEY,
            line_id NUMBER(10) NOT NULL REFERENCES CALDOVA_OWNER.production_line(line_id),
            recorded_time TIMESTAMP NOT NULL,
            alarm_code VARCHAR2(20) NOT NULL,
            severity VARCHAR2(20) NOT NULL,
            alarm_message VARCHAR2(200) NOT NULL)');
    create_table_if_missing('EQUIPMENT_SIGNAL',
        'CREATE TABLE CALDOVA_OWNER.equipment_signal (
            signal_id NUMBER(10) PRIMARY KEY,
            line_id NUMBER(10) NOT NULL REFERENCES CALDOVA_OWNER.production_line(line_id),
            signal_name VARCHAR2(80) NOT NULL,
            recorded_value VARCHAR2(80) NOT NULL,
            unit VARCHAR2(30),
            recorded_time TIMESTAMP NOT NULL)');
    create_table_if_missing('READINESS_CHECK',
        'CREATE TABLE CALDOVA_OWNER.readiness_check (
            check_id NUMBER(10) PRIMARY KEY,
            line_id NUMBER(10) NOT NULL REFERENCES CALDOVA_OWNER.production_line(line_id),
            check_name VARCHAR2(80) NOT NULL,
            recorded_result VARCHAR2(80) NOT NULL,
            recorded_time TIMESTAMP NOT NULL)');
    create_table_if_missing('MANUAL_EXCERPT',
        'CREATE TABLE CALDOVA_OWNER.manual_excerpt (
            excerpt_id NUMBER(10) PRIMARY KEY,
            line_id NUMBER(10) NOT NULL REFERENCES CALDOVA_OWNER.production_line(line_id),
            document_ref VARCHAR2(40) NOT NULL,
            title VARCHAR2(80) NOT NULL,
            revision VARCHAR2(20) NOT NULL,
            excerpt VARCHAR2(2000) NOT NULL)');
END;
/
