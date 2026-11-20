DECLARE
    PROCEDURE create_table_if_missing(table_name VARCHAR2, ddl VARCHAR2) IS
        table_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO table_count FROM all_tables
        WHERE owner='CALDOVA_OWNER' AND all_tables.table_name=create_table_if_missing.table_name;
        IF table_count=0 THEN EXECUTE IMMEDIATE ddl; END IF;
    END;
BEGIN
    create_table_if_missing('MAINTENANCE_WORK',
        'CREATE TABLE CALDOVA_OWNER.maintenance_work (
            work_id NUMBER(10) PRIMARY KEY,
            line_id NUMBER(10) NOT NULL REFERENCES CALDOVA_OWNER.production_line(line_id),
            title VARCHAR2(100) NOT NULL,
            work_status VARCHAR2(20) NOT NULL CHECK (work_status IN (''SCHEDULED'',''IN_PROGRESS'',''COMPLETED'')),
            planned_date DATE NOT NULL,
            completed_date DATE,
            work_note VARCHAR2(500) NOT NULL,
            CHECK ((work_status=''COMPLETED'' AND completed_date IS NOT NULL) OR
                   (work_status<>''COMPLETED'' AND completed_date IS NULL)))');
    create_table_if_missing('EMPLOYEE',
        'CREATE TABLE CALDOVA_OWNER.employee (
            employee_id NUMBER(10) PRIMARY KEY,
            plant_id NUMBER(10) NOT NULL REFERENCES CALDOVA_OWNER.plant(plant_id),
            employee_code VARCHAR2(20) NOT NULL UNIQUE,
            employee_name VARCHAR2(80) NOT NULL,
            employee_role VARCHAR2(60) NOT NULL)');
    create_table_if_missing('SHIFT_ASSIGNMENT',
        'CREATE TABLE CALDOVA_OWNER.shift_assignment (
            assignment_id NUMBER(10) PRIMARY KEY,
            employee_id NUMBER(10) NOT NULL REFERENCES CALDOVA_OWNER.employee(employee_id),
            shift_date DATE NOT NULL,
            shift_code VARCHAR2(10) NOT NULL CHECK (shift_code IN (''DAY'',''NIGHT'')),
            UNIQUE (employee_id,shift_date))');
    create_table_if_missing('APP_USER',
        'CREATE TABLE CALDOVA_OWNER.app_user (
            username VARCHAR2(64) PRIMARY KEY,
            password_hash VARCHAR2(100) NOT NULL,
            enabled NUMBER(1) DEFAULT 1 NOT NULL CHECK (enabled IN (0,1)))');
END;
/
