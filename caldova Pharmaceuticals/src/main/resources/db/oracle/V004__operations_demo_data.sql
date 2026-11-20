DECLARE
    TYPE texts IS TABLE OF VARCHAR2(100);
    names texts := texts('Avery Cole','Blair Reed','Casey Gray','Drew Lane',
        'Ellis Stone','Finley Brooks','Harper Quinn','Jordan Vale',
        'Kai Rivers','Lee Morgan','Marley West','Noel Fields',
        'Oakley Wells','Parker Hale','Reese Bell','Sage Ford');
    titles texts := texts('Recorded equipment inspection','Planned service review','Scheduled condition check');
    codes texts := texts('NB','WH','EM','SW');
    v_id NUMBER;
    v_assignment NUMBER;
    v_work NUMBER;
    v_status VARCHAR2(20);
    v_role VARCHAR2(60);
    v_shift VARCHAR2(10);
BEGIN
    FOR p IN 1..4 LOOP
        FOR n IN 1..4 LOOP
            v_id := (p-1)*4+n;
            v_role := CASE WHEN MOD(n,2)=1 THEN 'Shift lead' ELSE 'Line operator' END;
            MERGE INTO CALDOVA_OWNER.employee d USING (SELECT v_id id FROM dual) s ON (d.employee_id=s.id)
            WHEN MATCHED THEN UPDATE SET plant_id=p,employee_code=codes(p)||'-EMP'||TO_CHAR(n,'FM000'),
                employee_name=names(v_id),employee_role=v_role
            WHEN NOT MATCHED THEN INSERT VALUES (v_id,p,codes(p)||'-EMP'||TO_CHAR(n,'FM000'),names(v_id),v_role);
            FOR day_offset IN 0..2 LOOP
                v_assignment := day_offset*16+v_id;
                v_shift := CASE WHEN n<=2 THEN 'DAY' ELSE 'NIGHT' END;
                MERGE INTO CALDOVA_OWNER.shift_assignment d USING (SELECT v_assignment id FROM dual) s
                ON (d.assignment_id=s.id)
                WHEN MATCHED THEN UPDATE SET employee_id=v_id,shift_date=DATE '2026-11-19'+day_offset,shift_code=v_shift
                WHEN NOT MATCHED THEN INSERT VALUES (v_assignment,v_id,DATE '2026-11-19'+day_offset,v_shift);
            END LOOP;
        END LOOP;
        FOR n IN 1..3 LOOP
            v_work := (p-1)*3+n;
            v_status := CASE n WHEN 1 THEN 'COMPLETED' WHEN 2 THEN 'IN_PROGRESS' ELSE 'SCHEDULED' END;
            MERGE INTO CALDOVA_OWNER.maintenance_work d USING (SELECT v_work id FROM dual) s ON (d.work_id=s.id)
            WHEN MATCHED THEN UPDATE SET line_id=(p-1)*40+n,title=titles(n),work_status=v_status,
                planned_date=DATE '2026-11-17'+n,
                completed_date=CASE WHEN n=1 THEN DATE '2026-11-18' ELSE NULL END,
                work_note='Fictional maintenance record. No operating or safety procedure is provided.'
            WHEN NOT MATCHED THEN INSERT VALUES
                (v_work,(p-1)*40+n,titles(n),v_status,DATE '2026-11-17'+n,
                 CASE WHEN n=1 THEN DATE '2026-11-18' ELSE NULL END,
                 'Fictional maintenance record. No operating or safety procedure is provided.');
        END LOOP;
    END LOOP;
END;
/
COMMIT;
